package org.colos.ejs.library.utils;

import org.colos.ejs.library.Model;

/**
 * A class of utilities for model elements
 * @author Francisco Esquembre
 * @version 1.0, August 2010
 *
 */
public class ModelElementsUtilities {

  /**
   * Removes a string from the beginning and end of the value (if it is there)
   */
  static private String removeEnclosingString(String _value, String _removeString) {
    int length = _removeString.length();
    if (_value.startsWith(_removeString)) _value = _value.substring(length);
    if (_value.endsWith(_removeString)) _value = _value.substring(0,_value.length()-length);
    return _value;
  }

  /**
   * Whether the value is linked to a model variable
   * @param _value
   * @return
   */
  static public boolean isLinkedToVariable(String _value) {
    return _value.startsWith("%");
  }
  
  /**
   * Gets the pure value, either the name of the model variable or the constant value
   * @param _value
   * @return
   */
  static public String getPureValue(String _value) {
    if (_value.startsWith("%")) return removeEnclosingString(_value,"%"); // It is linked to a model variable
    return removeEnclosingString(_value,"\"");
  }

  /**
   * Gets the value within quotes, either the name of the model variable or the constant value
   * @param _value
   * @return
   */
  static public String getQuotedValue(String _value) {
    if (_value.startsWith("%")) return "\"" + _value + "\""; // It is linked to a model variable
    return "\""+removeEnclosingString(_value,"\"")+"\"";
  }

  /**
   * Returns the value of a constant String (by removing its quotes), 
   * or that of a String variable of the model
   * @return
   */
  static public String getValue(Model _model, String _value) {
    if (_value.startsWith("%")) return _model._getVariable(removeEnclosingString(_value,"%"));
    return removeEnclosingString(_value,"\"");
  }

}
