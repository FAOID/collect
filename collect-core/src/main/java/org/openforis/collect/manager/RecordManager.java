/**
 * 
 */
package org.openforis.collect.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import javax.xml.namespace.QName;

import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.State;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.FieldSymbol;
import org.openforis.collect.model.RecordSummarySortField;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.MissingRecordKeyException;
import org.openforis.collect.persistence.MultipleEditException;
import org.openforis.collect.persistence.RecordDao;
import org.openforis.collect.persistence.RecordLockedException;
import org.openforis.collect.persistence.RecordPersistenceException;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.metamodel.validation.ValidationResult;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodePointer;
import org.openforis.idm.model.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author M. Togna
 * @author S. Ricci
 */
public class RecordManager {
	//private final Log log = LogFactory.getLog(RecordManager.class);
	
	private static final QName COUNT_ANNOTATION = new QName("http://www.openforis.org/collect/3.0/collect", "count");

	@Autowired
	private RecordDao recordDao;
	
	protected void init() {
		unlockAll();
	}
	
	@Transactional
	public void save(CollectRecord record) throws RecordPersistenceException {
		updateKeys(record);
		
		updateCounts(record);
		
		Integer id = record.getId();
		if(id == null) {
			recordDao.insert(record);
			User user = record.getModifiedBy();
			id = record.getId();
			recordDao.lock(id, user);
		} else {
			recordDao.update(record);
		}
	}

	@Transactional
	public void delete(int recordId, User user) throws RecordPersistenceException {
		recordDao.lock(recordId, user);
		recordDao.delete(recordId);
	}

	/**
	 * Returns a record and lock it
	 * 
	 * @param survey
	 * @param user
	 * @param recordId
	 * @return
	 * @throws MultipleEditException 
	 */
	@Transactional
	public CollectRecord checkout(CollectSurvey survey, User user, int recordId, int step) throws RecordPersistenceException {
		CollectRecord record = recordDao.load(survey, recordId, step);
		recordDao.lock(recordId, user);
		record.setLockedBy(user);
		return record;
	}

	@Transactional
	public List<CollectRecord> getSummaries(CollectSurvey survey, String rootEntity, String... keys) {
		return recordDao.loadSummaries(survey, rootEntity, keys);
	}
	
	@Transactional
	public List<CollectRecord> getSummaries(CollectSurvey survey, String rootEntity, int offset, int maxNumberOfRecords, List<RecordSummarySortField> sortFields, String filter) {
		List<CollectRecord> recordsSummary = recordDao.loadSummaries(survey, rootEntity, offset, maxNumberOfRecords, sortFields, filter);
		return recordsSummary;
	}

	@Transactional
	public int getCountRecords(CollectSurvey survey, String rootEntity) {
		Schema schema = survey.getSchema();
		EntityDefinition rootEntityDefinition = schema.getRootEntityDefinition(rootEntity);
		int count = recordDao.countRecords(rootEntityDefinition.getId());
		return count;
	}

	@Transactional
	public CollectRecord create(CollectSurvey survey, EntityDefinition rootEntityDefinition, User user, String modelVersionName) throws RecordPersistenceException {
		recordDao.checkLock(user);
		
		CollectRecord record = new CollectRecord(survey, modelVersionName);
		record.createRootEntity(rootEntityDefinition.getName());
		
		record.setCreationDate(new Date());
		record.setCreatedBy(user);
		record.setStep(Step.ENTRY);
		return record;
	}

	@Transactional
	public void unlock(CollectRecord record, User user) throws RecordLockedException {
		recordDao.unlock(record.getId(), user);
		record.setLockedBy(null);
	}

	@Transactional
	public void unlockAll() {
		recordDao.unlockAll();
	}

	@Transactional
	public void promote(CollectRecord record, User user) throws RecordPersistenceException, RecordPromoteException {
		//save changes on current step
		if(hasErrors(record)){
			throw new RecordPromoteException("Record cannot be promoted becuase it contains errors.");
		}
		
		Integer id = record.getId();
		if(id == null) {
			recordDao.insert(record);
		} else {
			recordDao.update(record);
		}
		//change step and update the record
		Step currentStep = record.getStep();
		Step nextStep = currentStep.getNext();
		Date now = new Date();
		record.setModifiedBy(user);
		record.setModifiedDate(now);
		record.setStep(nextStep);
		record.setState(null);
		recordDao.update(record);
	}

	private boolean hasErrors(CollectRecord record) {
		Entity rootEntity = record.getRootEntity();
		Stack<Node<?>> stack = new Stack<Node<?>>();
		stack.push(rootEntity);
		
		while(!stack.isEmpty()){
			Node<?> node = stack.pop();
			
			if(node instanceof Entity){
				Entity entity = (Entity) node;
				boolean cardinalityError = hasCardinalityError(entity);
				if(cardinalityError){
					return true;
				} else {
					List<Node<?>> children = entity.getChildren();
					for (Node<?> child : children) {
						stack.push(child);
					}
				}
			} else if(node instanceof Attribute){
				ValidationResults validationResults = ((Attribute<?,?>) node).validateValue();
				List<ValidationResult> errors = validationResults.getErrors();
				if(errors.size() > 0){
					return true;
				}
			}
		}
		return false;
	}

	private boolean hasCardinalityError(Entity entity) {
		EntityDefinition definition = entity.getDefinition();
		List<NodeDefinition> childDefinitions = definition.getChildDefinitions();
		for (NodeDefinition childDefinition : childDefinitions) {
			String childName = childDefinition.getName();
			ValidationResultFlag minCountFlag = entity.validateMinCount(childName );
			if(minCountFlag == ValidationResultFlag.ERROR){
				return true;
			}
			
			ValidationResultFlag maxCountFlag = entity.validateMaxCount(childName);
			if(maxCountFlag == ValidationResultFlag.ERROR){
				return true;
			}
		}
		return false;
	}

	@Transactional
	public void demote(CollectSurvey survey, int recordId, Step currentStep, User user) throws RecordPersistenceException {
		Step prevStep = currentStep.getPrevious();
		CollectRecord prevStepRecord = recordDao.load(survey, recordId, prevStep.getStepNumber());
		Date now = new Date();
		prevStepRecord.setModifiedBy(user);
		prevStepRecord.setModifiedDate(now);
		prevStepRecord.setStep(prevStep);
		prevStepRecord.setState(State.REJECTED);
		recordDao.update(prevStepRecord);
	}
	
	public Entity addEntity(Entity parentEntity, String nodeName) {
		Entity entity = parentEntity.addEntity(nodeName);
		addEmptyNodes(entity);
		return entity;
	}
	
	public void addEmptyNodes(Entity entity) {
		Record record = entity.getRecord();
		ModelVersion version = record.getVersion();
		
		addEmptyEnumeratedEntities(entity);
		EntityDefinition entityDefn = entity.getDefinition();
		List<NodeDefinition> childDefinitions = entityDefn.getChildDefinitions();
		for (NodeDefinition nodeDefn : childDefinitions) {
			if(version.isApplicable(nodeDefn)) {
				String name = nodeDefn.getName();
				if(entity.getCount(name) == 0) {
					if(nodeDefn instanceof AttributeDefinition) {
						Node<?> createNode = nodeDefn.createNode();
						entity.add(createNode);
					} else if(nodeDefn instanceof EntityDefinition && ! nodeDefn.isMultiple()) {
						addEntity(entity, name);
					}
				} else {
					List<Node<?>> all = entity.getAll(name);
					for (Node<?> node : all) {
						if(node instanceof Entity) {
							addEmptyNodes((Entity) node);
						}
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public <V> void setFieldValue(Attribute<?,?> attribute, Object value, String remarks, FieldSymbol symbol, int fieldIdx){
		if(fieldIdx < 0){
			fieldIdx = 0;
		}
		Field<V> field = (Field<V>) attribute.getField(fieldIdx);
		field.setValue((V)value);
		field.setRemarks(remarks);
		Character symbolChar = null;
		if (symbol != null) {
			symbolChar = symbol.getCode();
		}
		field.setSymbol(symbolChar);
	}
	
	@SuppressWarnings("unchecked")
	public <V> void setAttributeValue(Attribute<?,V> attribute, Object value, String remarks){
		attribute.setValue((V)value);
		Field<V> field = (Field<V>) attribute.getField(0);
		field.setRemarks(remarks);
		field.setSymbol(null);
	}
	
	public Set<Attribute<?, ?>> clearValidationResults(Attribute<?,?> attribute){
		Set<Attribute<?,?>> checkDependencies = attribute.getCheckDependencies();
		clearValidationResults(checkDependencies);
		return checkDependencies;
	}

	public void clearValidationResults(Set<Attribute<?, ?>> checkDependencies) {
		for (Attribute<?, ?> attr : checkDependencies) {
			attr.clearValidationResults();
		}
	}
	
	public Set<NodePointer> clearRelevanceRequiredStates(Node<?> node){
		Set<NodePointer> relevantDependencies = node.getRelevantDependencies();
		clearRelevantDependencies(relevantDependencies);
		Set<NodePointer> requiredDependencies = node.getRequiredDependencies();
		requiredDependencies.addAll(relevantDependencies);
		clearRequiredDependencies(requiredDependencies);
		return requiredDependencies;
	}
	
	public void clearRelevantDependencies(Set<NodePointer> nodePointers) {
		for (NodePointer nodePointer : nodePointers) {
			Entity entity = nodePointer.getEntity();
			entity.clearRelevanceState(nodePointer.getChildName());
		}
	}
	
	public void clearRequiredDependencies(Set<NodePointer> nodePointers) {
		for (NodePointer nodePointer : nodePointers) {
			Entity entity = nodePointer.getEntity();
			entity.clearRequiredState(nodePointer.getChildName());
		}
	}
	
	private void addEmptyEnumeratedEntities(Entity entity) {
		Record record = entity.getRecord();
		ModelVersion version = record.getVersion();
		EntityDefinition entityDefn = entity.getDefinition();
		List<NodeDefinition> childDefinitions = entityDefn.getChildDefinitions();
		for (NodeDefinition childDefn : childDefinitions) {
			if(childDefn instanceof EntityDefinition && version.isApplicable(childDefn)) {
				EntityDefinition childEntityDefn = (EntityDefinition) childDefn;
				if(childEntityDefn.isMultiple() && childEntityDefn.isEnumerable()) {
					addEmptyEnumeratedEntities(entity, childEntityDefn);
				}
			}
		}
	}

	private void addEmptyEnumeratedEntities(Entity entity, EntityDefinition enumeratedEntityDefn) {
		Record record = entity.getRecord();
		ModelVersion version = record.getVersion();
		CodeAttributeDefinition enumeratingCodeDefn = getEnumeratingKeyCodeAttribute(enumeratedEntityDefn, version);
		if(enumeratingCodeDefn != null) {
			CodeList list = enumeratingCodeDefn.getList();
			List<CodeListItem> items = list.getItems();
			for (CodeListItem item : items) {
				if(version.isApplicable(item)) {
					String code = item.getCode();
					if(! hasEnumeratedEntity(entity, enumeratedEntityDefn, enumeratingCodeDefn, code)) {
						Entity addedEntity = addEntity(entity, enumeratedEntityDefn.getName());
						//there will be an empty CodeAttribute after the adding of the new entity
						//set the value into this node
						CodeAttribute addedCode = (CodeAttribute) addedEntity.get(enumeratingCodeDefn.getName(), 0);
						addedCode.setValue(new Code(code));
					}
				}
			}
		}
	}

	private CodeAttributeDefinition getEnumeratingKeyCodeAttribute(EntityDefinition entity, ModelVersion version) {
		List<AttributeDefinition> keys = entity.getKeyAttributeDefinitions();
		for (AttributeDefinition key: keys) {
			if(key instanceof CodeAttributeDefinition && version.isApplicable(key)) {
				CodeAttributeDefinition codeDefn = (CodeAttributeDefinition) key;
				if(codeDefn.getList().getLookupTable() == null) {
					return codeDefn;
				}
			}
		}
		return null;
	}
	
	private boolean hasEnumeratedEntity(Entity parentEntity, EntityDefinition childEntityDefn, 
			CodeAttributeDefinition enumeratingCodeAttributeDef, String value) {
		List<Node<?>> children = parentEntity.getAll(childEntityDefn.getName());
		for (Node<?> node : children) {
			Entity child = (Entity) node;
			Code code = getCodeAttributeValue(child, enumeratingCodeAttributeDef);
			if(code != null && value.equals(code.getCode())) {
				return true;
			}
		}
		return false;
	}
	
	private Code getCodeAttributeValue(Entity entity, CodeAttributeDefinition def) {
		Node<?> node = entity.get(def.getName(), 0);
		if(node != null) {
			return ((CodeAttribute)node).getValue();
		} else {
			return null;
		}
	}
	
	/**
	 * Returns first level entity definitions of the passed root entity that have the attribute countInSummaryList set to true
	 * 
	 * @param rootEntityDefinition
	 * @return 
	 */
	private List<EntityDefinition> getCountableInList(EntityDefinition rootEntityDefinition) {
		List<EntityDefinition> result = new ArrayList<EntityDefinition>();
		List<NodeDefinition> childDefinitions = rootEntityDefinition.getChildDefinitions();
		for (NodeDefinition childDefinition : childDefinitions) {
			if(childDefinition instanceof EntityDefinition) {
				EntityDefinition entityDefinition = (EntityDefinition) childDefinition;
				String annotation = childDefinition.getAnnotation(COUNT_ANNOTATION);
				if(annotation != null && Boolean.parseBoolean(annotation)) {
					result.add(entityDefinition);
				}
			}
		}
		return result;
	}
	
	private void updateCounts(CollectRecord record) {
		Entity rootEntity = record.getRootEntity();
		EntityDefinition rootEntityDefn = rootEntity.getDefinition();
		List<EntityDefinition> countableDefns = getCountableInList(rootEntityDefn);
		
		//set counts
		List<Integer> counts = new ArrayList<Integer>();
		for (EntityDefinition defn : countableDefns) {
			String name = defn.getName();
			int count = rootEntity.getCount(name);
			counts.add(count);
		}
		record.setEntityCounts(counts);
	}
	
	private void updateKeys(CollectRecord record) throws RecordPersistenceException {
		record.updateRootEntityKeyValues();
		
		//check that all keys have been specified
		List<String> rootEntityKeyValues = record.getRootEntityKeyValues();
		Entity rootEntity = record.getRootEntity();
		EntityDefinition rootEntityDefn = rootEntity.getDefinition();
		List<AttributeDefinition> keyAttributeDefns = rootEntityDefn.getKeyAttributeDefinitions();

		if (keyAttributeDefns.size() != rootEntityKeyValues.size()) {
			throw new MissingRecordKeyException();
		}
	}

}
