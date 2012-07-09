package org.openforis.collect.remoting.service.dataImport;

import java.util.HashMap;
import java.util.Map;

import org.openforis.collect.remoting.service.dataProcessing.DataProcessingState;


/**
 * 
 * @author S. Ricci
 *
 */
public class DataImportState extends DataProcessingState {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum MainStep {
		INITED, SUMMARY_CREATION, IMPORT;
	}
	
	public enum SubStep {
		INITED, PREPARING, RUNNING, COMPLETE, CANCELLED, ERROR;
	}
	
	private MainStep mainStep;
	private SubStep subStep;
	private Map<String, String> errors;
	private Map<String, String> warnings;
	
	private int insertedCount;
	private int updatedCount;

	public DataImportState() {
		super();
		insertedCount = 0;
		updatedCount = 0;
		errors = new HashMap<String, String>();
		warnings = new HashMap<String, String>();
		mainStep = MainStep.SUMMARY_CREATION;
		subStep = SubStep.INITED;
	}

	public void addError(String fileName, String error) {
		errors.put(fileName, error);
	}

	public void addWarning(String fileName, String warning) {
		warnings.put(fileName, warning);
	}
	
	public void incrementInsertedCount() {
		insertedCount ++;
		incrementCount();
	}
	
	public void incrementUpdatedCount() {
		updatedCount ++;
		incrementCount();
	}

	public int getInsertedCount() {
		return insertedCount;
	}

	public int getUpdatedCount() {
		return updatedCount;
	}

	public Map<String, String> getErrors() {
		return errors;
	}

	public Map<String, String> getWarnings() {
		return warnings;
	}

	public MainStep getMainStep() {
		return mainStep;
	}

	public void setMainStep(MainStep mainStep) {
		this.mainStep = mainStep;
	}

	public SubStep getSubStep() {
		return subStep;
	}

	public void setSubStep(SubStep subStep) {
		this.subStep = subStep;
	}

}