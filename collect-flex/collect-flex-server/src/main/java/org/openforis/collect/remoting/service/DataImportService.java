package org.openforis.collect.remoting.service;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletContext;

import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SessionManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.UserManager;
import org.openforis.collect.model.User;
import org.openforis.collect.remoting.service.dataImport.DataImportProcess;
import org.openforis.collect.remoting.service.dataImport.DataImportState;
import org.openforis.collect.web.session.SessionState;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author S. Ricci
 *
 */
public class DataImportService {
	
	private static final String IMPORT_PATH = "import";
	
	private static final String FILE_NAME = "data_import.zip";
	
	@Autowired
	private SessionManager sessionManager;
	
	@Autowired
	private SurveyManager surveyManager;
	
	@Autowired
	private RecordManager recordManager;
	
	@Autowired
	private UserManager userManager;
	
	private File packagedFile;
	
	@Autowired 
	private ServletContext servletContext;
	
	private File importDirectory;
	
	private DataImportProcess dataImportProcess;

	
	public void init() {
		String importRealPath = servletContext.getRealPath(IMPORT_PATH);
		importDirectory = new File(importRealPath);
		if ( importDirectory.exists() ) {
			importDirectory.delete();
		}
		if ( ! importDirectory.mkdirs() && ! importDirectory.canRead() ) {
			throw new IllegalStateException("Cannot access import directory: " + importRealPath);
		}
	}
	
	public DataImportState initProcess(String surveyName, String rootEntityName) {
		SessionState sessionState = sessionManager.getSessionState();
		File userImportFolder = new File(importDirectory, sessionState.getSessionId());
		packagedFile = new File(userImportFolder, FILE_NAME);
		List<User> usersList = userManager.loadAll();
		HashMap<String, User> users = new HashMap<String, User>();
		for (User user : usersList) {
			users.put(user.getName(), user);
		}
		if ( dataImportProcess == null || dataImportProcess.isComplete() ) {
			dataImportProcess = new DataImportProcess(surveyManager, recordManager, users, surveyName, rootEntityName, packagedFile);
		}
		return null;
	}
}
