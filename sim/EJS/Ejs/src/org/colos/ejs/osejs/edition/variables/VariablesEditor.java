/**
 * The edition package contains generic tools to edit
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition.variables;

import java.util.StringTokenizer;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.EjsControl;
import org.colos.ejs.library.control.PropertyEditor;
import org.colos.ejs.library.control.value.BooleanValue;
import org.colos.ejs.library.control.value.DoubleValue;
import org.colos.ejs.library.control.value.IntegerValue;
import org.colos.ejs.library.control.value.ObjectValue;
import org.colos.ejs.library.control.value.StringValue;
import org.colos.ejs.library.control.value.Value;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.edition.Editor;
import org.colos.ejs.osejs.edition.ode_editor.EquationEditor;
import org.colos.ejs.osejs.edition.TabbedEditor;

import bsh.Interpreter;

/**
 * Created a TabbedEditor with tables of variables
 * @author Paco
 *
 */
public class VariablesEditor extends TabbedEditor implements org.colos.ejs.library.control.VariableEditor {

  public VariablesEditor (org.colos.ejs.osejs.Osejs _ejs) {
    super (_ejs, Editor.VARIABLE_EDITOR, "Model.Variables");
  }

  protected Editor createPage (String _type, String _name, String _code) {
    TableOfVariablesEditor page = new TableOfVariablesEditor (ejs);
    page.setName(_name);
    if (_code!=null) page.readString(_code);
    else page.clear();
    return page;
  }

  /**
   * Whether this variable name is already in use
   * @param _name
   * @return
   */
  public boolean nameExists(String _name) {
    for (java.util.Enumeration<Editor> e=pageList.elements(); e.hasMoreElements();) {
      TableOfVariablesEditor table = (TableOfVariablesEditor) e.nextElement();
      if (table.nameExists(_name)) return true;
    }
    if (ejs.getModelEditor().getElementsEditor().nameExists(_name)) return true;
    return false;
  }

  // ---------------------------------
  // Implementation of TableOfVariablesEditor
  // ---------------------------------

  /**
   * Checks the syntax, evaluates variables, and passes model variables to the control
   */
  public void updateControlValues (boolean showErrors) {
    //System.out.println ("Updating control values");
    EjsControl control = ejs.getViewEditor().getTree().getControl();
    if (control==null) return; //early call, no real variables in it
    resetInterpreter(); 
    control.clearModelVariables(); // Cleans the control values defined by the model
    control.resetTraces();
    for (Editor page : getPages())  ((TableOfVariablesEditor) page).evaluateVariables ();
    ejs.getModelEditor().getElementsEditor().evaluateVariables(ejs);
    for (Editor page : ejs.getModelEditor().getEvolutionEditor().getPages()) {
      if (page instanceof EquationEditor) ((EquationEditor) page).checkSyntax();
    }
    //control.update();
    ejs.getModelEditor().checkSyntax();
    ejs.getViewEditor().getTree().updateProperties(showErrors);
    control.update();
    control.finalUpdate();
  }

  // --- Syntax checking and evaluation of variables

  /**
   * Updates a variable of the model with the given value. 
   * Triggered by interaction of the user with the control.
   */
  public void updateTableValues (PropertyEditor _editor, String _variable, String _value, Value _theValue) {
    if (ejs.getViewEditor().getTree().getControl()==null) return;
    for (Editor page : getPages()) if (((TableOfVariablesEditor) page).updateVariableInTable(_editor,_variable,_value,_theValue)) break;
  }

  /**
   * Returns the initial value for a variable, if it exists. Null otherwise
   */
  public String getInitialValue (String _name) {  
    for (Editor page : getPages()) {
      String value = ((TableOfVariablesEditor) page).getInitialValue(_name);
      if (value!=null) return value;
    }
    return null;
  }

  // ---------------------------------
  // All about parsing expressions
  // ---------------------------------

  private Interpreter interpreter=new Interpreter();  // Interpreter for syntax error
  static private final int[] EMPTY_INT_ARRAY=new int[0];
  static private final double[] EMPTY_DOUBLE_ARRAY=new double[0];

  private void resetInterpreter() {
//  System.out.println ("resetting interpreter");
  try { interpreter.eval("clear()"); } 
  catch (Exception exc) { interpreter = new Interpreter(); }
  interpreter.setStrictJava(true);
  try { 
    for (String anImport : ejs.getSimInfoEditor().getImportsList()) interpreter.eval("import "+anImport);
    interpreter.eval("java.util.Map _stringProperties = new java.util.HashMap();");

    interpreter.eval("org.colos.ejs.library.control.EjsControl _view = new org.colos.ejs.library.control.EjsControl();");
    interpreter.eval(" org.opensourcephysics.numerics.Function _ZERO_FUNCTION = new org.opensourcephysics.numerics.Function() {public double evaluate(double x){return 0;}};");
    interpreter.eval("boolean _isPaused = true;");
    interpreter.eval("boolean _isPlaying = false;");
    interpreter.eval("boolean _isApplet = false;");
    interpreter.eval("boolean _isPaused() { return true; }");
    interpreter.eval("boolean _isPlaying() { return false; }");
    interpreter.eval("boolean _isApplet() { return false; }");
    String formatStr = "String _format(double _value, String _pattern) {\n" +
      "java.text.DecimalFormat _tmp_format = new java.text.DecimalFormat (_pattern);\n"+
      "return _tmp_format.format(_value);\n"+ 
      "}";
    interpreter.eval(formatStr);
    interpreter.eval("int _getDelay() { return 10; }");
    interpreter.eval("String _getParameter(String _name) { return _name; }");
    interpreter.eval("String[] _getArguments() { return null; }");
    interpreter.eval("String _getStringProperty(String _property) { Object _value = _stringProperties.get(_property); if (_value!=null) return _value.toString(); return _property; }");
    for (org.colos.ejs.osejs.utils.TwoStrings ts : ejs.getTranslationEditor().getResourceDefaultPairs()) {
      interpreter.eval("_stringProperties.put(\""+ts.getFirstString()+"\",\""+ts.getSecondString()+"\");");
    }

    // add extra declarations to the interpreter
    // in particular, methods for state variables of ODEs
    ejs.getModelEditor().getEvolutionEditor().addToInterpreter(interpreter);
    
      if (ejs.getSimInfoEditor().useInterpreter()) {
      TabbedEditor libEditor = ejs.getModelEditor().getLibraryEditor();
      for (Editor page : libEditor.getPages()) {
        String customCode = page.generateCode(Editor.GENERATE_CODE,null).toString();
        //        System.out.println ("Code for page "+page.getName()+" = \n"+customCode);
        try { interpreter.eval(customCode); }
        catch (bsh.EvalError parseExc) {
          ejs.getOutputArea().println(Osejs.getResources().getString("VariablesEditor.ErrorOnCustomPage")+" "+ page.getName()+ ":\n  "+parseExc.toString());
          //          ejs.getOutputArea().println("  ("+parseExc.getErrorLineNumber()+"): "+parseExc.getErrorText());
        }
      }
    }
//      String methods[] = (String[]) interpreter.get("this.methods");
//      for (int i=0; i<methods.length; i++) 
//        System.out.println ("Method "+i+" = "+methods[i]);
  } 
  catch (Exception exc) { exc.printStackTrace(); }
}

/**
 * Counts the number of (non-scaped) quotes in the string
 * @param str
 * @return
 */
static public int numberOfQuotes (String str) {
  int l = str.length();
  int counter = 0;
  for (int i=0; i<l; i++) {
    char c = str.charAt(i);
    if (c=='\"') ++counter;
    else if (c=='\\') i++;
  }
  return counter;
}

/**
 * Whether an expression is a constant for a given element property
 */
static public boolean isAConstant (ControlElement _element, String _property, String _expression) { //AMAVP (See note in ControlElement)
  if (_expression==null || _expression.length()<=0) return true;
  if (_element!=null) {  // Check for a special constant
    if (_element.parseConstant (_element.propertyType(_property),_expression)!=null) {
      //System.out.println (_expression +" Is a constant for the element");
      return true;
    }
    if (_element.propertyIsTypeOf(_property,"String")) {
      int count = numberOfQuotes(_expression);
      if (count==0) return true;
      if (count==2 && _expression.startsWith("\"") && _expression.endsWith("\"")) return true;
      if (count==1 || count>2) return false;
      return false;
    }
  }
  if (Value.parseConstantOrArray(_expression,true)!=null) return true; // It's a constant
  return false;
}

/**
 * Evaluates an expression 
 */
public Object evaluateExpression(String _expression) {
  try { return interpreter.eval(_expression); } 
  catch (Exception exc) { return null; }
}

/**
 * Whether a given name corresponds to a variable of the given type.
 */
public boolean isVariableDefined(String _name, String _type) {
  try {
    if (_name.indexOf('[')>0) return false; // avoid expressions like x[0] to be considered a variable
    interpreter.eval(_name+" = "+_name+";"); // This avoids evaluating java.awt.Color.RED as a variable
    // If I am here, the name stands for a variable. We now check the right type
    Object object = interpreter.get(_name);
    //if (object==null) return false; null strings or objects are acceptable
    if (_type.equals("double") || _type.equals("float")) return object instanceof Double;
    if (_type.equals("boolean")) return object instanceof Boolean;
    if (_type.equals("int") || _type.equals("char") || _type.equals("short") || _type.equals("long")) 
      return object instanceof Integer;
    if (_type.equals("String")) return object instanceof String || object==null; // a null string is also valid
    return true;
  }
  catch (Exception exc) { return false; }
}

/**
 * Whether a given name corresponds to double array.
 */
public boolean isDoubleArray(String _name) {
  try {
    String name = "__ejs_tmp";
    interpreter.unset(name);
    interpreter.eval("double[] "+name+" = "+_name+";"); 
    // If I am here, the name stands for a variable. We now check the right type
    return true;
  }
  catch (Exception exc) { return false; }
}

/**
 * Parses an expression of the given type and returns a Value with is value
 * @return a Value with the correct type and value, null if there was any error
 */
public Value checkExpression(String _expression, String _type) {
  try {
    String name = "__ejs_tmp";
    interpreter.unset(name);
    interpreter.eval(_type +" "+ name+";"); 
    interpreter.eval(name+" = "+_expression+ ";"); 
    Object object = interpreter.get(name);
    if (_type.equals("double") || _type.equals("float")) return new DoubleValue(((Double)object).doubleValue());
    if (_type.equals("boolean")) return new BooleanValue(((Boolean)object).booleanValue());
    if (_type.equals("int") || _type.equals("char") || _type.equals("short") || _type.equals("long")) return new IntegerValue(((Integer)object).intValue());
    if (_type.equals("String")) return new StringValue((String)object);
    return new ObjectValue(object);
  } 
  catch (Exception exc) { return null; }
}

/**
 * Parsers the given expression and assigns it to a variable of the given name, type, and dimension.
 * Used only by TableOfVariablesEditor
 * @return A Value with the correct type and value, null if there was any error
 */
public Value checkVariableValue(String _name, String _expression, String _type, String _dimension) {
//  System.out.println ("Checking variable "+_name+ " with value "+_expression);
  //if (control.getVariable(_name)!=null) return null; 
  try {
    if (interpreter.get(_name)!=null) return null; // The variable has been processed already
    int dim = 0;
    if (_dimension.length()>0) {
      java.util.StringTokenizer tkn = new java.util.StringTokenizer(_dimension,"[] ");
      dim = tkn.countTokens();
    }
    boolean hasExpression = _expression.length()>0;
    // Special hack to avoid bsh to issue an exception when a boolean a = 1!
    if (hasExpression && _type.equals("boolean")) {
      // If the value is a constant numeric value return null
      try { 
        Double.parseDouble(_expression);
        return null; 
      }
      catch (NumberFormatException nfe) {}
    }
    if (dim<=0) {  // Simple variable
      String sentence = _type +" " + _name;
      if (hasExpression) sentence += " = " + _expression;
      interpreter.eval(sentence);
      Object object = interpreter.get(_name);
      // Check for integers initialized to doubles. BeanShell accepts int x = 0.3; !!!
      if (hasExpression && object!=null && object.getClass()==Integer.class) {
        Object valueExpression = interpreter.eval(_expression);
        if (valueExpression.getClass()==Double.class) {
          //System.out.println("Incorrect value for <"+_name+"> = "+_expression);
          return null;
        }
      }
      Value value=null;
      if      (_type.equals("double") || _type.equals("float")) value = new DoubleValue(((Double) object).doubleValue());
      else if (_type.equals("boolean")) value = new BooleanValue(((Boolean) object).booleanValue());
      else if (_type.equals("int") || _type.equals("char") || _type.equals("short")) value = new IntegerValue(((Integer) object).intValue());
      else if (_type.equals("long")) value = new IntegerValue(((Long) object).intValue());
      else if (_type.equals("String")) value = new StringValue((String) object);
      else value =  new ObjectValue(object);
      return value;
    }
    // It is an array

    java.util.StringTokenizer tknIndexes = new java.util.StringTokenizer(_name,"[] ");
    int dimIndex = tknIndexes.countTokens();
    String lineOfIndexes = null;
    if (dimIndex>1) {
      _name = tknIndexes.nextToken();
      lineOfIndexes = tknIndexes.nextToken();
      while (tknIndexes.hasMoreTokens()) lineOfIndexes += ","+tknIndexes.nextToken();
      if ((dimIndex-1)!=dim) ejs.getOutputArea().println ("Syntax error in variable name "+_name.toString());
    }
    String sentence = _type;
    for (int k = 0; k < dim; k++) sentence += "[]";
    sentence += " " + _name;

    sentence += initCodeForAnArray (null, lineOfIndexes, _name, _type, _dimension, _expression, ejs);
    //System.out.println ("Init Sentence is "+sentence);
    interpreter.eval(sentence);
    Object object = interpreter.get(_name);

    // Check for integer arrays initialized with doubles. BeanShell accepts int[] x = new int[]{0.3,0.4}; !!!
    if (hasExpression && object.getClass()==EMPTY_INT_ARRAY.getClass()) {
      if (_expression.startsWith("{")) {     
        Object objectExpression = Value.parseConstantOrArray(_expression, true).getObject();
        if (objectExpression.getClass()==EMPTY_DOUBLE_ARRAY.getClass()) return null;
      }
    }

    return new ObjectValue(object);
  }
  catch (Exception exc) { return null; }
}

static public String initCodeForAnArray (String _comment, String _lineOfIndexes,
    String _name, String _type, String _dimension, String _value, Osejs _ejs) {
  
  StringTokenizer tkn = new java.util.StringTokenizer(_dimension,"[] ");
  int dim = tkn.countTokens();

  if (_value.startsWith("new ")) return " = " + _value+"; // " + _comment;
  
  if (!_ejs.getModelEditor().getVariablesEditor().isVariableDefined(_value, _type) && // It is NOT a single variable 
       _ejs.getModelEditor().getVariablesEditor().isVariableDefined(_value, "Object")) // But is an object, i.e. an array 
        return " = " + _value+"; // " + _comment;
    
  StringBuffer line = new StringBuffer(" = new "+_type+" ");
  if (_value.startsWith("{")) {
    while (tkn.hasMoreTokens()) { line.append("[]"); tkn.nextToken(); }
    line.append(_value);
    if (_comment==null) line.append(";");
    else line.append("; // " + _comment);
    return line.toString();
  }
  
  while (tkn.hasMoreTokens()) line.append("[" + tkn.nextToken() + "]");
  if (_comment==null) line.append(";");
  else line.append("; // " + _comment);
  if (_value.length()<=0) return line.toString();

  tkn = new java.util.StringTokenizer(_dimension,"[] ");
  if (_lineOfIndexes!=null) {
    StringTokenizer tknIndexes = new java.util.StringTokenizer(_lineOfIndexes,",");
    for (int k=0; k<dim; k++) {
      String indexStr = tknIndexes.nextToken();
      line.append("\n    for (int "+indexStr+"=0; "+indexStr+"<"+tkn.nextToken()+"; "+indexStr+"++) ");
    }
  }
  else {
    for (int k=0; k<dim; k++) {
      String indexStr = "_i"+k;
      line.append("\n    for (int "+indexStr+"=0; "+indexStr+"<"+tkn.nextToken()+"; "+indexStr+"++) ");
    }
  }
  line.append(_name);
  if (_lineOfIndexes!=null) {
    StringTokenizer tknIndexes = new java.util.StringTokenizer(_lineOfIndexes,",");
    for (int k=0; k<dim; k++) line.append("["+tknIndexes.nextToken()+"]");
  }
  else {
    for (int k=0; k<dim; k++) line.append("[_i"+k+"]");
  }
  line.append(" = "+_value+";");
  if (_comment!=null) line.append(" // " + _comment);
  return line.toString();
}

} // end of class

