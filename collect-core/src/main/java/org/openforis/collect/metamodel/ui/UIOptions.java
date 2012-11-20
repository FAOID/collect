package org.openforis.collect.metamodel.ui;

import static org.openforis.collect.metamodel.ui.UIOptionsConstants.UI_NAMESPACE_URI;
import static org.openforis.collect.metamodel.ui.UIOptionsConstants.UI_TYPE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.idm.metamodel.ApplicationOptions;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.LanguageSpecificText;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.NodeDefinitionVisitor;
import org.openforis.idm.metamodel.NodeLabel;
import org.openforis.idm.metamodel.Schema;
import org.openforis.idm.util.CollectionUtil;


/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
public class UIOptions implements ApplicationOptions, Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final String TABSET_NAME_PREFIX = "tabset_";
	private static final String TAB_NAME_PREFIX = "tab_";

	public enum Annotation {
		TAB_SET(new QName(UI_NAMESPACE_URI, UIOptionsConstants.TAB_SET_NAME)),
		TAB_NAME(new QName(UI_NAMESPACE_URI, UIOptionsConstants.TAB)),
		LAYOUT(new QName(UI_NAMESPACE_URI, UIOptionsConstants.LAYOUT)),
		COUNT_IN_SUMMARY_LIST(new QName(UI_NAMESPACE_URI, UIOptionsConstants.COUNT)),
		SHOW_ROW_NUMBERS(new QName(UI_NAMESPACE_URI, UIOptionsConstants.SHOW_ROW_NUMBERS)),
		AUTOCOMPLETE(new QName(UI_NAMESPACE_URI, UIOptionsConstants.AUTOCOMPLETE));
		
		private QName qName;

		private Annotation(QName qname) {
			this.qName = qname;
		}
		
		public QName getQName() {
			return qName;
		}
	}
	
	public enum Layout {
		FORM, TABLE
	}
	
	private CollectSurvey survey;
	private List<UITabSet> tabSets;
	
	public UIOptions() {
	}
	
	public UIOptions(CollectSurvey survey) {
		this();
		this.survey = survey;
	}

	@Override
	public String getType() {
		return UI_TYPE;
	}

	public CollectSurvey getSurvey() {
		return survey;
	}
	
	public void setSurvey(CollectSurvey survey) {
		this.survey = survey;
	}
	
	public List<UITabSet> getTabSets() {
		return CollectionUtil.unmodifiableList(tabSets);
	}
	
	public UITabSet createTabSet() {
		return createTabSet(TABSET_NAME_PREFIX + survey.nextId());
	}
	
	public UITabSet createTabSet(String name) {
		UITabSet result = new UITabSet(this);
		result.setName(name);
		return result;
	}
	
	public UITab createTab() {
		return createTab(TAB_NAME_PREFIX + survey.nextId());
	}

	public UITab createTab(String name) {
		UITab result = new UITab(this);
		result.setName(name);
		return result;
	}
	
	public UITabSet createRootTabSet(EntityDefinition rootEntity) {
		UIOptions uiOpts = survey.getUIOptions();
		UITabSet tabSet = uiOpts.createTabSet();
		UITab mainTab = createMainTab(rootEntity, tabSet);
		uiOpts.addTabSet(tabSet);
		uiOpts.assignToTabSet(rootEntity, tabSet);
		uiOpts.assignToTab(rootEntity, mainTab);
		return tabSet;
	}

	protected UITab createMainTab(EntityDefinition nodeDefn,
			UITabSet tabSet) {
		UITab tab = createTab();
		copyLabels(nodeDefn, tab);
		tabSet.addTab(tab);
		return tab;
	}

	protected void copyLabels(EntityDefinition nodeDefn, UITab tab) {
		removeLabels(tab);
		List<NodeLabel> labels = nodeDefn.getLabels();
		for (NodeLabel label : labels) {
			if ( label.getType() == NodeLabel.Type.INSTANCE ) {
				tab.setLabel(label.getLanguage(), label.getText());
			}
		}
	}

	protected void removeLabels(UITab tab) {
		List<LanguageSpecificText> labels = tab.getLabels();
		for (LanguageSpecificText label : labels) {
			tab.removeLabel(label.getLanguage());
		}
	}
	
	public UITab getMainTab(UITabSet rootTabSet) {
		List<UITab> tabs = rootTabSet.getTabs();
		if ( tabs.isEmpty() ) {
			return null;
		} else {
			return tabs.get(0);
		}
	}
	
	public boolean isMainTab(UITab tab) {
		return tab.getIndex() == 0 && tab.getDepth() == 1;
	}
	
	public UITab getAssignedTab(NodeDefinition nodeDefn) {
		return getAssignedTab(nodeDefn, true);
	}
	
	public UITab getAssignedTab(NodeDefinition nodeDefn, boolean includeInherited) {
		EntityDefinition parentDefn = (EntityDefinition) nodeDefn.getParentDefinition();
		return getAssignedTab(parentDefn, nodeDefn, includeInherited);
	}
	
	public UITab getAssignedTab(EntityDefinition parentDefn, NodeDefinition nodeDefn, boolean includeInherited) {
		UITab result = null;
		UITabSet rootTabSet = getAssignedRootTabSet(parentDefn, nodeDefn);
		if ( rootTabSet != null ) {
			String tabName = nodeDefn.getAnnotation(Annotation.TAB_NAME.getQName());
			if ( StringUtils.isNotBlank(tabName) && ( parentDefn == null || parentDefn.getParentDefinition() == null ) ) {
				result = rootTabSet.getTab(tabName);
			} else if ( parentDefn != null ) {
				UITab parentTab = getAssignedTab(parentDefn);
				if ( parentTab != null && StringUtils.isNotBlank(tabName) ) {
					result = parentTab.getTab(tabName);
				} else if ( includeInherited ) {
					result = parentTab;
				}
			}
		}
		return result;
	}

	protected UITabSet getAssignedRootTabSet(EntityDefinition parentDefn, NodeDefinition nodeDefn) {
		EntityDefinition rootEntityDefn;
		if ( parentDefn != null ) {
			rootEntityDefn = parentDefn.getRootEntity();
		} else if ( nodeDefn instanceof EntityDefinition ) {
			rootEntityDefn = (EntityDefinition) nodeDefn;
		} else {
			throw new IllegalArgumentException("Parent entity definition not specified");
		}
		UITabSet tabSet = getAssignedRootTabSet(rootEntityDefn);
		return tabSet;
	}

	public UITabSet getAssignedRootTabSet(EntityDefinition rootEntityDefn) {
		String tabSetName = rootEntityDefn.getAnnotation(Annotation.TAB_SET.getQName());
		UITabSet tabSet = getTabSet(tabSetName);
		return tabSet;
	}
	
	public List<UITab> getAssignableTabs(NodeDefinition nodeDefn) {
		EntityDefinition parentEntity = (EntityDefinition) nodeDefn.getParentDefinition();
		if ( parentEntity == null ) {
			UITabSet rootTabSet = getAssignedRootTabSet((EntityDefinition) nodeDefn);
			if ( rootTabSet != null ) {
				return rootTabSet.getTabs();
			} else {
				return Collections.emptyList();
			}
		} else {
			return getTabsAssignableToChildren(parentEntity);
		}
	}
	
	public UITabSet getAssignedTabSet(EntityDefinition entityDefn) {
		if ( entityDefn.getParentDefinition() == null ) {
			return getAssignedRootTabSet(entityDefn);
		} else {
			return getAssignedTab(entityDefn);
		}
	}

	public List<UITab> getTabsAssignableToChildren(EntityDefinition entityDefn) {
		EntityDefinition rootEntity = entityDefn.getRootEntity();
		UITabSet rootTabSet = getAssignedRootTabSet(rootEntity);
		UITabSet parentTabSet = null;
		if ( rootTabSet != null ) {
			if ( entityDefn == null || entityDefn.getParentDefinition() == null ) {
				parentTabSet = rootTabSet;
			} else {
				UITab assignedTab = getAssignedTab(entityDefn);
				parentTabSet = assignedTab;
			}
		}
		if (parentTabSet == null) {
			return Collections.emptyList();
		} else {
			return parentTabSet.getTabs();
		}
	}
	
	public boolean isAssignableTo(NodeDefinition nodeDefn, UITab tab) {
		EntityDefinition parentEntityDefn = (EntityDefinition) nodeDefn.getParentDefinition();
		List<UITab> allowedTabs = getTabsAssignableToChildren(parentEntityDefn);
		boolean result = allowedTabs.contains(tab);
		return result;
	}

	public List<NodeDefinition> getNodesPerTab(UITab tab, boolean includeDescendants) {
		List<NodeDefinition> result = new ArrayList<NodeDefinition>();
		UITabSet tabSet = tab.getRootTabSet();
		EntityDefinition rootEntity = getRootEntityDefinition(tabSet);
		Queue<NodeDefinition> queue = new LinkedList<NodeDefinition>();
		queue.addAll(rootEntity.getChildDefinitions());
		while ( ! queue.isEmpty() ) {
			NodeDefinition defn = queue.remove();
			UITab nodeTab = getAssignedTab(defn);
			boolean nodeInTab = false;
			if ( nodeTab != null && nodeTab.equals(tab) ) {
				result.add(defn);
				nodeInTab = true;
			}
			if ( defn instanceof EntityDefinition && (includeDescendants || !nodeInTab) ) {
				queue.addAll(((EntityDefinition) defn).getChildDefinitions());
			}
		}
		return result;
	}

	public void assignToTabSet(EntityDefinition rootEntity, UITabSet tabSet) {
		String name = tabSet.getName();
		rootEntity.setAnnotation(Annotation.TAB_SET.getQName(), name);
	}

	public void assignToTab(NodeDefinition nodeDefn, UITab tab) {
		String tabName = tab.getName();
		nodeDefn.setAnnotation(Annotation.TAB_NAME.getQName(), tabName);
		
		afterTabAssociationChanged(nodeDefn);
	}

	protected void afterTabAssociationChanged(NodeDefinition nodeDefn) {
		if ( nodeDefn instanceof EntityDefinition ) {
			removeInvalidTabAssociationInDescendants((EntityDefinition) nodeDefn);
		}
	}

	protected void removeInvalidTabAssociationInDescendants(EntityDefinition nodeDefn) {
		((EntityDefinition) nodeDefn).traverse(new NodeDefinitionVisitor() {
			@Override
			public void visit(NodeDefinition descendantDefn) {
				UITab descendantTab = getAssignedTab(descendantDefn, false);
				if ( descendantTab == null ) {
					//tab not set or not found
					performTabAssociationRemoval(descendantDefn);
				}
			}
		});
	}
	
	public void removeTabAssociation(NodeDefinition nodeDefn) {
		performTabAssociationRemoval(nodeDefn);
		
		afterTabAssociationChanged(nodeDefn);
	}

	protected void performTabAssociationRemoval(NodeDefinition nodeDefn) {
		nodeDefn.setAnnotation(Annotation.TAB_NAME.getQName(), null);
	}
	
	public void removeTabAssociation(UITab tab) {
		Stack<UITab> stack = new Stack<UITab>();
		stack.push(tab);
		while ( ! stack.isEmpty() ) {
			UITab currentTab = stack.pop();
			List<NodeDefinition> nodesPerTab = getNodesPerTab(currentTab, true);
			for (NodeDefinition nodeDefn : nodesPerTab) {
				performTabAssociationRemoval(nodeDefn);
			}
			List<UITab> childTabs = currentTab.getTabs();
			stack.addAll(childTabs);
		}
	}

	public boolean isAssociatedToTab(NodeDefinition nodeDefn) {
		UITab tab = getAssignedTab(nodeDefn);
		return tab != null;
	}
	
	public Layout getLayout(EntityDefinition node) {
		String layoutValue = node.getAnnotation(Annotation.LAYOUT.getQName());
		if ( layoutValue == null ) {
			EntityDefinition parent = (EntityDefinition) node.getParentDefinition();
			if ( parent != null ) {
				if ( node.isMultiple()) {
					return Layout.TABLE;
				} else {
					return getLayout(parent);
				}
			} else {
				return Layout.FORM;
			}
		}
		return layoutValue != null ? Layout.valueOf(layoutValue.toUpperCase()): null;
	}

	public void setLayout(EntityDefinition entityDefn, Layout layout) {
		String layoutValue = layout != null ? layout.name().toLowerCase(): null;
		entityDefn.setAnnotation(Annotation.LAYOUT.getQName(), layoutValue);
	}
	
	/**
	 * Supported layouts are:
	 * - Root entity: FORM
	 * - Single entity: FORM and TABLE (only inside an entity with TABLE layout)
	 * - Multiple entity: FORM only if it's the only multiple entity with FORM layout
	 * 		into the tab
	 * 
	 * @param parentEntityDefn
	 * @param entityDefn
	 * @param layout
	 * @return 
	 */
	public boolean isLayoutSupported(EntityDefinition parentEntityDefn, int entityDefnId, UITab associatedTab, boolean multiple, Layout layout) {
		if ( parentEntityDefn == null ) {
			return layout == Layout.FORM;
		} else if ( ! multiple ) {
			Layout parentLayout = getLayout(parentEntityDefn);
			return layout == Layout.FORM || parentLayout == Layout.TABLE;
		} else if ( layout == Layout.FORM) {
			//UITab tab = getAssignedTab(parentEntityDefn, entityDefn, true);
			EntityDefinition multipleEntity = getFormLayoutMultipleEntity(associatedTab);
			return multipleEntity == null || multipleEntity.getId() == entityDefnId;
		} else {
			return true;
		}
	}

	protected EntityDefinition getFormLayoutMultipleEntity(UITab tab) {
		List<NodeDefinition> nodesPerTab = getNodesPerTab(tab, false);
		for (NodeDefinition nodeDefn : nodesPerTab) {
			if ( nodeDefn instanceof EntityDefinition && nodeDefn.isMultiple() ) {
				Layout nodeLayout = getLayout((EntityDefinition) nodeDefn);
				if ( nodeLayout == Layout.FORM ) {
					return (EntityDefinition) nodeDefn;
				}
			}
		}
		return null;
	}

	public EntityDefinition getRootEntityDefinition(UITabSet tabSet) {
		Schema schema = survey.getSchema();
		List<EntityDefinition> rootEntityDefinitions = schema.getRootEntityDefinitions();
		for (EntityDefinition defn : rootEntityDefinitions) {
			UITabSet entityTabSet = getAssignedRootTabSet(defn);
			if ( entityTabSet != null && entityTabSet.equals(tabSet) ) {
				return defn;
			}
		}
		return null;
	}
	
	public UITabSet getTabSet(String name) {
		if ( tabSets != null ) {
			for (UITabSet tabSet : tabSets) {
				if ( tabSet.getName().equals(name) ) {
					return tabSet;
				}
			}
		}
		return null;
	}
	
	public void addTabSet(UITabSet tabSet) {
		if ( tabSets == null ) {
			tabSets = new ArrayList<UITabSet>();
		}
		tabSets.add(tabSet);
	}
	
	public void setTabSet(int index, UITabSet tabSet) {
		if ( tabSets == null ) {
			tabSets = new ArrayList<UITabSet>();
		}
		tabSets.set(index, tabSet);
	}
	
	public void removeTabSet(UITabSet tabSet) {
		tabSets.remove(tabSet);
	}
	
	public UITabSet updateTabSet(String name, String newName) {
		UITabSet tabSet = getTabSet(name);
		tabSet.setName(newName);
		return tabSet;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((tabSets == null) ? 0 : tabSets.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UIOptions other = (UIOptions) obj;
		if (tabSets == null) {
			if (other.tabSets != null)
				return false;
		} else if (!tabSets.equals(other.tabSets))
			return false;
		return true;
	}

}
