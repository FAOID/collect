package org.openforis.collect.util
{
	import mx.utils.StringUtil;

	/**
	 * @author S. Ricci
	 */
	public class StringUtil	{
		
		public static function trim(value:String, trimChar:String = null):String {
			if(trimChar == null) {
				return mx.utils.StringUtil.trim(value);
			} else {
				var temp:String = leftTrim(value, trimChar);
				temp = rightTrim(temp, trimChar);
				return temp;
			}
		} 

		public static function leftTrim(value:String, trimChar:String = null):String {
			if(trimChar == null)
				trimChar = " ";
			var i:int = 0;
			while(i < value.length && value.charAt(i) == trimChar) {
				i++;
			}
			return value.substr(i);
		}  
		
		public static function rightTrim(value:String, trimChar:String = null):String {
			if(trimChar == null)
				trimChar = " ";
			var i:int = value.length - 1;
			while(i >= 0 && value.charAt(i) == trimChar) {
				i--;
			}
			return value.substr(0, i + 1);
		}  

		public static function replaceAll(string:String, regExp:RegExp, replace:String=""):String{
			var newString:String = new String(string);
			while(newString.search(regExp)>0){
				newString = newString.replace(regExp,replace);
			}
			return newString;
		}
		
		public static function isWhitespace(value:String):Boolean {
			return value == " ";
		}
		
		public static function isBlank(value:String, trim:Boolean = false):Boolean {
			return value == null || value == "" || (trim && mx.utils.StringUtil.trim(value) == "");
		}
		
		public static function isNotBlank(value:String):Boolean {
			return value != null && value != "";
		}
		
		public static function nullToBlank(value:String):String {
			if(value == null)
				return "";
			else return value;
		}
		
		/**
		 * Concats two or more strings separating them with a specified separator.
		 * Only not null and not blank values are included in the final string.
		 * 
		 * @param separator Separator to use in the concat
		 * @param args Strings to concat
		 * @return concatenated values
		 * 
		 **/
		public static function concat(separator:String, ... args):String {
			var parts:Array = [];
			for each(var value:String in args) {
				if(isNotBlank(value)) {
					parts.push(value);
				}
			}
			return parts.join(separator);
		}
		
		public static function concatEvenNulls(separator:String, ... args):String {
			var parts:Array = [];
			for each(var value:String in args) {
				if(isNotBlank(value)) {
					parts.push(value);
				} else {
					parts.push("");
				}
			}
			return parts.join(separator);
		}
		
		public static function startsWith(string:String, startsWith:String):Boolean {
			return string != null && string.indexOf(startsWith) == 0;
		}
	}
}