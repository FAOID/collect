/**
 * 
 */
package org.openforis.collect.designer.viewmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openforis.collect.designer.form.NodeDefinitionFormObject;
import org.openforis.collect.designer.model.LabelKeys;
import org.openforis.collect.metamodel.ui.UIOptions;
import org.openforis.collect.metamodel.ui.UITab;
import org.openforis.collect.metamodel.ui.UITabSet;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.Schema;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.Binder;
import org.zkoss.bind.Form;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.GlobalCommand;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.resource.Labels;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.Path;

/**
 * 
 * @author S. Ricci
 *
 */
public abstract class NodeDefinitionVM<T extends NodeDefinition> extends SurveyObjectBaseVM<T> {

	protected static final String FORM_CONTAINER_ID = "nodeFormContainer";
	
	protected Form tempFormObject;
	protected EntityDefinition parentEntity;

	@Init(superclass=false)
	public void init(EntityDefinition parentEntity, T nodeDefn, Boolean newItem) {
		super.init();
		if ( nodeDefn != null ) {
			this.parentEntity = parentEntity;
			this.newItem = newItem;
			setEditedItem(nodeDefn);
		}
	}

	@Override
	protected T createItemInstance() {
		return null;
	}

	@Override
	protected void addNewItemToSurvey() {
		//do nothing
	}
	
	@Override
	@GlobalCommand
	public void currentLanguageChanged() {
		super.currentLanguageChanged();
		setEditedItem(editedItem);
		notifyChange("tempFormObject","formObject");
	}
	
	@Override
	@Command
	public void applyChanges() {
		formObject.saveTo(editedItem, currentLanguageCode);
		if ( newItem ) {
			if ( parentEntity == null ) {
				Schema schema = editedItem.getSchema();
				schema.addRootEntityDefinition((EntityDefinition) editedItem);
			} else {
				parentEntity.addChildDefinition(editedItem);
			}
			newItem = false;
		}
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("parentEntity", parentEntity);
		BindUtils.postGlobalCommand(null, null, "editedNodeChanged", args);
	}
	
	@Override
	protected List<T> getItemsInternal() {
		return null;
	}

	@Override
	protected void moveSelectedItem(int indexTo) {
		//do nothing
	}

	@Override
	protected void deleteItemFromSurvey(T item) {
		//do nothing
	}
	
	@GlobalCommand
	public void tabSetChanged(@ContextParam(ContextType.BINDER) Binder binder, @BindingParam("tabSet") UITabSet tabSet) {
		notifyChange("assignableTabs");
		if ( isEditingItem() ) {
			dispatchApplyChangesCommand(binder);
		}
	}
	
	@GlobalCommand
	public void closeVersioningManagerPopUp(@ContextParam(ContextType.BINDER) Binder binder) {
		validateForm(binder);
	}
	
	protected void validateForm(@ContextParam(ContextType.BINDER) Binder binder) {
		Component view = binder.getView();
		IdSpace currentIdSpace = view.getSpaceOwner();
		Component formComponent = Path.getComponent(currentIdSpace, FORM_CONTAINER_ID);
		Binder formComponentBinder = (Binder) formComponent.getAttribute("binder");
		formComponentBinder.postCommand("applyChanges", null);
	}
	
	// GETTERS AND SETTERS
	public Form getTempFormObject() {
		return tempFormObject;
	}
	
	@Deprecated
	public List<Object> getAssignableTabs() {
		if ( editedItem == null ) {
			return null;
		} else {
			CollectSurvey survey = getSurvey();
			UIOptions uiOptions = survey.getUIOptions();
			List<Object> result = new ArrayList<Object>();
			result.add(NodeDefinitionFormObject.INHERIT_TAB);
			result.addAll(uiOptions.getAssignableTabs(editedItem));
			return result;
		}
	}
	
	public List<String> getAssignableTabNames() {
		if ( editedItem == null ) {
			return null;
		} else {
			CollectSurvey survey = getSurvey();
			UIOptions uiOptions = survey.getUIOptions();
			List<UITab> assignableTabs;
			if ( parentEntity != null ) {
				assignableTabs = uiOptions.getTabsAssignableToChildren(parentEntity);
			} else {
				//edited item is root entity
				assignableTabs = uiOptions.getTabsAssignableToChildren((EntityDefinition) editedItem);
			}
			List<String> result = new ArrayList<String>();
			result.add(NodeDefinitionFormObject.INHERIT_TAB_NAME);
			for (UITab uiTab : assignableTabs) {
				result.add(uiTab.getName());
			}
			return result;
		}
	}
	
	public String getTabLabel(String tabName) {
		if ( tabName == null || tabName.equals(NodeDefinitionFormObject.INHERIT_TAB_NAME) ) {
			return Labels.getLabel(LabelKeys.INHERIT_TAB);
			//return NodeDefinitionFormObject.INHERIT_TAB.getName();
		} else {
			UITab tab = getTab(tabName);
			if ( tab != null ) {
				String result = tab.getLabel(currentLanguageCode);
				if ( result == null && isDefaultLanguage() ) {
					//try to get label associated to default language code
					result = tab.getLabel(null);
				}
				return result;
			} else {
				return null;
			}
		}
	}

	protected UITab getTab(String tabName) {
		UITabSet parentTabSet = getParentTabSet();
		UITab tab = parentTabSet != null ? parentTabSet.getTab(tabName): null;
		return tab;
	}
	
	protected UITabSet getParentTabSet() {
		CollectSurvey survey = getSurvey();
		UIOptions uiOptions = survey.getUIOptions();
		UITabSet result;
		if ( parentEntity == null ) {
			result = uiOptions.getAssignedRootTabSet((EntityDefinition) editedItem);
		} else {
			result = uiOptions.getAssignedTabSet(parentEntity);
		}
		return result;
	}
}
