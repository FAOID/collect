/**
 * Generated by Gas3 v2.2.0 (Granite Data Services).
 *
 * WARNING: DO NOT CHANGE THIS FILE. IT MAY BE OVERWRITTEN EACH TIME YOU USE
 * THE GENERATOR.
 */

package org.openforis.idm.metamodel.impl {

    import org.granite.util.Enum;

    [Bindable]
    [RemoteClass(alias="org.openforis.idm.metamodel.impl.ComparisonCheckImpl$OPERATION")]
    public class ComparisonCheckImpl$OPERATION extends Enum {

        public static const LT:ComparisonCheckImpl$OPERATION = new ComparisonCheckImpl$OPERATION("LT", _);
        public static const LTE:ComparisonCheckImpl$OPERATION = new ComparisonCheckImpl$OPERATION("LTE", _);
        public static const GT:ComparisonCheckImpl$OPERATION = new ComparisonCheckImpl$OPERATION("GT", _);
        public static const GTE:ComparisonCheckImpl$OPERATION = new ComparisonCheckImpl$OPERATION("GTE", _);
        public static const EQ:ComparisonCheckImpl$OPERATION = new ComparisonCheckImpl$OPERATION("EQ", _);

        function ComparisonCheckImpl$OPERATION(value:String = null, restrictor:* = null) {
            super((value || LT.name), restrictor);
        }

        override protected function getConstants():Array {
            return constants;
        }

        public static function get constants():Array {
            return [LT, LTE, GT, GTE, EQ];
        }

        public static function valueOf(name:String):ComparisonCheckImpl$OPERATION {
            return ComparisonCheckImpl$OPERATION(LT.constantOf(name));
        }
    }
}