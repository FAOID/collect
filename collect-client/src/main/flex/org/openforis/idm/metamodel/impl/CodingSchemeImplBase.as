/**
 * Generated by Gas3 v2.2.0 (Granite Data Services).
 *
 * WARNING: DO NOT CHANGE THIS FILE. IT MAY BE OVERWRITTEN EACH TIME YOU USE
 * THE GENERATOR. INSTEAD, EDIT THE INHERITED CLASS (CodingSchemeImpl.as).
 */

package org.openforis.idm.metamodel.impl {

    import flash.utils.IDataInput;
    import flash.utils.IDataOutput;
    import flash.utils.IExternalizable;
    import mx.collections.ListCollectionView;
    import org.granite.util.Enum;
    import org.openforis.idm.metamodel.CodingScheme;
    import org.openforis.idm.metamodel.CodingScheme$CodeScope;
    import org.openforis.idm.metamodel.CodingScheme$CodeType;

    [Bindable]
    public class CodingSchemeImplBase implements IExternalizable, CodingScheme {

        private var _codeScope:CodingScheme$CodeScope;
        private var _codeType:CodingScheme$CodeType;
        private var _descriptions:ListCollectionView;
        private var _isDefault:Boolean;
        private var _labels:ListCollectionView;
        private var _name:String;

        [Bindable(event="unused")]
        public function get codeScope():CodingScheme$CodeScope {
            return _codeScope;
        }

        [Bindable(event="unused")]
        public function get codeType():CodingScheme$CodeType {
            return _codeType;
        }

        [Bindable(event="unused")]
        public function get descriptions():ListCollectionView {
            return _descriptions;
        }

        [Bindable(event="unused")]
        public function get labels():ListCollectionView {
            return _labels;
        }

        [Bindable(event="unused")]
        public function get name():String {
            return _name;
        }

        public function get default():Boolean {
            return false;
        }

        public function readExternal(input:IDataInput):void {
            _codeScope = Enum.readEnum(input) as CodingScheme$CodeScope;
            _codeType = Enum.readEnum(input) as CodingScheme$CodeType;
            _descriptions = input.readObject() as ListCollectionView;
            _isDefault = input.readObject() as Boolean;
            _labels = input.readObject() as ListCollectionView;
            _name = input.readObject() as String;
        }

        public function writeExternal(output:IDataOutput):void {
            output.writeObject(_codeScope);
            output.writeObject(_codeType);
            output.writeObject(_descriptions);
            output.writeObject(_isDefault);
            output.writeObject(_labels);
            output.writeObject(_name);
        }
    }
}