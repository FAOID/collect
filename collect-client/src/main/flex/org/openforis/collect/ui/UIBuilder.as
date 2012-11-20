package org.openforis.collect.ui {
	import mx.binding.utils.BindingUtils;
	import mx.collections.ArrayCollection;
	import mx.collections.ArrayList;
	import mx.collections.IList;
	import mx.collections.ListCollectionView;
	import mx.core.ClassFactory;
	import mx.core.IFactory;
	import mx.core.IVisualElement;
	
	import org.openforis.collect.i18n.Message;
	import org.openforis.collect.metamodel.proxy.AttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.BooleanAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CodeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.CoordinateAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.DateAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.EntityDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.FileAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.ModelVersionProxy;
	import org.openforis.collect.metamodel.proxy.NodeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.NumberAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.NumberAttributeDefinitionProxy$Type;
	import org.openforis.collect.metamodel.proxy.RangeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.SurveyProxy;
	import org.openforis.collect.metamodel.proxy.TaxonAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.TextAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.TextAttributeDefinitionProxy$Type;
	import org.openforis.collect.metamodel.proxy.TimeAttributeDefinitionProxy;
	import org.openforis.collect.metamodel.proxy.UIOptionsProxy;
	import org.openforis.collect.metamodel.proxy.UITabProxy;
	import org.openforis.collect.metamodel.proxy.UITabSetProxy;
	import org.openforis.collect.metamodel.proxy.UnitProxy;
	import org.openforis.collect.model.proxy.EntityProxy;
	import org.openforis.collect.ui.component.datagrid.CompleteColumnItemRenderer;
	import org.openforis.collect.ui.component.datagrid.RecordSummaryDataGrid;
	import org.openforis.collect.ui.component.datagrid.RecordSummaryErrorsColumnItemRenderer;
	import org.openforis.collect.ui.component.datagroup.DataGridHeaderRenderer;
	import org.openforis.collect.ui.component.detail.AttributeFormItem;
	import org.openforis.collect.ui.component.detail.AttributeItemRenderer;
	import org.openforis.collect.ui.component.detail.CodeAttributeFormItem;
	import org.openforis.collect.ui.component.detail.CompositeAttributeFormItem;
	import org.openforis.collect.ui.component.detail.EntityFormContainer;
	import org.openforis.collect.ui.component.detail.EntityFormItem;
	import org.openforis.collect.ui.component.detail.FormContainer;
	import org.openforis.collect.ui.component.detail.MultipleAttributeDataGroupFormItem;
	import org.openforis.collect.ui.component.detail.MultipleAttributeFormItem;
	import org.openforis.collect.ui.component.detail.MultipleEntityAsTableFormItem;
	import org.openforis.collect.ui.component.detail.MultipleEntityFormItem;
	import org.openforis.collect.ui.component.detail.SingleAttributeFormItem;
	import org.openforis.collect.ui.component.detail.SingleEntityFormItem;
	import org.openforis.collect.ui.component.input.AutoCompleteInputField;
	import org.openforis.collect.ui.component.input.BooleanInputField;
	import org.openforis.collect.ui.component.input.CodeInputField;
	import org.openforis.collect.ui.component.input.CoordinateAttributeRenderer;
	import org.openforis.collect.ui.component.input.DateAttributeRenderer;
	import org.openforis.collect.ui.component.input.FixedCodeInputField;
	import org.openforis.collect.ui.component.input.ImageInputField;
	import org.openforis.collect.ui.component.input.InputField;
	import org.openforis.collect.ui.component.input.IntegerInputField;
	import org.openforis.collect.ui.component.input.MemoInputField;
	import org.openforis.collect.ui.component.input.MultipleCodeInputField;
	import org.openforis.collect.ui.component.input.NumericAttributeRenderer;
	import org.openforis.collect.ui.component.input.NumericInputField;
	import org.openforis.collect.ui.component.input.RangeAttributeRenderer;
	import org.openforis.collect.ui.component.input.RangeInputField;
	import org.openforis.collect.ui.component.input.StringInputField;
	import org.openforis.collect.ui.component.input.TaxonAttributeRenderer;
	import org.openforis.collect.ui.component.input.TimeAttributeRenderer;
	import org.openforis.collect.util.CollectionUtil;
	import org.openforis.collect.util.UIUtil;
	
	import spark.components.HGroup;
	import spark.components.Label;
	import spark.components.SkinnableContainer;
	import spark.components.gridClasses.GridColumn;
	
	/**
	 * @author Mino Togna
	 * @author S. Ricci
	 * 
	 * */
	public class UIBuilder {
		
		private static const DATA_GROUP_HEADER_STYLE:String = "dataGroupHeader";
		
		public static function buildForm(rootEntity:EntityDefinitionProxy, version:ModelVersionProxy):FormContainer {
			var formContainer:FormContainer = new FormContainer();
			formContainer.initialize();
			
			addMainEntityFormContainer(formContainer, rootEntity, version);
			
			var rootTabSet:UITabSetProxy = getRootEntityTabSet(rootEntity);
			if ( rootTabSet != null && rootTabSet.tabs != null) {
				for each (var tab:UITabProxy in rootTabSet.tabs) {
					if ( ! isMainTab(rootEntity, tab) ) {
						addChildEntityFormContainer(formContainer, rootEntity, version, tab);
					}
				}
			}
			return formContainer;
		}
		
		/**
		 * Returns true if the tab is the first tab in the corresponding root tab set
		 */
		private static function isMainTab(rootEntity:EntityDefinitionProxy, tab:UITabProxy):Boolean {
			var rootTabSet:UITabSetProxy = getRootEntityTabSet(rootEntity);
			return rootTabSet.tabs.getItemIndex(tab) == 0;
		}
		
		private static function addMainEntityFormContainer(formContainer:FormContainer, rootEntity:EntityDefinitionProxy, version:ModelVersionProxy):void {
			var form:EntityFormContainer = new EntityFormContainer();
			form.entityDefinition = rootEntity;
			form.modelVersion = version;
			
			form.build();
			formContainer.addEntityFormContainer(form);
			/*
			in this case the parentEntity of the formContainer will be null and 
			the "entity" will be record's "rootEntity"
			*/
			form.parentEntity = null;
			BindingUtils.bindProperty(form, "entity", formContainer, ["record", "rootEntity"]);
		}
		
		private static function addChildEntityFormContainer(formContainer:FormContainer, rootEntity:EntityDefinitionProxy, version:ModelVersionProxy, tab:UITabProxy):void {
			var childForm:EntityFormContainer = new EntityFormContainer();
			var child:NodeDefinitionProxy = rootEntity.getChildDefinitionByTabName(tab.name);
			if(child is EntityDefinitionProxy) {
				var edp:EntityDefinitionProxy = child as EntityDefinitionProxy;
				childForm.entityDefinition = edp;
				childForm.modelVersion = version;
				childForm.build();
				formContainer.addEntityFormContainer(childForm);
				/*
				in this case the parentEntity will be the record's rootEntity
				*/
				BindingUtils.bindProperty(childForm, "parentEntity", formContainer, ["record", "rootEntity"]);
			}
		}
		
		public static function getRecordSummaryListColumns(rootEntity:EntityDefinitionProxy):IList {
			var columns:IList = new ArrayList();
			var column:GridColumn;
			//key attributes columns
			var position:int = 1;
			var keyAttributeDefs:IList = rootEntity.keyAttributeDefinitions;
			var headerText:String, dataField:String, width:Number, labelFunction:Function;
			for each(var keyAttributeDef:AttributeDefinitionProxy in keyAttributeDefs) {
				headerText = keyAttributeDef.getLabelText();
				dataField = "key" + position;
				width = NaN;
				labelFunction = RecordSummaryDataGrid.keyLabelFunction;
				column = getGridColumn(headerText, dataField, width, labelFunction, true);
				columns.addItem(column);
				position ++;
			}
			//count entity columns
			var firstLevelDefs:IList = rootEntity.childDefinitions;
			position = 1;
			for each(var nodeDef:NodeDefinitionProxy in firstLevelDefs) {
				if(nodeDef is EntityDefinitionProxy) {
					var entityDef:EntityDefinitionProxy = EntityDefinitionProxy(nodeDef);
					if(entityDef.countInSummaryList) {
						//headerText = Message.get("list.headerCount", [entityDef.getLabelText()]);
						headerText = entityDef.getLabelText();
						dataField = "count" + position;
						width = 80;
						labelFunction = RecordSummaryDataGrid.entityCountLabelFunction;
						column = getGridColumn(headerText, dataField, width, labelFunction, true);
						columns.addItem(column);
						position ++;
					}
				}
			}
			/*
			//errors count column
			column = getGridColumn(Message.get("list.errors"), "errors", 80, UIUtil.gridColumnNumberLabelFunction);
			columns.addItem(column);
			//skipped count column
			column = getGridColumn(Message.get("list.skipped"), "skipped", 80, UIUtil.gridColumnNumberLabelFunction);
			columns.addItem(column);
			//missing count column
			column = getGridColumn(Message.get("list.missing"), "missing", 80, UIUtil.gridColumnNumberLabelFunction);
			columns.addItem(column);
			*/
			//errors count column
			column = getGridColumn(Message.get("list.errors"), "errors", 80, RecordSummaryDataGrid.errorsCountLabelFunction, false, new ClassFactory(RecordSummaryErrorsColumnItemRenderer));
			columns.addItem(column);
			//warnings count column
			column = getGridColumn(Message.get("list.warnings"), "warnings", 80, UIUtil.gridColumnNumberLabelFunction);
			columns.addItem(column);
			//creation date column
			column = getGridColumn(Message.get("list.creationDate"), "creationDate", 120, UIUtil.gridColumnDateTimeLabelFunction);
			columns.addItem(column);
			//date modified column
			column = getGridColumn(Message.get("list.modifiedDate"), "modifiedDate", 120, UIUtil.gridColumnDateTimeLabelFunction);
			columns.addItem(column);
			//entry completed column
			column = getGridColumn(Message.get("list.entryComplete"), "entryComplete", 70, 
				null, true, new ClassFactory(CompleteColumnItemRenderer));
			columns.addItem(column);
			//cleansing completed column
			column = getGridColumn(Message.get("list.cleansingComplete"), "cleansingComplete", 70, 
				null, true, new ClassFactory(CompleteColumnItemRenderer));
			columns.addItem(column);
			return columns;
		}
		
		public static function getAttributeFormItem(def:AttributeDefinitionProxy):AttributeFormItem {
			var parentLayout:String = def.parentLayout;
			var formItem:AttributeFormItem = null;
			if(def is CodeAttributeDefinitionProxy) {
				formItem = new CodeAttributeFormItem();
			} else if(def is CoordinateAttributeDefinitionProxy || def is TaxonAttributeDefinitionProxy) {
				formItem = new CompositeAttributeFormItem();
			} else if(def.multiple) {
				if(parentLayout == UIUtil.LAYOUT_TABLE){
					formItem = new MultipleAttributeDataGroupFormItem();
				} else {
					formItem = new MultipleAttributeFormItem();
				}
			} else {
				formItem = new SingleAttributeFormItem();
			}
			formItem.attributeDefinition = def;
			return formItem;
		}
		
		public static function getEntityFormItem(definition:EntityDefinitionProxy):EntityFormItem {
			var entityFormItem:EntityFormItem = null;
			if(definition.multiple) {
				if ( definition.layout == UIUtil.LAYOUT_FORM ) {
					entityFormItem = new MultipleEntityFormItem();
				} else {
					entityFormItem = new MultipleEntityAsTableFormItem();
				}
			} else {
				entityFormItem = new SingleEntityFormItem();
			}
			entityFormItem.entityDefinition = definition;
			return entityFormItem;
		}
		
		public static function getInputFieldWidth(def:AttributeDefinitionProxy):Number {
			var parentLayout:String = def.parentLayout;
			if(def is BooleanAttributeDefinitionProxy) {
				var headerText:String = def.getLabelText();
				var headerWidth:Number = UIUtil.measureGridHeaderWidth(headerText);
				var width:Number = Math.max(headerWidth, 20);
				return width;
			} else if(def is CodeAttributeDefinitionProxy) {
				if(parentLayout == UIUtil.LAYOUT_TABLE) {
					if(def.key && def.parent.enumerable) {
						//return NaN;
						return 150;
					} else {
						return 85;
					}
				} else {
					return NaN;
				}
			} else if(def is CoordinateAttributeDefinitionProxy) {
				if(parentLayout == UIUtil.LAYOUT_TABLE) {
					return 310;
				} else {
					return 100;
				}
			} else if(def is DateAttributeDefinitionProxy) {
				return 130;
			} else if(def is FileAttributeDefinitionProxy) {
				return 300;
			} else if(def is NumberAttributeDefinitionProxy) {
				var units:IList = NumberAttributeDefinitionProxy(def).units;
				var gap:int = 2;
				if(units.length > 1) {
					return 70 + gap + 120;
				} else if ( units.length == 1 && def.parentLayout == UIUtil.LAYOUT_FORM ) {
					var unit:UnitProxy = units.getItemAt(0) as UnitProxy;
					var unitWidth:Number = UIUtil.measureUnitWidth(unit.name);
					return 70 + gap + unitWidth;
				} else {
					return 70;
				}
			} else if(def is RangeAttributeDefinitionProxy) {
				var rangeDef:RangeAttributeDefinitionProxy = RangeAttributeDefinitionProxy(def);
				var rangeUnitsCount:int = rangeDef.units.length;
				if(rangeUnitsCount > 1) {
					return 242;
				} else if(rangeUnitsCount == 1 && def.parentLayout == UIUtil.LAYOUT_FORM ) {
					return 147;
				} else {
					return 120;
				}
			} else if(def is TaxonAttributeDefinitionProxy) {
				if(parentLayout == UIUtil.LAYOUT_TABLE) {
					return 504;
				} else {
					return 100;
				}
			} else if(def is TextAttributeDefinitionProxy) {
				var textAttributeDef:TextAttributeDefinitionProxy = TextAttributeDefinitionProxy(def);
				var type:TextAttributeDefinitionProxy$Type = textAttributeDef.type;
				switch(type) {
					case TextAttributeDefinitionProxy$Type.MEMO:
						return 300;
					case TextAttributeDefinitionProxy$Type.SHORT:
						return 100;
					default:
						return 150;
				}
			} else if(def is TimeAttributeDefinitionProxy) {
				return 64;
			} else {
				return 100;
			}
		}
		
		public static function getAttributeDataGroupHeaderWidth(def:AttributeDefinitionProxy, ancestorEntity:EntityProxy):Number {
			var parentEntityDefn:EntityDefinitionProxy = def.parent;
			if(ancestorEntity != null && parentEntityDefn.enumerable && def.key && def is CodeAttributeDefinitionProxy) {
				var enumeratedCodeWidth:Number = ancestorEntity.getEnumeratedCodeWidth(parentEntityDefn.name);
				var headerText:String = def.getLabelText();
				var headerWidth:Number = UIUtil.measureGridHeaderWidth(headerText);
				var width:Number = Math.max(headerWidth, enumeratedCodeWidth);
				return width + 2;
			} else {
				var inputFieldWidth:Number = getInputFieldWidth(def);
				if(!isNaN(inputFieldWidth)) {
					return inputFieldWidth + 2; //consider validation display border container and gap
				} else {
					return NaN;
				}
			}
		}
		
		public static function getInputField(def:AttributeDefinitionProxy):InputField {
			var parentLayout:String = def.parentLayout;
			var inputField:InputField = null;
			if(def is BooleanAttributeDefinitionProxy) {
				inputField = new BooleanInputField();
			} else if(def is CodeAttributeDefinitionProxy) {
				var codeDef:CodeAttributeDefinitionProxy = CodeAttributeDefinitionProxy(def);
				if(parentLayout == UIUtil.LAYOUT_TABLE && codeDef.parent.enumerable && codeDef.key) {
					inputField = new FixedCodeInputField();
				} else if(def.multiple) {
					inputField = new MultipleCodeInputField();
				} else {
					inputField = new CodeInputField();
				}
			} else if(def is FileAttributeDefinitionProxy) {
				//TODO use different input fields for text and image files
				inputField = new ImageInputField();
			} else if(def is NumberAttributeDefinitionProxy) {
				var numberAttributeDefn:NumberAttributeDefinitionProxy = NumberAttributeDefinitionProxy(def);
				if(numberAttributeDefn.type == NumberAttributeDefinitionProxy$Type.INTEGER) {
					inputField = new IntegerInputField();
				} else {
					inputField = new NumericInputField();
				}
			} else if(def is RangeAttributeDefinitionProxy) {
				inputField = new RangeInputField();
			} else if(def is TextAttributeDefinitionProxy) {
				var textAttributeDef:TextAttributeDefinitionProxy = TextAttributeDefinitionProxy(def);
				var type:TextAttributeDefinitionProxy$Type = textAttributeDef.type;
				switch(type) {
					case TextAttributeDefinitionProxy$Type.MEMO:
						inputField = new MemoInputField();
						break;
					case TextAttributeDefinitionProxy$Type.SHORT:
					default:
						if(def.autocomplete) {
							inputField = new AutoCompleteInputField();
						} else {
							inputField = new StringInputField();
						}
						break;
				}
			}
			inputField.width = getInputFieldWidth(def);
			inputField.attributeDefinition = def;
			return inputField;
		}
		
		public static function getAttributeItemRenderer(def:AttributeDefinitionProxy):AttributeItemRenderer {
			var renderer:AttributeItemRenderer;
			if(def is CoordinateAttributeDefinitionProxy) {
				renderer = new CoordinateAttributeRenderer();
			} else if(def is DateAttributeDefinitionProxy) {
				renderer = new DateAttributeRenderer();
			} else if(def is NumberAttributeDefinitionProxy) {
				var numberAttributeDefn:NumberAttributeDefinitionProxy = NumberAttributeDefinitionProxy(def);
				if(numberAttributeDefn.precisionDefinitions != null && numberAttributeDefn.units.length >= 1) {
					renderer = new NumericAttributeRenderer();
					var width:Number = getInputFieldWidth(def);
					var borderWidth:Number = 1;
					renderer.width = width + borderWidth * 2;
				}
			} else if(def is RangeAttributeDefinitionProxy) {
				var rangeDef:RangeAttributeDefinitionProxy = RangeAttributeDefinitionProxy(def);
				renderer = new RangeAttributeRenderer();
			} else if(def is TaxonAttributeDefinitionProxy) {
				renderer = new TaxonAttributeRenderer();
			} else if(def is TimeAttributeDefinitionProxy) {
				renderer = new TimeAttributeRenderer();
			}
			if(renderer == null) {
				renderer = new AttributeItemRenderer();
				var inputField:InputField = getInputField(def);
				inputField.fieldIndex = 0;
				renderer.addElement(inputField);
				BindingUtils.bindProperty(inputField, "parentEntity", renderer, "parentEntity");
				BindingUtils.bindProperty(inputField, "attribute", renderer, "attribute");
				if ( inputField.hasOwnProperty("attributes") ) {
					BindingUtils.bindProperty(inputField, "attributes", renderer, "attributes");
				}
			}
			renderer.attributeDefinition = def;
			return renderer;
		}
		
		public static function getDataGroupHeader(defn:NodeDefinitionProxy, parentEntity:EntityProxy = null):IVisualElement {
			var elem:IVisualElement = null;
			if(defn is AttributeDefinitionProxy){
				elem = getAttributeDataGroupHeader(defn as AttributeDefinitionProxy, parentEntity);
			} else if(defn is EntityDefinitionProxy) {
				elem = getEntityDataGroupHeader(defn as EntityDefinitionProxy, parentEntity);
			}
			return elem;
		}
		
		private static function getEntityDataGroupHeader(defn:EntityDefinitionProxy, parentEntity:EntityProxy = null):IVisualElement {
			var result:SkinnableContainer = new SkinnableContainer();
			result.styleName = DATA_GROUP_HEADER_STYLE;
			result.percentHeight = 100;
			var l:Label = new Label();
			l.styleName = "bold";
			l.text = defn.getLabelText();
			result.addElement(l);
			
			var childDefinitionsContainer:HGroup = new HGroup();
			childDefinitionsContainer.percentHeight = 100;
			childDefinitionsContainer.verticalAlign = "bottom";
			childDefinitionsContainer.gap = 4;
			var childDefn:ListCollectionView = defn.childDefinitions;
			for each (var childDef:NodeDefinitionProxy in childDefn) {
				var elem:IVisualElement = getDataGroupHeader(childDef, null);
				childDefinitionsContainer.addElement(elem);
			}
			result.addElement(childDefinitionsContainer);
			
			return result;
		}
		
		private static function getAttributeDataGroupHeader(defn:AttributeDefinitionProxy, parentEntity:EntityProxy = null):IVisualElement {
			var result:SkinnableContainer = new SkinnableContainer();
			result.styleName = DATA_GROUP_HEADER_STYLE;
			var width:Number = getAttributeDataGroupHeaderWidth(defn, parentEntity);
			result.width = width;
			result.percentHeight = 100;
			var h:HGroup;
			var l:Label;
			if(defn is TaxonAttributeDefinitionProxy) {
				//attribute label
				l = getLabel(defn.getLabelText(), 100, "dataGroupHeader");
				result.addElement(l);
				//subheader
				h = new HGroup();
				h.gap = 6;
				l = getLabel(Message.get('edit.taxon.code'), 80, "bold");
				h.addElement(l);
				l = getLabel(Message.get('edit.taxon.scientificName'), 100, "bold");
				h.addElement(l);
				l = getLabel(Message.get('edit.taxon.vernacularName'), 100, "bold");
				h.addElement(l);
				l = getLabel(Message.get('edit.taxon.languageCode'), 100, "bold");
				h.addElement(l);
				l = getLabel(Message.get('edit.taxon.languageVariety'), 100, "bold");
				h.addElement(l);
				result.addElement(h);
			} else if(defn is CoordinateAttributeDefinitionProxy) {
				//attribute label
				l = getLabel(defn.getLabelText(), 100, "bold");
				result.addElement(l);
				//subheader
				h = new HGroup();
				h.gap = 6;
				l = getLabel(Message.get('edit.coordinate.srs'), 100, "bold");
				h.addElement(l);
				l = getLabel(Message.get('edit.coordinate.x'), 100, "bold");
				h.addElement(l);
				l = getLabel(Message.get('edit.coordinate.y'), 100, "bold");
				h.addElement(l);
				result.addElement(h);
			} else if (defn is NumberAttributeDefinitionProxy && NumberAttributeDefinitionProxy(defn).defaultUnit != null || 
				defn is RangeAttributeDefinitionProxy && RangeAttributeDefinitionProxy(defn).defaultUnit != null ) {
				var defaultUnit:UnitProxy;
				if (defn is NumberAttributeDefinitionProxy) {
					defaultUnit = NumberAttributeDefinitionProxy(defn).defaultUnit;
				} else {
					defaultUnit = RangeAttributeDefinitionProxy(defn).defaultUnit;
				}
				var labStr:String = defn.getLabelText() + " (" + defaultUnit.name + ")";
				l = getLabel(labStr, width, "bold");
				result.addElement(l);
			} else {
				l = getLabel(defn.getLabelText(), width, "bold");
				result.addElement(l);
			}
			return result;
		}
		
		public static function getGridColumn(headerText:String, dataField:String, width:Number, 
											 labelFunction:Function = null, headerTextWrap:Boolean = false,
											 itemRenderer:IFactory = null
											):GridColumn {
			var c:GridColumn = new GridColumn();
			c.headerText = headerText;
			c.dataField = dataField;
			c.labelFunction = labelFunction;
			c.width = width;
			if(headerTextWrap) {
				c.headerRenderer = new ClassFactory(DataGridHeaderRenderer);
			}
			if(itemRenderer != null) {
				c.itemRenderer = itemRenderer;
			}
			return c;
		}

		public static function getLabel(text:String, width:Number = NaN, styleName:String = null):Label {
			var l:Label = new Label();
			l.text = text;
			l.width = width;
			l.styleName = styleName;
			return l;
		}

		public static function getDefinitionsInVersion(defs:IList, currentVersion:ModelVersionProxy):IList {
			var result:IList = new ArrayCollection();
			for each (var defn:NodeDefinitionProxy in defs) {
				if(currentVersion.isApplicable(defn)){
					result.addItem(defn);
				}
			}
			return result;
		}
		
		/**
		 * Returns a list of lists of NodeDefinitionProxy object.
		 * Each item of the list is a list of node definitions associated to the tab in that index.
		 **/
		public static function getDefinitionsPerEachSubTab(entityDefinition:EntityDefinitionProxy, modelVersion:ModelVersionProxy):IList {
			var result:IList = new ArrayCollection();
			var uiTab:UITabProxy = getUITab(entityDefinition);
			if ( uiTab != null ) {
				var tabs:ListCollectionView = uiTab.tabs;
				if ( CollectionUtil.isNotEmpty(tabs) ) {
					var totalTabs:int = tabs.length;
					for(var i:int = 0; i < totalTabs; i ++) {
						result.addItemAt(new ArrayCollection(), i);
					}
					//put each definition in the corresponding list per tab
					var childDefns:IList = UIBuilder.getDefinitionsInVersion(entityDefinition.childDefinitions, modelVersion);
					for each (var defn:NodeDefinitionProxy in childDefns) {
						var tabName:String = defn.uiTabName;
						var tabIndex:int = CollectionUtil.getItemIndex(tabs, "name", tabName);
						if(tabIndex >= 0) {
							var nodeDefs:IList = result[tabIndex];
							nodeDefs.addItem(defn);
						}
					}
				}
			}
			return result;
		}
			
		public static function getDefinitionsPerMainTab(entityDefinition:EntityDefinitionProxy, modelVersion:ModelVersionProxy):IList {
			var result:IList = new ArrayCollection();
			var uiTab:UITabProxy = getUITab(entityDefinition);
			var childDefns:IList = UIBuilder.getDefinitionsInVersion(entityDefinition.childDefinitions, modelVersion);
			for each (var defn:NodeDefinitionProxy in childDefns) {
				var tabName:String = defn.uiTabName;
				if ( tabName == uiTab.name ) {
					result.addItem(defn);
				}
			}
			return result;
		}
		
		public static function getRootEntityTabSet(rootEntityDefinition:EntityDefinitionProxy):UITabSetProxy {
			var survey:SurveyProxy = rootEntityDefinition.survey;
			var uiOpts:UIOptionsProxy = survey.uiOptions;
			var tabSet:UITabSetProxy = null;
			if(uiOpts != null) {
				var tabSetName:String = rootEntityDefinition.rootTabSetName;
				tabSet = uiOpts.getTabSet(tabSetName);
			}
			return tabSet;
		}
		
		public static function getUITab(nodeDefn:NodeDefinitionProxy):UITabProxy {
			var survey:SurveyProxy = nodeDefn.survey;
			var rootEntity:EntityDefinitionProxy = nodeDefn.rootEntity;
			var tabSet:UITabSetProxy = getRootEntityTabSet(rootEntity);
			if ( tabSet != null ) {
				var tab:UITabProxy = tabSet.getTab(nodeDefn.uiTabName);
				return tab;
			} else {
				return null;
			}
		}
		
	}
}