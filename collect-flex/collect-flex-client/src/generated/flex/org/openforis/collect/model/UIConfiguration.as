/**
 * Generated by Gas3 v2.2.0 (Granite Data Services).
 *
 * NOTE: this file is only generated if it does not exist. You may safely put
 * your custom code here.
 */

package org.openforis.collect.model {

    [Bindable]
    [RemoteClass(alias="org.openforis.collect.model.UIConfiguration")]
    public class UIConfiguration extends UIConfigurationBase {
		
		public function getTabDefinition(rootEntity:String):UITabDefinition {
			for each (var tabDef:UITabDefinition in tabDefinitions) {
				if(tabDef.rootEntity == rootEntity) {
					return tabDef;
				}
			}
			return null;
		}
		
    }
}