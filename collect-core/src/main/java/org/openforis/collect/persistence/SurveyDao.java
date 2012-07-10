package org.openforis.collect.persistence;

import static org.openforis.collect.persistence.jooq.Sequences.OFC_SURVEY_ID_SEQ;
import static org.openforis.collect.persistence.jooq.tables.OfcRecord.OFC_RECORD;
import static org.openforis.collect.persistence.jooq.tables.OfcSurvey.OFC_SURVEY;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.impl.Factory;
import org.jooq.impl.SQLDataType;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.persistence.jooq.JooqDaoSupport;
import org.openforis.collect.persistence.xml.CollectIdmlBindingContext;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ExternalCodeListProvider;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NumberAttributeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.metamodel.xml.InvalidIdmlException;
import org.openforis.idm.metamodel.xml.SurveyMarshaller;
import org.openforis.idm.metamodel.xml.SurveyUnmarshaller;
import org.openforis.idm.model.RealAttribute;
import org.openforis.idm.model.Value;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author G. Miceli
 * @author M. Togna
 */
@Transactional
public class SurveyDao extends JooqDaoSupport {
	// private final Log LOG = LogFactory.getLog(SurveyDao.class);

	private CollectIdmlBindingContext bindingContext;

	@Autowired
	private ExpressionFactory expressionFactory;
	@Autowired
	private Validator validator;
	@Autowired
	private ExternalCodeListProvider externalCodeListProvider;

	public SurveyDao() {
	}

	public void init() {
		bindingContext = new CollectIdmlBindingContext(
				new CollectSurveyContext(expressionFactory, validator,
						externalCodeListProvider));
	}

	@Transactional
	public void importModel(Survey survey) throws SurveyImportException {
		String name = survey.getName();
		if (StringUtils.isBlank(name)) {
			throw new SurveyImportException(
					"Survey name must be set before importing");
		}

		String idml = marshalSurvey(survey);

		// Insert into OFC_SURVEY table
		Factory jf = getJooqFactory();
		int surveyId = jf.nextval(OFC_SURVEY_ID_SEQ).intValue();
		jf.insertInto(OFC_SURVEY).set(OFC_SURVEY.ID, surveyId)				
				.set(OFC_SURVEY.NAME, survey.getName())
				.set(OFC_SURVEY.URI, survey.getUri())
				.set(OFC_SURVEY.IDML, Factory.val(idml, SQLDataType.CLOB))
				.execute();

		survey.setId(surveyId);
	}

	public Survey load(int id) {
		Factory jf = getJooqFactory();
		Record record = jf.select().from(OFC_SURVEY)
				.where(OFC_SURVEY.ID.equal(id)).fetchOne();
		Survey survey = processSurveyRow(record);
		return survey;
	}

	public CollectSurvey load(String name) {
		Factory jf = getJooqFactory();
		Record record = jf.select().from(OFC_SURVEY)
				.where(OFC_SURVEY.NAME.equal(name)).fetchOne();
		CollectSurvey survey = processSurveyRow(record);
		return survey;
	}

	public CollectSurvey loadByUri(String uri) {
		Factory jf = getJooqFactory();
		Record record = jf.select().from(OFC_SURVEY)
				.where(OFC_SURVEY.URI.equal(uri)).fetchOne();
		CollectSurvey survey = processSurveyRow(record);
		return survey;
	}
	
	@Transactional
	public List<CollectSurvey> loadAll() {
		Factory jf = getJooqFactory();
		List<CollectSurvey> surveys = new ArrayList<CollectSurvey>();
		Result<Record> results = jf.select().from(OFC_SURVEY).fetch();
		for (Record row : results) {
			CollectSurvey survey = processSurveyRow(row);
			if (survey != null) {
				//loadNodeDefinitions(survey);
				surveys.add(survey);
			}
		}
		return surveys;
	}

	private CollectSurvey processSurveyRow(Record row) {
		try {
			if (row == null) {
				return null;
			}
			String idml = row.getValueAsString(OFC_SURVEY.IDML);
			CollectSurvey survey = (CollectSurvey) unmarshalIdml(idml);
			survey.setId(row.getValueAsInteger(OFC_SURVEY.ID));
			survey.setName(row.getValue(OFC_SURVEY.NAME));
			return survey;
		} catch (IOException e) {
			throw new RuntimeException(
					"Error deserializing IDML from database", e);
		}
	}

	public CollectSurvey unmarshalIdml(String idml) throws IOException {
		byte[] bytes = idml.getBytes("UTF-8");
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		return unmarshalIdml(is);
	}
	
	public CollectSurvey unmarshalIdml(InputStream is) throws IOException {
		SurveyUnmarshaller su = bindingContext.createSurveyUnmarshaller();
		CollectSurvey survey;
		try {
			survey = (CollectSurvey) su.unmarshal(is);
		} catch (InvalidIdmlException e) {
			throw new DataInconsistencyException("Invalid idm");
		}
		return survey;
	}

	public void validateAgainstSchema(byte[] idml) throws InvalidIdmlException {
		SurveyUnmarshaller su = bindingContext.createSurveyUnmarshaller();
		su.validateAgainstSchema(idml);
	}
	
	public String marshalSurvey(Survey survey) throws SurveyImportException {
		try {
			// Serialize Survey to XML
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			marshalSurvey(survey, os);
			return os.toString("UTF-8");
		} catch (IOException e) {
			throw new SurveyImportException("Error marshalling survey", e);
		}
	}
	
	public void marshalSurvey(Survey survey, OutputStream os) throws SurveyImportException {
		try {
			SurveyMarshaller sm = bindingContext.createSurveyMarshaller();
			sm.setIndent(true);
			sm.marshal(survey, os);
		} catch (IOException e) {
			throw new SurveyImportException("Error marshalling survey", e);
		}
	}

	public void clearModel() {
		Factory jf = getJooqFactory();
		jf.delete(OFC_RECORD).execute();
		jf.delete(OFC_SURVEY).execute();
	}

	public CollectIdmlBindingContext getBindingContext() {
		return bindingContext;
	}

	public void updateModel(CollectSurvey newSurvey) throws SurveyImportException {
		String name = newSurvey.getName();
		if (StringUtils.isBlank(name)) {
			throw new SurveyImportException(
					"Survey name must be set before importing");
		}

		String idml = marshalSurvey(newSurvey);

		// Get OFC_SURVEY table id for name
		Factory jf = getJooqFactory();
		int surveyId = 0;
		SelectConditionStep query = jf.select(OFC_SURVEY.ID).from(OFC_SURVEY)
				.where(OFC_SURVEY.NAME.equal(name));
		query.execute();
		Result<Record> result = query.getResult();

		System.out.println("Checking survey");
		if (result.isEmpty()) { // we should insert it now			
			surveyId = jf.nextval(OFC_SURVEY_ID_SEQ).intValue();
			System.out.println("    Survey " +  name + " not exist. Inserting with ID = " + surveyId );
			jf.insertInto(OFC_SURVEY).set(OFC_SURVEY.ID, surveyId)
					.set(OFC_SURVEY.NAME, newSurvey.getName())
					.set(OFC_SURVEY.URI, newSurvey.getUri())
					.set(OFC_SURVEY.IDML, Factory.val(idml, SQLDataType.CLOB))
					.execute();
			newSurvey.setId(surveyId);
		} else {
			
			//load old-first survey
			List<CollectSurvey> listOldSurvey = loadAll();
			CollectSurvey oldSurvey;
			Schema oldSchema;
			Collection<NodeDefinition> oldDefinitions;			
			if(listOldSurvey.size()==1){
				oldSurvey = listOldSurvey.get(0);
				oldSchema = oldSurvey.getSchema();
				oldDefinitions = oldSchema.getAllDefinitions();
			}else if(listOldSurvey.size()>1){
				throw new SurveyImportException("Multiple survey not supported yet");
			}else{
				throw new SurveyImportException("No existing survey in the database");
			}
			
			// validate things
			Schema newSchema = newSurvey.getSchema();
			Collection<NodeDefinition> definitions = newSchema.getAllDefinitions();
			System.out.println("Enumerating all nodeDefinition.");
			for (NodeDefinition newDefinition : definitions) {
				String path = newDefinition.getPath();				
				NodeDefinition oldDefinition = oldSchema.getById(newDefinition.getId());				
				if(!newDefinition.getClass().equals(oldDefinition.getClass()))
				{
					throw new SurveyImportException("Can not change node type '" + newDefinition.getName() + "' from " + oldDefinition.getClass() + " to " + newDefinition.getClass());
				}else{					
					if(newDefinition instanceof NumberAttributeDefinition){						
						NumberAttributeDefinition newNum = (NumberAttributeDefinition) newDefinition;
						Class<? extends Value> newSubType = newNum.getValueType();
						
						NumberAttributeDefinition oldNum = (NumberAttributeDefinition) oldDefinition;
						Class<? extends Value> oldSubType = oldNum.getValueType();
						
						if(!oldSubType.equals(newSubType)){
							throw new SurveyImportException("[SUBTYPE] Can not change node type '" + newDefinition.getName() + "' from " + oldDefinition.getClass() + " to " + newDefinition.getClass());
						}
					}
				}
			}

						
			Record record = result.get(0);			
			surveyId = record.getValueAsInteger(OFC_SURVEY.ID);			
			newSurvey.setId(surveyId);
			System.out.println("    Survey " +  name + " exist. Updating with ID = " + surveyId );
			jf.update(OFC_SURVEY)
					.set(OFC_SURVEY.IDML, Factory.val(idml, SQLDataType.CLOB))
					.set(OFC_SURVEY.NAME, newSurvey.getName())
					.set(OFC_SURVEY.URI, newSurvey.getUri())
					.where(OFC_SURVEY.ID.equal(newSurvey.getId())).execute();
		}

	}
}
