/**
 * Copyright (c) June 2003 F. Esquembre
 * Last revision: September 2008
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition.ode_editor;

import java.util.Vector;

import javax.swing.JOptionPane;

import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.osejs.edition.*;
import org.colos.ejs.osejs.edition.experiments.*;
import org.colos.ejs.osejs.utils.ResourceUtil;
import org.colos.ejs.osejs.utils.StringAndInteger;

import bsh.Interpreter;

public class EquationCode {
  static public final boolean PROCESS_DERIVATIVES = true;
  static private ResourceUtil res = new ResourceUtil ("Resources");
  
  /**
   * Fills the vector with the information about the variables in the given ODE editor
   * @param _ejs
   * @param _editor
   * @param _indVar
   * @param varNames
   * @return the total size as a String and the number of variables which are arrays
   */
  static StringAndInteger getEquationVariables(Osejs _ejs, EquationEditor _editor, String _indVar, Vector<EquationVariable> _variableList) {
    Vector<?> table = _editor.getDataVector();
    String totalSizeStr = "1";  // For the independent variable
    int arrayCounter = 0;
    for (int rowNumber=0,rowCount=table.size(); rowNumber<rowCount; rowNumber++) { // construct the list of variables
      Vector<?> row = (Vector<?>) table.get(rowNumber);
      String rateString = row.get(1).toString().trim(); 
      if (rateString.length()>0) { // The rate cell is not empty
        String stateString = row.get(0).toString().trim();
        if (stateString.length()<=0) continue; // The state cell is empty
        boolean isArray = true;
        if (stateString.indexOf("[")<0) { // If is has no [] or [i], it could still be an array.
          isArray = _ejs.getModelEditor().getVariablesEditor().isDoubleArray(stateString); // ask the variables editor
//          if (isArray) stateString = stateString+"[]"; // Allow for specifying arrays as x instead of x[]
        }
        if (isArray) {
          int index = stateString.indexOf ("[");
          String varName = (index<0) ? stateString : stateString.substring (0,index).trim();
          _variableList.add (new EquationVariable(varName,rowNumber,true,stateString,rateString));
          totalSizeStr += "+"+varName+".length";
          arrayCounter++;
        }
        else {
          _variableList.add (new EquationVariable(stateString,rowNumber,false,stateString,rateString));
          totalSizeStr += "+1";
        }
      }
    }
    if (PROCESS_DERIVATIVES && !_variableList.isEmpty()) { // See which variable is followed by its derivative
      EquationVariable currentVariable = _variableList.get(0);
      for (int i=1, n=_variableList.size(); i<n; i++) {
        EquationVariable nextVariable = _variableList.get(i);
//        System.out.println ("Current rate = "+currentVariable.getRateString());
//        System.out.println ("Next state   = "+nextVariable.getStateString());
//        System.out.println ("  is followed = "+ currentVariable.getRateString().equals(nextVariable.getStateString()));
        currentVariable.setFollowedByDerivative(currentVariable.getRateString().equals(nextVariable.getStateString()));
        currentVariable = nextVariable;
      }
    } // end of for
    
    // The independent variable goes last (OSP Convention)
    _variableList.add(new EquationVariable(_indVar,-1,false,_indVar,"1"));
    return new StringAndInteger(totalSizeStr,arrayCounter);
  }

  /**
   * Returns a StringBuffer with the code required to extract our variables from a single
   * unidmensional array, indicated by _fromState. Thsi code is used several times in the 
   * process of solving ODEs
   * @param _fromState
   * @param varNamesList
   * @return
   */
  static private StringBuffer extractVariablesFromStateCode(String _fromState, boolean _declareTemporary, Vector<EquationVariable> _variableList) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("      // Extract our variables from "+_fromState+"\n");
    buffer.append("      int __cOut=0;\n");
    for (int i=0,n=_variableList.size(); i<n; i++) {
      EquationVariable eqnVar = _variableList.get(i);
      String varName = eqnVar.getName();
//      System.out.println ("Var "+varName +" is followed by its derivative="+eqnVar.isFollowedByDerivative());
      if (eqnVar.isArray()) {
        if (_declareTemporary) buffer.append("      double[] "+varName+" = _"+varName+";\n");
        if (PROCESS_DERIVATIVES && eqnVar.isFollowedByDerivative()) {
          EquationVariable nextVar = _variableList.get(i+1);
          String nextVarName = nextVar.getName();
          if (_declareTemporary) buffer.append("      double[] "+nextVarName+" = _"+nextVarName+";\n");
          buffer.append("      for (int __i=0; __i<"+varName+".length; __i++) { // These two alternate in the state\n");
          buffer.append("        "+varName+"[__i] = "+_fromState+"[__cOut++];\n");
          buffer.append("        "+nextVarName+"[__i] = "+_fromState+"[__cOut++];\n");
          buffer.append("      }\n");
          i++; // step over the next one
        }
        else buffer.append("      System.arraycopy("+_fromState+",__cOut,"+varName+",0,"+varName+".length); __cOut+="+varName+".length;\n");
      }
      else {
        if (_declareTemporary) buffer.append("      double ");
        else buffer.append("      ");
        buffer.append(varName+" = "+_fromState+"[__cOut++];\n");
      }
    }
    return buffer;
  }
  
  static private StringBuffer updateStateFromVariablesCode(String _toState, boolean _addSynchro, Vector<EquationVariable> _variableList) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("      // Copy our variables to "+_toState+"[] \n");
    buffer.append("      int __cIn=0;\n"); 
    for (int i=0,n=_variableList.size(); i<n; i++) {
      EquationVariable eqnVar = _variableList.get(i);
      String varName = eqnVar.getName();
      if (eqnVar.isArray()) {
        if (PROCESS_DERIVATIVES && eqnVar.isFollowedByDerivative()) {
          EquationVariable nextVar = _variableList.get(i+1);
          String nextVarName = nextVar.getName();
          if (_addSynchro) {
            buffer.append("      if (!__mustReinitialize)\n");
            buffer.append("        for (int __i=0,__n=__cIn; __i<"+varName+".length; __i++)\n");
            buffer.append("          if ("+_toState+"[__n++]!="+varName+"[__i] || "+_toState+"[__n++]!="+nextVarName+"[__i]) { __mustReinitialize = true; break; }\n");
          }
          buffer.append("      for (int __i=0; __i<"+varName+".length; __i++) { // These two alternate in the state\n");
          buffer.append("         "+_toState+"[__cIn++] = "+varName+"[__i];\n");
          buffer.append("         "+_toState+"[__cIn++] = "+nextVarName+"[__i];\n");
          buffer.append("      }\n");
          i++; // step over the next one
        }
        else {
          if (_addSynchro) {
            buffer.append("      if (!__mustReinitialize)\n");
            buffer.append("        for (int __i=0,__n=__cIn; __i<"+varName+".length; __i++)\n");
            buffer.append("           if ("+_toState+"[__n++]!="+varName+"[__i]) { __mustReinitialize = true; break; }\n");
          }
          buffer.append("      System.arraycopy("+varName+",0,"+_toState+",__cIn,"+varName+".length); __cIn += "+varName+".length;\n");
        }
      }
      else {
        if (_addSynchro) buffer.append("      if ("+_toState+"[__cIn]!="+varName+") __mustReinitialize = true;\n");
        buffer.append("      "+_toState+"[__cIn++] = "+varName+";\n");
      }
    }
    return buffer;
  }
 
  /**
   * Creates a method to retrieve past values (from the memory) for each variable in the ODE
   * @param _editor
   * @param _variableList
   * @return
   */
  static private StringBuffer createFunctionsCode(EquationEditor _editor, String _odeName, String _indVar, Vector<EquationVariable> _variableList, boolean _useDelays) {
    StringBuffer buffer = new StringBuffer();
    String totalSizeStr = "0";
    for (int i=0,n=_variableList.size()-1; i<n; i++) { // -1 because the last one is time
      EquationVariable eqnVar = _variableList.get(i);
      String varName = eqnVar.getName();
      if (eqnVar.isArray()) { 
        buffer.append("    public double[] " + varName + " (double __time) {\n");
        buffer.append("      int __beginIndex = "+totalSizeStr+";\n");
        if(_useDelays) {
          buffer.append("      if ("+_odeName+"._delayIntervalData!=null) {\n");
          buffer.append("        for (int __i=0; __i<"+_odeName+"._delaysArray.length; __i++) \n");
          buffer.append("          if (Math.abs("+_odeName+"._delayCurrentTime-"+_odeName+"._delaysArray[__i]-__time)<1.e-14) \n");
          buffer.append("            return "+_odeName+"._delayIntervalData[__i].interpolate(__time,new double["+varName+".length],__index,"+varName+".length);\n");
          buffer.append("        throw new org.opensourcephysics.numerics.ODESolverException(_model + \": You are trying to use delays which is NOT previously defined for this DDE.\");\n");
          buffer.append("      }\n");
        }
//        buffer.append("      System.err.println (\"Not using the data for \"+__time);\n");
        buffer.append("      return "+_odeName+".__solver.getStateMemory().interpolate(__time,false,new double["+varName+".length],__beginIndex,"+varName+".length);\n");
        buffer.append("    }\n\n");
        totalSizeStr += "+"+varName+".length";
      }    
      else {
        buffer.append("    public double " + varName + " (double __time) {\n");
        buffer.append("      int __index = "+totalSizeStr+";\n");
        if(_useDelays) {
          buffer.append("      if ("+_odeName+"._delayIntervalData!=null) {\n");
          buffer.append("        for (int __i=0; __i<"+_odeName+"._delayIntervalData.length; __i++) \n");
          buffer.append("          if (Math.abs("+_odeName+"._delayCurrentTime-"+_odeName+"._delaysArray[__i]-__time)<1.e-14) \n");
          buffer.append("            return "+_odeName+"._delayIntervalData[__i].interpolate(__time,__index);\n");
          buffer.append("        throw new org.opensourcephysics.numerics.ODESolverException(_model + \": You are trying to use delays which is NOT previously defined for this DDE.\");\n");
          buffer.append("      }\n");
        }
//        buffer.append("      System.err.println (\"Not using the data for \"+__time);\n");
        buffer.append("      return "+_odeName+".__solver.getStateMemory().interpolate(__time,false,__index);\n");
        buffer.append("    }\n\n");
        totalSizeStr += "+1";
      }
    }
    return buffer;
  }
    
    static private void declareFunctionsCode(EquationEditor _editor, Vector<EquationVariable> _variableList, Interpreter _interpreter) throws bsh.EvalError {
      for (int i=0,n=_variableList.size()-1; i<n; i++) { // -1 because the last one is time
        EquationVariable eqnVar = _variableList.get(i);
        String varName = eqnVar.getName();
        if (eqnVar.isArray())  _interpreter.eval("double[] " + varName + " (double __time) { return null; }\n");
        else                   _interpreter.eval("double " + varName + " (double __time) { return 0; }\n");
      }
  }

//  static private StringBuffer createMemoryFunctionsCode(EquationEditor _editor, Vector<EquationVariable> _variableList, boolean _useDDESolvers) {
//    StringBuffer buffer = new StringBuffer();
//    buffer.append("    public org.opensourcephysics.numerics.Function getFunction (String __name) {\n");
//    if (_useDDESolvers) for (int i=0,n=_variableList.size()-1; i<n; i++) {
//      EquationVariable eqnVar = _variableList.get(i);
//      String varName = eqnVar.getName();
//      if (!eqnVar.isArray()) {
//        buffer.append("    if (\""+varName+"\".equals(__name)) return new org.opensourcephysics.numerics.Function() {\n");
//        buffer.append("      public double evaluate(double __time) {\n");
//        buffer.append("        return "+varName+"(__time);\n");
//        buffer.append("      }\n");
//        buffer.append("    };\n");
//      }
//    }
//    buffer.append("      return null;\n");
//    buffer.append("    }\n\n");
//
//    buffer.append("    public org.opensourcephysics.numerics.VectorRealFunction getVectorRealFunction(String __name) {\n");
//    if (_useDDESolvers) for (int i=0,n=_variableList.size()-1; i<n; i++) {
//      EquationVariable eqnVar = _variableList.get(i);
//      String varName = eqnVar.getName();
//      if (eqnVar.isArray()) {
//        buffer.append("    if (\""+varName+"\".equals(__name)) return new org.opensourcephysics.numerics.VectorRealFunction() {\n");
//        buffer.append("      public double[] evaluate(double __time) {\n");
//        buffer.append("        return "+varName+"(__time);\n");
//        buffer.append("      }\n");
//        buffer.append("    };\n");
//      }
//    }
//    buffer.append("      return null;\n");
//    buffer.append("    }\n\n");
//    return buffer;
//  }
  
  /**
   * Write the code to compute the rate
   * @param _singleRate true if the rate is required only for a simgle index (for MultirateODEs)
   * @param _info
   * @param _variableList
   * @return
   */
  static private StringBuffer computeRateCode (boolean _singleRate, String _info, Vector<EquationVariable> _variableList) {
    StringBuffer buffer = new StringBuffer();
    buffer.append("      // Compute the rate\n");
    buffer.append("      int __cRate = 0;\n");
    for (int varNumber=0,numVars=_variableList.size()-1; varNumber<numVars; varNumber++) {
      EquationVariable eqnVar = _variableList.get(varNumber);
      // location information for error display
      String fullInfo = " // "+_info+":"+(eqnVar.getRowNumber()+1)+"\n";
      if (eqnVar.isArray()) {
        String varLengthString = eqnVar.getName()+".length";
        String stateString = eqnVar.getStateString();
        String indexString = OsejsCommon.getPiece(stateString,"[","]",false); // i.e. "[i]" or "[]"
        if (PROCESS_DERIVATIVES && eqnVar.isFollowedByDerivative()) {
          EquationVariable nextVar = _variableList.get(varNumber+1);
          if (indexString==null || indexString.length()<=0) { // The index was not specified
            if (_singleRate) {
              buffer.append("      if (__index<(__cRate+2*"+varLengthString+")) {\n");
              buffer.append("        int __indexDelta = __index-__cRate;\n");
              buffer.append("        int __theIndex = __indexDelta/2;\n");
              buffer.append("        if (__indexDelta % 2 == 0) return "+eqnVar.getRateString() +"[__theIndex];"+fullInfo);
              buffer.append("        else                       return "+nextVar.getRateString()+"[__theIndex]; // "+_info+":"+(nextVar.getRowNumber()+1)+"\n");
              buffer.append("      }\n");
              buffer.append ("      __cRate += 2*"+varLengthString+";\n");
            }
            else {
              buffer.append("      for (int __i=0; __i<"+varLengthString+"; __i++) {\n");
              buffer.append("        __aRate[__cRate++] = "+eqnVar.getRateString() +"[__i];"+fullInfo);
              buffer.append("        __aRate[__cRate++] = "+nextVar.getRateString()+"[__i]; // "+_info+":"+(nextVar.getRowNumber()+1)+"\n");
              buffer.append("      }\n");
            }
          }
          else { // The index WAS specified
            if (_singleRate) {
              buffer.append("      if (__index<(__cRate+2*"+varLengthString+")) {\n");
              buffer.append("        int __indexDelta = __index-__cRate;\n");
              buffer.append("        int "+indexString+" = __indexDelta/2;\n");
              buffer.append("        if (__indexDelta % 2 == 0) return "+eqnVar.getRateString() +";"+fullInfo);
              buffer.append("        else                       return "+nextVar.getRateString()+"; // "+_info+":"+(nextVar.getRowNumber()+1)+"\n");
              buffer.append("      }\n");
              buffer.append ("      __cRate += 2*"+varLengthString+";\n");
            }
            else {
              buffer.append("      for (int "+indexString+"=0; "+indexString+"<"+varLengthString+"; "+indexString+"++) {\n");
              buffer.append("        __aRate[__cRate++] = "+eqnVar.getRateString() +";"+fullInfo);
              buffer.append("        __aRate[__cRate++] = "+nextVar.getRateString()+"; // "+_info+":"+(nextVar.getRowNumber()+1)+"\n");
              buffer.append("      }\n");
            }
          }
          varNumber++;
        }
        else { // Not followed by its derivative
          if (indexString==null || indexString.length()<=0) { // The index was not specified
            if (_singleRate) buffer.append("      if (__index<(__cRate+"+varLengthString+")) return "+eqnVar.getRateString()+"[__index-__cRate];"+fullInfo);
            else buffer.append ("      System.arraycopy("+eqnVar.getRateString()+",0,__aRate,__cRate,_"+varLengthString+");"+fullInfo);
            buffer.append ("      __cRate += "+varLengthString+";\n");
          }
          else { // The index WAS specified
            if (_singleRate) {
              buffer.append("      if (__index<__cRate+"+varLengthString+") { int "+indexString+" = __index-__cRate; return "+eqnVar.getRateString()+"; }"+fullInfo);
              buffer.append("      __cRate += "+varLengthString+";\n");
            }
            else buffer.append("      for (int "+indexString+"=0; "+indexString+"<"+varLengthString+"; "+indexString+"++) __aRate[__cRate++] = "+eqnVar.getRateString()+";"+fullInfo);
          }
        }
      }
      else {  // It's a single variable
        if (_singleRate) {
          buffer.append("      if (__index<=__cRate) return "+eqnVar.getRateString()+";" + fullInfo);
          buffer.append("      __cRate++;\n");
        }
        else buffer.append("      __aRate[__cRate++] = "+eqnVar.getRateString()+";"+fullInfo);
      }
    } // for ends
    if (_singleRate) buffer.append("      return 1.0; // The independent variable\n");
    else buffer.append("      __aRate[__cRate++] = 1.0; // The independent variable \n");
    return buffer;
  }
  
  /**
   * Fills the vector with the names of the variables
   * @param _editor
   * @param _indVar
   * @param varNames
   * @return the total size String
  static StringAndInteger getVarNames(Osejs _ejs, EquationEditor _editor, String _indVar, Vector<String> varNames) {
    String totalSizeStr = "1";  // For the independent variable
    int arrayCounter = 0;

    Vector<?> table = _editor.getDataVector();
    for (int i=0; i<table.size(); i++) { // construct the list of variables
      Vector<?> row = (Vector<?>) table.get(i);
      if (row.get(1).toString().trim().length()>0) {
        String aVar = row.get(0).toString().trim();
        if (aVar.length()<=0) continue;
        boolean isArray = true;
        if (aVar.indexOf("[")<0) { // If is has no [] or [i]
          isArray = _ejs.getModelEditor().getVariablesEditor().isDoubleArray(aVar);
          if (isArray) aVar = aVar+"[]"; // Allow for specifying double arrays as x instead of x[]
        }
        if (isArray) {
          String justvar = aVar.substring (0,aVar.indexOf ("[")).trim();
          varNames.add (new String("[]"+justvar)); // i.e. '[]x'
          totalSizeStr += "+"+justvar+".length";
          arrayCounter++;
        }
        else {
          varNames.add (new String(aVar)); // i.e. 'x'
          totalSizeStr += "+1";
        }
      }
    }
    // The independent variable goes last (OSP Convention)
    varNames.add(_indVar);
    return new StringAndInteger(totalSizeStr,arrayCounter);
  }
   */

  /**
   *  Checks whether the ODE has the x - dx ordering required by Verlet methods
   *  Returns an empty String if true. And a non-empty String with the variables that are not followed by their derivatives
   */
  static private String respectsVerletOrdering(Vector<EquationVariable> _variableList) {
    StringBuffer buffer = new StringBuffer();
    for (int i=0, num=_variableList.size()-1; i<num; i+=2) { // Check every second variable, excluding time
      EquationVariable eqnVar = _variableList.get(i);
      if (!eqnVar.isFollowedByDerivative()) buffer.append(eqnVar.getName()+"\n");
    }
    return buffer.toString();
  }

  /**
   * Adds to the interpreter the declaration of a method for each of the state variables of the ODE 
   * @param _interpreter
   */
  static public void addToInterpreter (Osejs _ejs, EquationEditor _editor, String _indVar, Interpreter _interpreter) throws bsh.EvalError {
    boolean useDelays = _editor.getDelays().length()>0;
    String memoryLength = _editor.getMemoryLength();
    boolean useDDESolvers = memoryLength.length()>0 || useDelays;
    if (useDDESolvers) {
      Vector<EquationVariable> variableList = new Vector<EquationVariable>();
      getEquationVariables(_ejs,_editor,_indVar,variableList);
      declareFunctionsCode(_editor,variableList,_interpreter);
    }
  }

  // Example:
  //  _solverType = "RK4"
  //  _generateName = "evolution1"
  //  _info = "Evolution:My page"
  //  _indVar = "time"
  // _eventEditor is the editor for events for this ODE (if any)
  static public StringBuffer generateCode (Osejs _ejs, EquationEditor _editor,
      String _generateName, String _info, //_pageName, Gonzalo 070218
      String _indVar) {

    
    // Extract the list of variables from the editor for further use
    Vector<EquationVariable> variableList = new Vector<EquationVariable>();
    StringAndInteger  sai = getEquationVariables(_ejs,_editor,_indVar,variableList);
    final int arrayCounter = sai.getInteger();
    final String totalSizeStr = sai.getString();

    if (_editor.getSolverType().contains("Verlet")) {
      String wrongList = respectsVerletOrdering(variableList);
      if (wrongList.length()>0) JOptionPane.showMessageDialog(_ejs.getMainPanel(), 
          res.getString("Experiment.ScheduledEvent.ODEPage")+": "+_editor.getName()+"\n"+
          res.getString("EquationEditor.VerletOrderingNotRespected")+wrongList,
            res.getString("Warning"), JOptionPane.WARNING_MESSAGE);
    }

    boolean hasScheduleEvents=_ejs.getExperimentEditor().hasScheduleEvents();

    // Now build the class
    String pageName = _editor.getName();
    String classname = "_ODE_"+_generateName;

    String toleranceStr = null;
    String absTol = _editor.getAbsoluteToleranceStr();
    if (absTol.length()>0) toleranceStr = "setTolerances("+absTol+","+_editor.getRelativeToleranceStr()+")";

    String incidenceMatrix = _editor.getIncidenceMatrix();
    boolean isMultirate = incidenceMatrix.length()>0; 
    boolean manualSynchro = !_editor.getForceSynchro();
    boolean useDelays = _editor.getDelays().length()>0;
    String memoryLength = _editor.getMemoryLength();
    boolean useDDESolvers = memoryLength.length()>0 || useDelays;

    StringBuffer txt = new StringBuffer();
    
    txt.append("  private " +classname+ " _ODEi_"+_generateName+";\n\n");
    if (useDDESolvers) txt.append(createFunctionsCode(_editor, "_ODEi_"+_generateName, _indVar, variableList,useDelays));

    txt.append("\n  // ----------- private class for ODE in page "+_info+"\n\n");
    txt.append("  private class "+classname + " implements org.opensourcephysics.numerics.EJSODE");
    if (useDelays)   txt.append(", org.opensourcephysics.numerics.DDE");
    if (isMultirate) txt.append(", org.opensourcephysics.numerics.qss.MultirateODE");
    if (_editor.zenoEditor!=null) txt.append(", org.opensourcephysics.numerics.ZenoEffectListener");
    txt.append(" {\n");

    txt.append("    private org.opensourcephysics.numerics.ODESolverInterpolator __solver=null; // The interpolator solver\n");
    txt.append("    private org.opensourcephysics.numerics.ODEInterpolatorEventSolver __eventSolver=null; // The event solver\n");
    txt.append("    private Class<?> __solverClass=null; // The solver class\n");
    txt.append("    private double[] __state=null; // Our state array\n");

//    txt.append("    private double __currentTime; // The time we called rate\n");
    //    txt.append("    private boolean __initialized=false; // Whether the solver has been initialized\n");
    
    boolean hasErrorCode = _editor.getErrorHandlingCode().toString().trim().length()>0;
    if (!hasErrorCode) txt.append("    private boolean __ignoreErrors=false; // Whether to ignore solver errors\n");
    txt.append("    private boolean __mustInitialize=true; // Be sure to initialize the solver\n");
    txt.append("    private boolean __isEnabled=true; // Whether it is enabled\n");
    if (manualSynchro) txt.append("    private boolean __mustReinitialize=true; // flag to reinitialize the solver\n");
    if(useDelays) {
//      txt.append("    private double __delayInitCondTime; // The initial value of time below which you ask for the initial conditions\n");
      txt.append("    private double _delayCurrentTime; // The time at which the user asks for past states\n");
      txt.append("    private org.opensourcephysics.numerics.dde_solvers.interpolation.IntervalData[] _delayIntervalData=null; // a place holder for past states of the DDE\n");
      txt.append("    private double[] _delaysArray = {" + _editor.getDelays() + "}; // The array of delays\n\n");
    }
    if (isMultirate) txt.append("    private int[][] __directArray, __inverseArray; // implementation of MultirateODE\n");

    txt.append("\n");

    if (arrayCounter>0) { // create temporary variables for arrays
      txt.append("    // Temporary array variables matching those defined by the user\n") ;
      for (EquationVariable eqnVar : variableList) {
        if (eqnVar.isArray()) txt.append("    private double[] _"+eqnVar.getName()+";\n");
      }
      txt.append("\n");
    }
    
    txt.append("    "+classname + "() { // Class constructor\n");
    if (useDDESolvers) txt.append("      __solverClass = org.opensourcephysics.numerics.dde_solvers."+_editor.getSolverType()+".class;\n");
    else               txt.append("      __solverClass = org.opensourcephysics.numerics."+_editor.getSolverType()+".class;\n");
    txt.append("      __instantiateSolver();\n");
    txt.append("      _privateOdesList.put(\""+pageName+"\",this);\n");
    txt.append("    }\n\n");

    txt.append("    public org.opensourcephysics.numerics.ODEInterpolatorEventSolver getEventSolver() { return __eventSolver; } \n\n");

    txt.append("    public void setSolverClass (Class<?> __aSolverClass) { // Change the solver in run-time\n");
    txt.append("      this.__solverClass = __aSolverClass;\n"); 
    txt.append("      __instantiateSolver();\n");
    //    txt.append("      __initializeSolver();\n");
    txt.append("    }\n\n"); 

    //    txt.append("    public void __setEnabled(boolean _enabled) { __enabled = _enabled; } \n\n");

    txt.append("    private void __instantiateSolver () {\n");
    txt.append("      __state = new double["+totalSizeStr+"];\n");
    if (arrayCounter>0) {
      txt.append("      // allocate temporary arrays\n");
      for (EquationVariable eqnVar : variableList) {
        if (eqnVar.isArray()) txt.append("      _"+eqnVar.getName()+" = new double["+eqnVar.getName()+".length];\n");
      }
    }
    txt.append("      __pushState();\n");
    if (isMultirate) {
      txt.append("      __directArray = __recomputeDirectArray();\n");
      txt.append("      __inverseArray = org.opensourcephysics.numerics.qss.MultirateUtils.getReciprocalMatrix(__directArray);\n");
    }
    txt.append("      try { // Create the solver by reflection\n");
    txt.append("        Class<?>[] __c = { org.opensourcephysics.numerics.ODE.class };\n");
    txt.append("        Object[] __o = { this };\n");
    txt.append("        java.lang.reflect.Constructor<?> __constructor = __solverClass.getDeclaredConstructor(__c);\n");
    txt.append("        __solver = (org.opensourcephysics.numerics.ODESolverInterpolator) __constructor.newInstance(__o);\n");
    txt.append("      } catch (Exception exc) { exc.printStackTrace(); } \n");
    txt.append("      __eventSolver = new org.opensourcephysics.numerics.ODEInterpolatorEventSolver(__solver);\n");
    txt.append("      __mustInitialize = true;\n"); 
    txt.append("    }\n\n");

    txt.append("    public void setEnabled (boolean __enabled) { __isEnabled = __enabled; }\n\n");
    txt.append("    public double getIndependentVariableValue () { return __eventSolver.getIndependentVariableValue(); }\n\n");

    txt.append("    public double getInternalStepSize () { return __eventSolver.getSolver().getInternalStepSize(); }\n\n");

    txt.append("    public boolean isAccelerationIndependentOfVelocity() { return "+ _editor.getAccelerationIndependentOfVelocity() +"; }\n\n");
    
    txt.append("    public void initializeSolver () {\n");
    
    if (arrayCounter>0)  {
      txt.append("      if (__arraysChanged()) { __instantiateSolver(); initializeSolver(); return; }\n");
    }
    txt.append("      __pushState();\n");
    //    txt.append("      System.err.println (\"Initing \"+this);\n");
    txt.append("      __eventSolver.removeAllEvents();\n");
    String intStep = _editor.getInternalStepSize();
    if (intStep.length()<=0) txt.append("      __eventSolver.initialize("+_editor.getReadStepSize()+");\n");
    else txt.append("      __eventSolver.initialize("+intStep+");\n");
    txt.append("      __eventSolver.setBestInterpolation("+_editor.getUseBestInterpolation()+");\n");
    if (memoryLength.length()>0) txt.append("      __solver.setMemoryLength("+_editor.getMemoryLength()+");\n");
    
    String maxStep = _editor.getMaximumStepSize();
    if (maxStep.length()>0) txt.append("      __eventSolver.setMaximumInternalStepSize("+maxStep+");\n");
    String maxNumberOfSteps = _editor.getMaximumNumberOfSteps();
    if (maxNumberOfSteps.length()>0) txt.append("      __eventSolver.setMaximumInternalSteps("+maxNumberOfSteps+");\n");
    if (_editor.eventEditor!=null) {
      Vector<Editor> eventList = _editor.eventEditor.getPages();
      for (int counter=1,n=eventList.size(); counter<=n; counter++) {
        //        EventEditor anEvent = (EventEditor) eventList.elementAt(counter-1);
        txt.append("      if (_isEnabled_"+_generateName+"_Event"+counter+") __eventSolver.addEvent( new "+ classname +"_Event"+counter+"());\n");
      }
      if (_editor.zenoEditor!=null) txt.append("      __eventSolver.addZenoEffectListener(this);\n");
    }
    if (hasScheduleEvents) {
      txt.append("      java.util.ArrayList ___list = (java.util.ArrayList) _scheduledEventsList.get(\""+pageName+"\");\n");
      txt.append("      if (___list!=null) {\n");
      txt.append("        for (java.util.Iterator ___iterator = ___list.iterator(); ___iterator.hasNext(); ) {\n");
      txt.append("          int _seCounter = ((Integer) ___iterator.next()).intValue();\n");
      txt.append("          org.opensourcephysics.numerics.StateEvent _event = _createScheduledEvent(_seCounter);\n");
      txt.append("          if (_event!=null) __eventSolver.addEvent(_event);\n");
      txt.append("        } // end of for\n");
      txt.append("      } // end of if\n");
    }


    txt.append("      __eventSolver.setEstimateFirstStep("+_editor.getEstimateFirstStep()+");\n");
    txt.append("      __eventSolver.setEnableExceptions(false);\n");
//    txt.append("      __eventSolver.setEnableExceptions("+hasErrorCode+");\n");

    if (toleranceStr!=null) txt.append("      __eventSolver."+toleranceStr+";\n");
    if (manualSynchro) txt.append("      __mustReinitialize = true;\n");
    //    txt.append("      __initialized = true;\n");
    txt.append("      __mustInitialize = false;\n"); 
    
//    if(useDelays)txt.append("      __delayInitCondTime = __eventSolver.getIndependentVariableValue();\n");
    txt.append("    }\n\n");

    txt.append("    private void __pushState () { // Copy our variables to the state\n");
    txt.append(updateStateFromVariablesCode("__state",manualSynchro, variableList));
    txt.append("    }\n\n");

    if (arrayCounter>0) {
      txt.append("    private boolean __arraysChanged () {\n");
      for (EquationVariable eqnVar : variableList) {
        if (eqnVar.isArray()) txt.append("      if ("+eqnVar.getName()+".length != _"+eqnVar.getName()+".length) return true;\n");
      }
      txt.append("      return false;\n");
      txt.append("    }\n\n");
    }  // end of arrayCounter>0

    txt.append("    public void resetSolver () {\n");
    //    txt.append("      if (!__initialized) initializeSolver();\n");

    if (isMultirate) {
      txt.append("      __directArray = __recomputeDirectArray();\n");
      txt.append("      __inverseArray = org.opensourcephysics.numerics.qss.MultirateUtils.getReciprocalMatrix(__directArray);\n");
    }
    if (manualSynchro) txt.append("      __mustReinitialize = true;\n");
    //    txt.append("      System.err.println (\"period at reinit = \"+period);\n");
    txt.append("    }\n\n");

    //    txt.append("    public void debugTask () { System.err.println (\"period = \"+period); }\n\n");

    txt.append("    private void __errorAction () {\n");
    if (hasErrorCode) {
      txt.append("      int _errorCode = __eventSolver.getErrorCode();\n");
      txt.append(_editor.getErrorHandlingCode());
    }
    else {
      txt.append("      if (__ignoreErrors) return;\n");
      txt.append("      System.err.println (__eventSolver.getErrorMessage());\n");
      txt.append("      int __option = javax.swing.JOptionPane.showConfirmDialog(_view.getComponent(_simulation.getMainWindow()),org.colos.ejs.library.Simulation.getEjsString(\"ODEError.Continue\"),\n");
      txt.append("        org.colos.ejs.library.Simulation.getEjsString(\"Error\"), javax.swing.JOptionPane.YES_NO_CANCEL_OPTION);\n");
      txt.append("      if (__option==javax.swing.JOptionPane.YES_OPTION) __ignoreErrors = true;\n");
      txt.append("      else if (__option==javax.swing.JOptionPane.CANCEL_OPTION) _pause();\n");
    }
    if (manualSynchro) {
      txt.append("      // Make sure the solver is reinitialized;\n");
      txt.append("      __mustReinitialize = true;\n");
    }
    txt.append("    }\n\n");

    txt.append("    public double step() { return __privateStep(false); }\n\n");
    txt.append("    public double solverStep() { return __privateStep(true); }\n\n");

    txt.append("    private double __privateStep(boolean __takeMaximumStep) {\n");
    txt.append("      if (!__isEnabled) return 0;\n"); 
    txt.append("      if (__mustInitialize) initializeSolver();\n"); 
    if (arrayCounter>0)   txt.append("      if (__arraysChanged()) { __instantiateSolver(); initializeSolver(); }\n");
    txt.append("      __eventSolver.setStepSize("+_editor.getReadStepSize()+");\n");
    if (intStep.length()>0) txt.append("      __eventSolver.setInternalStepSize("+intStep+");\n");
    else txt.append("      __eventSolver.setInternalStepSize("+_editor.getReadStepSize()+");\n");
    if (memoryLength.length()>0) txt.append("      __solver.setMemoryLength("+_editor.getMemoryLength()+");\n");
    if (maxStep.length()>0) txt.append("      __eventSolver.setMaximumInternalStepSize("+maxStep+");\n");
    if (maxNumberOfSteps.length()>0) txt.append("      __eventSolver.setMaximumInternalSteps("+maxNumberOfSteps+");\n");
    String eventStep = _editor.getEventMaximumStep();
    if (eventStep.length()>0) txt.append("      __eventSolver.setMaximumEventStep("+eventStep+");\n");
    if (toleranceStr!=null) txt.append("      __eventSolver."+toleranceStr+";\n");
    txt.append("      __pushState();\n");
    //    txt.append("      synchronized(_model) {\n");
    
  //December
    if (useDelays) {
      txt.append("      double[] __delays = new double[] {" + _editor.getDelays() + " };\n");
      txt.append("      for (int i=0; i<__delays.length; i++){\n");
      txt.append("        if (__delays[i]!=_delaysArray[i]) {\n");
      if (manualSynchro) txt.append("          __mustReinitialize = true;\n");
      txt.append("          _delaysArray[i] = __delays[i];\n");
      txt.append("        }\n");
      txt.append("      }\n");
    }
    
//    if (hasErrorCode) {
//      txt.append("      double __stepTaken;\n");
//      txt.append("      try {\n");
//    }
    
    if (manualSynchro) {
      txt.append("      if (__mustReinitialize) { \n");
      //      txt.append("        System.err.println (\"period at REINIT = \"+period);\n");
      txt.append("        __eventSolver.reinitialize();\n");
      txt.append("        __mustReinitialize = false;\n"); 
      txt.append("        if (__eventSolver.getErrorCode()!=0) __errorAction();\n");
      txt.append("      }\n"); 
    }
    else txt.append("        __eventSolver.reinitialize(); // force synchronization: inefficient!\n");
    //    txt.append("      System.err.println (\"period at step = \"+period);\n");

//    if (hasErrorCode) {
//      txt.append("      __stepTaken = __takeMaximumStep ? __eventSolver.maxStep() : __eventSolver.step();\n");
//      txt.append("      }\n");
//      txt.append("      catch(Exception __exception) { __stepTaken = Double.NaN; }\n");
//    }
//    else 
      txt.append("      double __stepTaken = __takeMaximumStep ? __eventSolver.maxStep() : __eventSolver.step();\n");
    
    txt.append(extractVariablesFromStateCode("__state",false,variableList));
    txt.append("      // Check for error\n");
    txt.append("      if (__eventSolver.getErrorCode()!=0) __errorAction();\n");
//    txt.append("      if (Double.isNaN(__stepTaken)) __errorAction();\n");
    txt.append("      return __stepTaken;\n");
    txt.append("    }\n\n");

    txt.append("    public double[] getState () { return __state; }\n\n");

//    txt.append(createMemoryFunctionsCode(_editor, variableList,useDDESolvers));
    
    if (useDelays) {
      txt.append("    public double[] getDelays(double[] __aState) {\n");
      txt.append("      return _delaysArray;\n" );    
      txt.append("    }\n\n");

      txt.append("    public double getMaximumDelay() {\n");
      txt.append("      double maximum = Double.POSITIVE_INFINITY;\n" );    
      txt.append("      for (int i=0; i<_delaysArray.length; i++) maximum = Math.max(maximum,Math.abs(_delaysArray[i]));\n" );    
      txt.append("      return maximum;\n" );    
      txt.append("    }\n\n");
      
      txt.append("    public double[] getInitialConditionDiscontinuities() {\n");
      txt.append("      return new double[] {"+_editor.getDelayAddDiscont()+"};\n" ); 
      txt.append("    }\n\n");
      
      txt.append("    public double[] userDefinedInitialCondition(double _time){\n");
      txt.append("      // In case it uses the independent variable\n");
      txt.append("      double "+_indVar+" = _time;\n");
      txt.append(CodeEditor.splitCode(res.getString("EquationEditor.DelaysInitialConditionDiscontinuities"),
          _editor.getDelayInitCond(),_info + ":" + _editor.getName(),"      "));
      txt.append("    }\n\n");
       
      txt.append("    public double[] getInitialCondition(double _time, double _state[]) {\n");
//      txt.append("      if (_time<=__delayInitCondTime){\n");
      txt.append("      double[] userDelayInitCond = userDefinedInitialCondition(_time);\n");
      txt.append("      if (userDelayInitCond==null) return null;\n");
      txt.append("      System.arraycopy(userDelayInitCond,0,_state,0,userDelayInitCond.length);\n");
      txt.append("      _state[_state.length-1] = _time;\n");
      txt.append("       return _state; \n");
      txt.append("    }\n\n");

      //ODE's getRate that in this case is empty
      txt.append("    public void getRate (double[] __aState, double[] __aRate) { } // deliberately left empty\n\n"); 
      txt.append("    public void getRate (double[] __aState, org.opensourcephysics.numerics.dde_solvers.interpolation.IntervalData[] __intervals, double[] __aRate) {\n");
//      for (EquationVariable eqnVar : variableList) txt.append("       varNames.add (new String(\""+eqnVar.getName()+"\"));\n");
      txt.append("      _delayCurrentTime = __aState[__aState.length-1]; // so that one can only ask for t-delays past states\n");
      txt.append("      _delayIntervalData = __intervals; // so that one can only ask for t-delays past states\n");
    } // end of useDelay
    else txt.append("    public void getRate (double[] __aState, double[] __aRate) {\n");

    txt.append("      __aRate[__aRate.length-1] = 0.0; // In case the prelim code returns\n");
    txt.append("      int __index=-1; // so that it can be used in preliminary code\n");

    txt.append(extractVariablesFromStateCode("__aState",true,variableList));
//    txt.append("       __currentTime = "+_indVar+";\n");
    txt.append("      // Preliminary code: "+_editor.prelimEditor.getCommentField().getText()+"\n");
    txt.append(_editor.prelimEditor.generateCode(Editor.GENERATE_PLAINCODE,_info));
    
    txt.append(computeRateCode (false, _info, variableList));
    
    if (useDelays) txt.append("      _delayIntervalData = null; // so that others can ask for any past state\n");
    txt.append("    }//end of getRate\n\n");

    if (isMultirate) {
      txt.append("    public int[][] __recomputeDirectArray() {\n");
      txt.append(CodeEditor.splitCode(res.getString("EquationEditor.QSSDirectIncidenceMatrix"),
          incidenceMatrix,_info + ":" + _editor.getName(),"      "));
      txt.append("    }\n\n");
      txt.append("    public int [][] getInverseIncidenceMatrix () { return __inverseArray; }\n\n");
      txt.append("    public int [][] getDirectIncidenceMatrix ()  { return __directArray; }\n\n");

      txt.append("    public double getRate(double[] __aState, int __index) {\n");
      
      txt.append(extractVariablesFromStateCode("__aState",true,variableList));

      txt.append("      // Preliminary code: "+_editor.prelimEditor.getCommentField().getText()+"\n");
      txt.append(_editor.prelimEditor.generateCode(Editor.GENERATE_PLAINCODE,_info));

      txt.append("      // Compute a single rate\n");
      txt.append(computeRateCode (true, _info, variableList));
      txt.append("    } // End of single getRate(index)\n\n");
    }

    // The zeno effect listener
    if (_editor.zenoEditor!=null) {
      txt.append("    // Implementation of org.opensourcephysics.numerics.ZenoEffectListener\n");
      txt.append("    public boolean zenoEffectAction(org.opensourcephysics.numerics.GeneralStateEvent __anEvent, double[] __aState) {\n");
      txt.append(extractVariablesFromStateCode("__aState",true,variableList));
      txt.append(_editor.zenoEditor.generateCode(Editor.GENERATE_PLAINCODE,_info));
      txt.append(updateStateFromVariablesCode("__aState",false, variableList));
      txt.append("      return "+_editor.zenoEditor.isSelected()+";\n");
      txt.append("    }\n\n");

    }

    // Now build the event classes
    if (_editor.eventEditor!=null) {
      Vector<Editor> eventList = _editor.eventEditor.getPages();
      for (int counter=1,n=eventList.size(); counter<=n; counter++) {
        EventEditor anEvent = (EventEditor) eventList.elementAt(counter-1);
        //        if (!anEvent.isActive()) continue;
        txt.append("    private class " + classname + "_Event" + counter + " implements org.opensourcephysics.numerics.GeneralStateEvent {\n\n");
        txt.append("      public int getTypeOfEvent() { return org.opensourcephysics.numerics.GeneralStateEvent."+anEvent.getEventType()+"; }\n\n");
        txt.append("      public int getRootFindingMethod() { return "+anEvent.getMethod()+"; }\n\n");
        txt.append("      public int getMaxIterations() { return "+anEvent.getIterations()+"; }\n\n");

        txt.append("      public String toString () { return \""+anEvent.getName()+"\"; }\n\n");
        String eventToleranceStr = anEvent.getTolerance().trim();
        if (eventToleranceStr.length()<=0) {
          if (absTol.length()>0) eventToleranceStr = absTol;
        }
        txt.append("      public double getTolerance () { return "+eventToleranceStr+"; }\n\n");

        txt.append("      public double evaluate (double[] __aState) { \n");

        txt.append(extractVariablesFromStateCode("__aState",true,variableList));
        txt.append(anEvent.generateCode(EventEditor.ZERO_CONDITION,_info));
        txt.append("      }\n\n");

        txt.append("      public boolean action () { \n");
        txt.append(extractVariablesFromStateCode("__state",false,variableList));
        txt.append("        boolean _returnValue = userDefinedAction();\n");
        txt.append(updateStateFromVariablesCode("__state",false, variableList));
        txt.append("        return _returnValue;\n");
        txt.append("      }\n\n");

        txt.append("      private boolean userDefinedAction() {\n");
        txt.append(         anEvent.generateCode(EventEditor.ACTION,_info));
        txt.append("        return "+anEvent.getStopAtEvent()+";\n");
        txt.append("      }\n\n");

        txt.append("    } // End of event class "+classname+"_Event" + counter +"\n\n");
      }
    }

    // Now build the schedule event classes //Gonzalo 070217
    if (hasScheduleEvents) {
      TabbedEditor _scheduleEditor = _ejs.getExperimentEditor().getscheduledEventEditor();
      if (_scheduleEditor!=null) {
        txt.append("  // ---- Scheduled Events for "+ classname+ "\n\n");
        Vector<Editor> eventList = _scheduleEditor.getPages();

        txt.append("    org.opensourcephysics.numerics.StateEvent _createScheduledEvent (int _index) {\n");
        for (int counter=1,n=eventList.size(); counter<=n; counter++) {
          ScheduledEventEditor anEvent = (ScheduledEventEditor) eventList.elementAt(counter-1);
          if (!anEvent.isActive()) continue;
          if (anEvent.getOdePage().equals(pageName)) txt.append("      if (_index=="+counter+") return new _ScheduledEvent_" + counter+"();\n");
        }
        txt.append("      return null;\n");
        txt.append("    }\n\n");

        for (int counter=1,n=eventList.size(); counter<=n; counter++) {
          ScheduledEventEditor anEvent = (ScheduledEventEditor) eventList.elementAt(counter-1);
          if (!anEvent.isActive()) continue;
          if (anEvent.getOdePage().equals(pageName)){
            txt.append("    class _ScheduledEvent_" + counter + " implements org.opensourcephysics.numerics.StateEvent {\n");
            txt.append("\n      public double getTolerance () { return "+anEvent.getTolerance()+"; }\n\n");
            // the evaluate method
            txt.append("      public double evaluate (double[] __aState) { \n");
            txt.append(extractVariablesFromStateCode("__aState",true,variableList));
            txt.append(anEvent.generateCode(EventEditor.ZERO_CONDITION,_info));
            txt.append("      }\n\n");

            txt.append("      public boolean action () { \n");
            txt.append(extractVariablesFromStateCode("__state",false,variableList));
            txt.append("        boolean _returnValue = userDefinedAction();\n");
            txt.append(updateStateFromVariablesCode("__state",false, variableList));
            txt.append("        // Remove this scheduled event\n");
            txt.append("        __solver.removeEvent(this);\n");
            txt.append("        java.util.ArrayList ___list = (java.util.ArrayList) _scheduledEventsList.get(\""+pageName+"\");\n");
            txt.append("        if (___list!=null) ___list.remove(this);\n");
            txt.append("        return _returnValue;\n");
            txt.append("      }\n\n");

            txt.append("      private boolean userDefinedAction() {\n");
            txt.append(         anEvent.generateCode(ScheduledEventEditor.ACTION,_info));
            txt.append("        return "+anEvent.getStopAtEvent()+";\n");
            txt.append("      }\n\n");


            txt.append("    } // End of event class _ScheduledEvent_" + counter +"\n\n");
          }
        }
        txt.append("  // ---  End of Scheduled Events for "+ classname+ "\n\n");
      }
    }
    txt.append("  } // End of class "+classname+"\n\n");

    return txt;
  }


} // End of the class
