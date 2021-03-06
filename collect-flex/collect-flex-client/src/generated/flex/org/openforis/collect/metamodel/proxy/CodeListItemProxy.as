/**
 * Generated by Gas3 v2.2.0 (Granite Data Services).
 *
 * NOTE: this file is only generated if it does not exist. You may safely put
 * your custom code here.
 */

package org.openforis.collect.metamodel.proxy {

    [Bindable]
    [RemoteClass(alias="org.openforis.collect.metamodel.proxy.CodeListItemProxy")]
    public class CodeListItemProxy extends CodeListItemProxyBase {
		
		public function getLabelText(language:String="en"):String {
			var result:String = LanguageSpecificTextProxy.getLocalizedText(this.labels, language);
			return result;
		}
		
		public function getDescriptionText(language:String = null):String {
			var result:String = LanguageSpecificTextProxy.getLocalizedText(this.descriptions, language);
			return result;
		}
		
    }
}