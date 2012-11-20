/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.designer.util.MessageUtil;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.persistence.SurveyImportException;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.util.logging.Log;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.annotation.WireVariable;

/**
 * @author S. Ricci
 *
 */
public class SurveyImportVM extends SurveyBaseVM {

	private static final Log log = Log.lookup(SurveyImportVM.class);

	private static final String TEXT_XML_CONTENT = "text/xml";
	
	@WireVariable
	private SurveyManager surveyManager;

	private String surveyName;
	private String fileName;
	private CollectSurvey uploadedSurvey;

	@Command
	public void importSurvey() {
		if ( validateForm() ) {
			final String name = surveyName;
			if ( existsSurveyWithName(name) ) {
				Object[] args = new String[] {name};
				MessageUtil.showConfirm(new MessageUtil.ConfirmHandler() {
					@Override
					public void onOk() {
						processSurveyImport(name, true);
					}
				}, "survey.import_survey.confirm_overwrite", args);
			} else {
				processSurveyImport(name, false);
			}
		}
	}
	
	protected boolean validateForm() {
		if (StringUtils.isBlank(surveyName) ) {
			MessageUtil.showWarning("survey.import_survey.specify_name");
			return false;
		} else if ( uploadedSurvey == null ) {
			MessageUtil.showWarning("survey.import_survey.upload_a_file");
			return false;
		} else if ( existsSurveyWithSameUriButDifferentName(surveyName, uploadedSurvey.getUri()) ) {
			MessageUtil.showWarning("survey.import_survey.exists_survey_same_uri");
			return false;
		} else {
			return true;
		}
	}

	@Command
	public void fileUploaded(@ContextParam(ContextType.TRIGGER_EVENT) UploadEvent event) {
		Media media = event.getMedia();
		String contentType = media.getContentType();
		if ( contentType.equals(TEXT_XML_CONTENT) ) {
			fileName = media.getName();
			InputStream is = getInputStream(media);
			uploadedSurvey = unmarshalSurvey(is);
			notifyChange("fileName","uploadedSurvey");
		} else {
			throw new IllegalArgumentException("File type not supported");
		}
	}

	protected InputStream getInputStream(Media media) {
		try {
			String stringData = media.getStringData();
			byte[] bytes = stringData.getBytes("UTF-8");
			ByteArrayInputStream is = new ByteArrayInputStream(bytes);
			return is;
		} catch(Exception e) {
			log.error(e);
			throw new RuntimeException(e);
		}
	}
	
	protected void processSurveyImport(String surveyName, boolean overwrite) {
		uploadedSurvey.setName(surveyName);
		if ( overwrite ) {
			Integer id = getSurveyWorkId(surveyName);
			uploadedSurvey.setId(id);
		}
		try {
			surveyManager.saveSurveyWork(uploadedSurvey);
			BindUtils.postGlobalCommand(null, null, SurveySelectVM.CLOSE_SURVEY_IMPORT_POP_UP_GLOBAL_COMMNAD, null);
			Object[] args = new String[]{surveyName};
			MessageUtil.showInfo("survey.import_survey.successfully_imported", args);
		} catch (SurveyImportException e) {
			log.error(e);
			Object[] args = new String[]{e.getMessage()};
			MessageUtil.showError("survey.import_survey.error", args);
		}
	}
	
	protected boolean existsSurveyWithSameUriButDifferentName(String surveyName, String uri) {
		SurveySummary collidingSurvey = getSurveyByURI(uri);
		return  collidingSurvey != null && ! collidingSurvey.getName().equals(surveyName);
	}

	protected CollectSurvey unmarshalSurvey(InputStream is) {
		CollectSurvey survey = null;
		try {
			survey = surveyManager.unmarshalSurvey(is);
		} catch(Exception e) {
			log.error(e);
			Object[] args = new String[]{e.getMessage()};
			MessageUtil.showError("survey.import_survey.error", args);
		}
		return survey;
	}

	protected boolean existsSurveyWithName(String name) {
		List<SurveySummary> summaries = surveyManager.getSurveyWorkSummaries();
		for (SurveySummary summary : summaries) {
			String summaryName = summary.getName();
			if ( summaryName.equals(name) ) {
				return true;
			}
		}
		return false;
	}
	
	protected Integer getSurveyWorkId(String name) {
		List<SurveySummary> summaries = surveyManager.getSurveyWorkSummaries();
		for (SurveySummary summary : summaries) {
			String summaryName = summary.getName();
			if ( summaryName.equals(name) ) {
				return summary.getId();
			}
		}
		return null;
	}
	
	protected SurveySummary getSurveyByURI(String uri) {
		List<SurveySummary> summaries = surveyManager.getSurveyWorkSummaries();
		for (SurveySummary summary : summaries) {
			String summaryUri = summary.getUri();
			if ( summaryUri.equals(uri) ) {
				return summary;
			}
		}
		return null;
	}
	
	public String getSurveyName() {
		return surveyName;
	}

	public void setSurveyName(String surveyName) {
		this.surveyName = surveyName;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public CollectSurvey getUploadedSurvey() {
		return uploadedSurvey;
	}
	
}
