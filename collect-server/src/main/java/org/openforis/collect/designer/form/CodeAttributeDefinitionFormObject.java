/**
 * 
 */
package org.openforis.collect.designer.form;

import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.EntityDefinition;

/**
 * @author S. Ricci
 *
 */
public class CodeAttributeDefinitionFormObject<T extends CodeAttributeDefinition> extends AttributeDefinitionFormObject<T> {
	
	private boolean key;
	private CodeList list;
	private String parentExpression;
	private boolean strict;
	
	CodeAttributeDefinitionFormObject(EntityDefinition parentDefn) {
		super(parentDefn);
		strict = true;
	}

	@Override
	public void saveTo(T dest, String languageCode) {
		super.saveTo(dest, languageCode);
		dest.setList(list);
		dest.setKey(key);
		dest.setAllowUnlisted(! strict);
		dest.setParentExpression(parentExpression);
	}
	
	@Override
	public void loadFrom(T source, String languageCode, String defaultLanguage) {
		super.loadFrom(source, languageCode, defaultLanguage);
		key = source.isKey();
		list = source.getList();
		parentExpression = source.getParentExpression();
		strict = ! source.isAllowUnlisted();
	}

	public boolean isKey() {
		return key;
	}

	public void setKey(boolean key) {
		this.key = key;
	}

	public CodeList getList() {
		return list;
	}

	public void setList(CodeList list) {
		this.list = list;
	}

	public boolean isStrict() {
		return strict;
	}

	public void setStrict(boolean strict) {
		this.strict = strict;
	}
	
	public String getParentExpression() {
		return parentExpression;
	}
	
	public void setParentExpression(String parentExpression) {
		this.parentExpression = parentExpression;
	}

}
