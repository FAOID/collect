package org.openforis.collect.remoting.service.dataImport;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.RecordDao;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.persistence.xml.CollectIdmlBindingContext;
import org.openforis.collect.persistence.xml.DataHandler;
import org.openforis.collect.persistence.xml.DataUnmarshaller;
import org.openforis.collect.persistence.xml.DataUnmarshallerException;
import org.openforis.idm.metamodel.xml.InvalidIdmlException;
import org.openforis.idm.metamodel.xml.SurveyUnmarshaller;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.expression.ExpressionFactory;

/**
 * 
 * @author S. Ricci
 * 
 */
public class DataImportProcess implements Callable<Void> {

	private static Log LOG = LogFactory.getLog(DataImportProcess.class);

	private static final Object IDML_FILE_NAME = "idml.xml";

	private RecordManager recordManager;
	private RecordDao recordDao;
	private SurveyManager surveyManager;

	private Map<String, User> users;
	private String surveyName;
	private DataImportState state;
	private File packagedFile;

	private CollectSurvey packagedSurvey;
	private CollectSurvey existingSurvey;

	private boolean overwriteAll;

	private boolean overwriteExistingRecordInConflict;

	private DataUnmarshaller dataUnmarshaller;

	public DataImportProcess(SurveyManager surveyManager, RecordManager recordManager, RecordDao recordDao, Map<String, User> users, File packagedFile, boolean overwriteAll) {
		super();
		this.surveyManager = surveyManager;
		this.recordManager = recordManager;
		this.recordDao = recordDao;
		this.users = users;
		this.packagedFile = packagedFile;
		this.overwriteAll = overwriteAll;
		this.state = new DataImportState();
		this.overwriteExistingRecordInConflict = false;
	}

	public DataImportState getState() {
		return state;
	}

	public void cancel() {
		state.setCancelled(true);
		state.setRunning(false);
		state.setStep(DataImportState.Step.CANCELLED);
	}

	public boolean isRunning() {
		return state.isRunning();
	}

	public boolean isComplete() {
		return state.isComplete();
	}

	public void init() throws DataImportExeption {
		try {
			state.reset();
			state.setStep(DataImportState.Step.PREPARE);
			packagedSurvey = extractPackagedSurvey();

			String uri = packagedSurvey.getUri();
			CollectSurvey survey = surveyManager.getByUri(uri);
			state.setNewSurvey(survey == null);
			Map<CollectRecord.Step, Integer> totalRecords = new HashMap<CollectRecord.Step, Integer>();
			int total = 0;
			ZipFile zipFile = new ZipFile(packagedFile);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry zipEntry = (ZipEntry) entries.nextElement();
				String entryName = zipEntry.getName();
				if (zipEntry.isDirectory() || IDML_FILE_NAME.equals(entryName)) {
					continue;
				}
				Step step = getStep(entryName);
				Integer totalPerStep = totalRecords.get(step);
				if (totalPerStep == null) {
					totalPerStep = 0;
				}
				totalRecords.put(step, totalPerStep + 1);
				total++;
			}
			state.setTotal(total);
			state.setTotalPerStep(totalRecords);
			state.setStep(DataImportState.Step.INITED);
		} catch (Exception e) {
			throw new DataImportExeption("Error initializing data import process", e);
		}
	}

	@Override
	public Void call() throws Exception {
		try {
			state.setRunning(true);
			if (state.getStep() == DataImportState.Step.INITED) {
				dataUnmarshaller = initDataUnmarshaller();
			}
			Step[] steps = Step.values();
			for (Step step : steps) {
				ZipFile zipFile = new ZipFile(packagedFile);
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				while (entries.hasMoreElements()) {
					ZipEntry zipEntry = (ZipEntry) entries.nextElement();
					String entryName = zipEntry.getName();
					if (!zipEntry.isDirectory() && !IDML_FILE_NAME.equals(entryName)) {
						Step entryStep = getStep(entryName);
						if (entryStep == step) {
							DataImportState.Step dataImportStep = state.getStep();
							if (dataImportStep == DataImportState.Step.CONFLICT && !state.getConflictingEntryName().equals(entryName)) {
								continue;
							}
							InputStream entryInputStream = zipFile.getInputStream(zipEntry);
							state.setStep(DataImportState.Step.IMPORTING);
							importZipEntry(entryInputStream, entryName, step);
							entryInputStream.close();
							dataImportStep = state.getStep();
							if (dataImportStep == DataImportState.Step.CONFLICT) {
								zipFile.close();
								return null;
							} else {
								overwriteExistingRecordInConflict = false;
							}
						}
					}
				}
				zipFile.close();
			}
			DataImportState.Step step = state.getStep();
			if (!(step == DataImportState.Step.CANCELLED || step == DataImportState.Step.CONFLICT)) {
				state.setStep(DataImportState.Step.COMPLETE);
			}
		} catch (Exception e) {
			state.setError(true);
			state.setErrorMessage(e.getMessage());
			state.setStep(DataImportState.Step.ERROR);
			LOG.error("Error during data export", e);
		} finally {
			state.setRunning(false);
		}
		return null;
	}

	private DataUnmarshaller initDataUnmarshaller() throws SurveyImportException {
		DataHandler handler;
		if (state.isNewSurvey()) {
			packagedSurvey.setName(surveyName);
			surveyManager.importModel(packagedSurvey);
			handler = new DataHandler(packagedSurvey, users);
		} else {
			String uri = packagedSurvey.getUri();
			existingSurvey = surveyManager.getByUri(uri);
			// TODO compare packaged survey with the one into db matching surveyName (if any)
			handler = new DataHandler(existingSurvey, packagedSurvey, users);
		}
		DataUnmarshaller dataUnmarshaller = new DataUnmarshaller(handler);
		return dataUnmarshaller;
	}

	private void importZipEntry(InputStream inputStream, String entryName, Step step) throws IOException {
		LOG.info("Extracting: " + entryName);
		InputStreamReader reader = new InputStreamReader(inputStream);
		ParseRecordResult parseRecordResult = parseRecord(dataUnmarshaller, reader);
		CollectRecord parsedRecord = parseRecordResult.record;
		String message = parseRecordResult.message;
		DataImportState.Step dataImportStep = state.getStep();
		Boolean promptForConflict = !overwriteAll && dataImportStep != DataImportState.Step.CONFLICT;
		Boolean overwriteExistingRecord = overwriteAll || dataImportStep == DataImportState.Step.CONFLICT && overwriteExistingRecordInConflict;
		if (parsedRecord == null) {
			state.addError(entryName, message);
			state.incrementCount();
		} else {
			parsedRecord.setStep(step);
			CollectRecord oldRecord = findAlreadyExistingRecord(parsedRecord);
			if (oldRecord != null) {
				if (overwriteExistingRecord) {
					replaceData(parsedRecord, oldRecord);
					recordDao.update(oldRecord);
					state.incrementUpdatedCount();
					LOG.info("Updated: " + oldRecord.getId() + " (from file " + entryName + ")");
				} else if (promptForConflict) {
					DataImportConflict conflict = new DataImportConflict(entryName, oldRecord, parsedRecord);
					state.setStep(DataImportState.Step.CONFLICT);
					state.setConflict(conflict);
					state.setConflictingEntryName(entryName);
				} else {
					// skip record
					state.incrementCount();
				}
			} else {
				recordDao.insert(parsedRecord);
				state.incrementInsertedCount();
				LOG.info("Inserted: " + parsedRecord.getId() + " (from file " + entryName + ")");
			}
		}
		if (!parseRecordResult.success) {
			if (parseRecordResult.warnings > 0) {
				state.addWarning(entryName, message);
			} else {
				state.addError(entryName, message);
			}
		}
	}

	private CollectRecord findAlreadyExistingRecord(CollectRecord parsedRecord) {
		CollectSurvey survey = (CollectSurvey) parsedRecord.getSurvey();
		List<String> keyValues = parsedRecord.getRootEntityKeyValues();
		Entity rootEntity = parsedRecord.getRootEntity();
		String rootEntityName = rootEntity.getName();
		List<CollectRecord> oldRecords = recordManager.loadSummaries(survey, rootEntityName, keyValues.toArray(new String[0]));
		if (oldRecords != null && oldRecords.size() == 1) {
			return oldRecords.get(0);
		} else {
			return null;
		}
	}

	public CollectSurvey extractPackagedSurvey() throws IOException, InvalidIdmlException {
		ZipFile zipFile = new ZipFile(packagedFile);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry zipEntry = (ZipEntry) entries.nextElement();
			if (zipEntry.isDirectory()) {
				continue;
			}
			String entryName = zipEntry.getName();
			if (IDML_FILE_NAME.equals(entryName)) {
				InputStream is = zipFile.getInputStream(zipEntry);
				CollectSurvey survey = unmarshalSurvey(is);
				return survey;
			}
		}
		return null;
	}

	public CollectSurvey unmarshalSurvey(InputStream is) throws InvalidIdmlException {
		CollectSurvey survey = surveyManager.unmarshalSurvey(is);
		return survey;
	}

	public CollectSurvey importSurvey(String name, String idmlFilename) throws Exception {
		CollectSurvey survey = surveyManager.get(name);
		if (survey == null) {
			CollectIdmlBindingContext idmlBindingContext = new CollectIdmlBindingContext(new CollectSurveyContext(new ExpressionFactory(), null, null));
			SurveyUnmarshaller surveyUnmarshaller = idmlBindingContext.createSurveyUnmarshaller();
			survey = (CollectSurvey) surveyUnmarshaller.unmarshal(idmlFilename);
			survey.setName(name);
			surveyManager.importModel(survey);
		}
		return survey;
	}

	private ParseRecordResult parseRecord(DataUnmarshaller dataUnmarshaller, Reader reader) throws IOException {
		ParseRecordResult result = new ParseRecordResult();
		try {
			CollectRecord record = dataUnmarshaller.parse(reader);
			recordManager.addEmptyNodes(record.getRootEntity());
			result.record = record;
			List<String> warns = dataUnmarshaller.getLastParsingWarnings();
			if (warns.size() > 0) {
				result.message = "Processed with errors: " + warns.toString();
				result.warnings = warns.size();
				result.success = false;
			} else {
				result.success = true;
				record.updateDerivedStates();
				record.updateRootEntityKeyValues();
				record.updateEntityCounts();
			}
		} catch (DataUnmarshallerException e) {
			result.message = "Unable to process: " + e.getMessages().toString();
		} catch (RuntimeException e) {
			result.message = "Unable to process: " + e.toString();
		}
		return result;
	}

	private Step getStep(String zipEntryName) throws DataImportExeption {
		String entryPathSeparator = Pattern.quote(File.separator);
		String[] entryNameSplitted = zipEntryName.split(entryPathSeparator);
		if (entryNameSplitted.length != 2) {
			entryPathSeparator = Pattern.quote("/");
			entryNameSplitted = zipEntryName.split(entryPathSeparator);
		}
		if (entryNameSplitted.length != 2) {
			throw new DataImportExeption("Packaged file format exception: wrong entry name: " + zipEntryName);
		}
		String stepNumStr = entryNameSplitted[0];
		int stepNumber = Integer.parseInt(stepNumStr);
		return Step.valueOf(stepNumber);
	}

	private void replaceData(CollectRecord fromRecord, CollectRecord toRecord) {
		toRecord.setCreatedBy(fromRecord.getCreatedBy());
		toRecord.setCreationDate(fromRecord.getCreationDate());
		toRecord.setModifiedBy(fromRecord.getModifiedBy());
		toRecord.setModifiedDate(fromRecord.getModifiedDate());
		toRecord.setStep(fromRecord.getStep());
		toRecord.setState(fromRecord.getState());
		toRecord.setRootEntity(fromRecord.getRootEntity());
		toRecord.updateDerivedStates();
		toRecord.updateRootEntityKeyValues();
		toRecord.updateEntityCounts();
	}

	public void setOverwriteExistingRecordInConflict(boolean overwriteExistingRecordInConflict) {
		this.overwriteExistingRecordInConflict = overwriteExistingRecordInConflict;
	}

	public boolean isOverwriteAll() {
		return overwriteAll;
	}

	public void setOverwriteAll(boolean overwriteAll) {
		this.overwriteAll = overwriteAll;
	}

	public void setSurveyName(String surveyName) {
		this.surveyName = surveyName;
	}

	private class ParseRecordResult {
		private boolean success;
		private String message;
		private int warnings;
		private CollectRecord record;

		public ParseRecordResult() {
			success = false;
		}
	}

}
