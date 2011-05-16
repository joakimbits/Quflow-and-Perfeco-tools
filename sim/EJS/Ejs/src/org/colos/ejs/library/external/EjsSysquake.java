package org.colos.ejs.library.external;

import java.util.*;
import java.io.*;
import com.calerga.sysquake.SysquakeLink;
import org.colos.ejs.library.Simulation;

public class EjsSysquake implements org.colos.ejs.library.external.ExternalApp, com.calerga.sysquake.SQLinkVariableListener {
  static private SysquakeLink sqlink = null;
  static private String homeDir=null;
  static private boolean alreadyLoaded = false;
  static private int dllCounter=-1;

  private int sqID = -1; // The integer id for the Sysquake connection
  private String sqFile = null;
  private ExternalClient client=null;
  private Hashtable<String, Integer> varTable = new Hashtable<String, Integer>();

// --------------------------------------------------------
//  Static methods and constructors
// --------------------------------------------------------

  static private SysquakeLink tryThisOne (String _dll) {
    // System.err.println ("EjsSysquake: trying "+_dll);
    File file = new File(_dll);
    try { _dll = file.getCanonicalPath(); }
    catch (Exception exc) { _dll = file.getAbsolutePath(); }
    // System.err.println ("EjsSysquake: actually "+_dll);
    if (file.exists()) return new SysquakeLink(_dll);
    return null;
  }

  static private void initSysquake() {
    if (sqlink!=null) return;
    homeDir = null;
//    System.out.println ("Initing Sysquake");
    String localDir = System.getProperty("user.dir").replace('\\','/');
    if (!localDir.endsWith("/")) localDir += "/";
    if (org.opensourcephysics.display.OSPRuntime.isWindows()) {
      sqlink = tryThisOne(localDir+Simulation.getPathToLibrary()+"_library/external/SysquakeLink.dll"); // Simulation directory
      if (sqlink==null) sqlink = tryThisOne(localDir+"_library/external/SysquakeLink.dll"); // Same directory
      if (sqlink==null) { // Not yet found. Extract it to the user's home directory
        homeDir = Simulation.getTemporaryDir();
        if (!alreadyLoaded) {
          dllCounter=0;
          for (int i=1; i<=50; i++) {
            File lib = new File (homeDir+"_library/external/SysquakeLink"+i+".dll");
            if (lib.exists()) lib.delete(); // This cleans the directory of previous runs
            else if (dllCounter==0) {
              dllCounter = i;
              if (Simulation.extractResource ("../_library/external/SysquakeLink.dll",lib.getPath())!=null) // For applets in their own directory
                  Simulation.extractResource (   "_library/external/SysquakeLink.dll",lib.getPath()); // For applications and applets in the same directory
            }
          }
          alreadyLoaded = true;
        }
        sqlink = tryThisOne(homeDir+"_library/external/SysquakeLink"+dllCounter+".dll");
      }
    }
    else {
     new SysquakeLink(localDir+"_library/external/libSysquakeLink.jnilib"); // Connect to Sysquake (Unix)
    }
  }

  /**
   * Creates a Sysquake session.
   * @param _mFile The mFile to use to get information about variables,
   * parameters or Simulink model. Can be empty (i.e. "") if no information is needed.
   */
  public EjsSysquake (String _sqFile) {
    initSysquake();
    String userDir=null;
    // Get the working directory
    if (homeDir!=null) userDir = homeDir;
    else {
      userDir = System.getProperty("user.dir").replace('\\','/');
      if (!userDir.endsWith("/")) userDir += "/";
    }

    if (_sqFile!=null) {
      sqFile = _sqFile.trim();
      if (sqFile.length()==0 || sqFile.toLowerCase().startsWith("<sysquake>")) sqFile = null;
      else if (homeDir!=null || ! new File (userDir+sqFile).exists()) {  // Extract the mFile using the Class.loader
        userDir = Simulation.getTemporaryDir();
        if (Simulation.extractResource(sqFile,userDir+sqFile)!=null) {
          System.out.println("Warning : the sq file "+sqFile+" does not exist!");
          sqFile = null;
        }
      }
      if (sqFile!=null) sqFile = userDir + sqFile;
    }
    try { SysquakeLink.connect(); }
    catch (Exception exc) { System.err.println("EjsSysquake error: "+exc); } // .printStackTrace(); }

    if (!SysquakeLink.isConnected()) System.out.println("Warning : Sysquake is NOT connected!!!");
//    System.out.println("IS connected = "+SysquakeLink.isConnected());
    loadSQFile();
  }

  private synchronized void loadSQFile () {
    try {
      SysquakeLink.resetVariableChangeNotification(sqID);
      if (sqID!=-1) ;
      if (sqFile==null) { sqID = -1; return; }
      SysquakeLink.show();
      sqID = SysquakeLink.open(sqFile);
      if (sqID==-1) return;
      SysquakeLink.setVariableChangeNotification(sqID, this);
      String[] varNames = SysquakeLink.variableNames(sqID);
      if (varNames!=null) for (int i=0,n=varNames.length; i<n; i++) {
        String text = varNames[i];
        if (!text.startsWith("_")) varTable.put(varNames[i],new Integer(i));
//        System.out.println("Var ["+i+"] = "+varNames[i]);
      }
  /*
      varNames = SysquakeLink.lmeVariableNames();
      if (varNames!=null) for (int i=0,n=varNames.length; i<n; i++) {
        String text = varNames[i];
        System.out.println("LME Var ["+i+"] = "+varNames[i]);
      }
  */
    }
    catch (Exception exc) { System.err.println("EjsSysquake error: "+exc); } // .printStackTrace(); }

  }

//  public void init () {  }

  //  Implementation of VariableChangeListener
  public void variableChange(int instanceId) {
    if (instanceId!=sqID) {
      System.out.println("Wrong Sysquake ID!");
      return;
    }
    if (client!=null) client._externalGetValuesAndUpdate(false,this);
  }

  public String toString () { return sqFile; }

// --------------------------------------------------------
//  Implementation of ExternalApp
// --------------------------------------------------------
  public boolean setVarContext(Object _object){
    System.out.println("Defining setVarContext");
    return false;
  }
  
  public boolean linkVariables(String ivar, String evar){
    return false;
  }
  
  public String connect (){//Gonzalo 090611
    return connectionOK;
  }
 
  public void disconnect (){}; //Gonzalo 090611
  
  public boolean isConnected(){ return true; }  //Gonzalo 090611
  
  public void setClient (ExternalClient _cli) { client = _cli; }

  public void setInitCommand (String _command) { }

  public void eval (String _command) {
    try { SysquakeLink.execute(_command); }
    catch (Exception exc) { System.err.println("EjsSysquake error: "+exc); } // .printStackTrace(); }
  }

  // Gonzalo 060420
  public synchronized void eval(String _command, boolean _flushNow) {
   eval (_command);
  }

  public void resetIC () { }
  public void resetParam () { }// Gonzalo 060420

  public synchronized void step (double dt) { }

  // Gonzalo 060420
  //Asynchronous Remote Simulink
  public  void stepAS (double dt){}
  public  void stepAS (double dt, int _package){}

  public synchronized void reset() {
    try {
      SysquakeLink.disconnect();
      SysquakeLink.connect();
      loadSQFile();
    }
    catch (Exception exc) { System.err.println("EjsSysquake error: "+exc); } // .printStackTrace(); }
  }

  public synchronized void quit() {
    try {
      if (sqID!=-1) SysquakeLink.resetVariableChangeNotification(sqID);
      SysquakeLink.disconnect();
      sqID = -1;
      SysquakeLink.quit();
      sqlink = null;
    }
    catch (Exception exc) { System.err.println("EjsSysquake error: "+exc); } // .printStackTrace(); }
  }

// --------------------------------------------------------------
// Accessing the variables using EjsMatlabInfo as identifier
// --------------------------------------------------------------

  public void setValues (Vector<String> _names, Vector<Object> _values) throws Exception {
    if (sqID==-1) return;
    int n = _names.size();
    if (n<=0) return;
    int[] indexes = new int[n];
    for (int i=0; i<n; i++) {
      Integer index = varTable.get(_names.get(i));
      if (index != null) indexes[i] = index.intValue();
      else {
        System.out.println("EjsSysquake Error: Variable "+_names.get(i)+ " not found");
        indexes[i] = -1;
      }
    }
    SysquakeLink.setVariableValue(sqID,indexes,_values.toArray());
  }

  public void setValue (String _name, String _value) throws Exception {
    Integer n = varTable.get(_name);
    if (n != null) SysquakeLink.setVariableValue(sqID,_name,_value);
    else SysquakeLink.execute(_name+" = '"+_value+"';");
  }

  public void setValue (String _name, double _value) throws Exception {
    Integer n = varTable.get(_name);
    if (n != null) SysquakeLink.setVariableValue(sqID,_name,new Double(_value));
    else SysquakeLink.execute(_name+" = "+_value+";");
  }

  public void setValue (String _name, double[] _value) throws Exception {
    Integer n = varTable.get(_name);
    if (n != null) SysquakeLink.setVariableValue(sqID,_name,_value);
    else {
      StringBuffer cmd = new StringBuffer(_name);
      cmd.append(" = [ ");
      for (int i=0; i<_value.length; i++) {
        if (i>0) cmd.append(",");
        cmd.append(Double.toString(_value[i]));
      }
      cmd.append("];");
      SysquakeLink.execute(cmd.toString());
    }
  }

  public void setValue (String _name, double[][] _value) throws Exception {
    Integer n = varTable.get(_name);
    if (n != null) SysquakeLink.setVariableValue(sqID,_name,_value);
    else {
      StringBuffer cmd = new StringBuffer(_name);
      cmd.append(" = [ ");
      for (int i=0; i<_value.length; i++) {
        if (i>0) cmd.append(";");
        for (int j = 0; j < _value[i].length; j++) {
          if (j>0) cmd.append(",");
          cmd.append(Double.toString(_value[i][j]));
        }
      }
      cmd.append("];");
      SysquakeLink.execute(cmd.toString());
    }
  }

  public synchronized String getString (String _variable) throws Exception {
    Object obj = null;
    Integer n = varTable.get(_variable);
    if (n != null) obj = SysquakeLink.variableValue(sqID,_variable);
    else           obj = SysquakeLink.lmeVariableValue(_variable);
    return (String) obj;
  }

  public double getDouble (String _variable) {
    Object obj = null;
    Integer n = varTable.get(_variable);
    try {
      if (n != null) obj = SysquakeLink.variableValue(sqID,_variable);
      else           obj = SysquakeLink.lmeVariableValue(_variable);
    }
    catch (Exception exc) { System.err.println("EjsSysquake error: "+exc); } // .printStackTrace(); }
    if (obj==null) return Double.NaN;
    return ((Double) obj).doubleValue();
  }

  public synchronized double[] getDoubleArray (String _variable) {
    Object obj = null;
    Integer n = varTable.get(_variable);
    try {
      if (n != null) obj = SysquakeLink.variableValue(sqID,_variable);
      else           obj = SysquakeLink.lmeVariableValue(_variable);
      if (obj instanceof double[][]) return ((double[][])obj)[0];
      return (double[]) obj;
    }
    catch (Exception exc) { System.err.println("EjsSysquake error: "+exc); } // .printStackTrace(); }
    return null;
  }

  public double[][] getDoubleArray2D (String _variable) {
    Object obj = null;
    Integer n = varTable.get(_variable);
    try {
      if (n != null) obj = SysquakeLink.variableValue(sqID,_variable);
      else           obj = SysquakeLink.lmeVariableValue(_variable);
    }
    catch (Exception exc) { System.err.println("EjsSysquake error: "+exc); } // .printStackTrace(); }
    return (double[][]) obj;
  }
  // Gonzalo 060420
  public synchronized void update(String _command,String _outputvars,int _steps){
  }
  public synchronized void update(String _command,String _outputvars,int _steps,int _package){
  }
  public synchronized void haltUpdate(boolean _remove){
  }
  public synchronized void haltStepAS(boolean _remove){
  }

  public void setValue (String _name, double[][] _value, boolean _flushNow) throws Exception {
    setValue (_name, _value);
  }
  public void setValue (String _name, double[] _value, boolean _flushNow) throws Exception {
    setValue (_name, _value);
  }
  public synchronized void setValue (String _name, String _value, boolean _flushNow) throws Exception {
    setValue (_name, _value);
  }

  public void setValue (String _name, double _value, boolean _flushNow) throws Exception {
    setValue (_name,_value);
  }


  public synchronized String getStringAS () throws Exception {
    return null;
  }

  public double getDoubleAS () {
    return 0;
  }

  public synchronized void synchronize(boolean _remove){
  }

  public synchronized void synchronize(){
  }

  public synchronized void externalVars(String _externalVars){
  }

  public synchronized void setCommand(String _command){
  }

  public synchronized void packageSize(double _package)  {
}

  public synchronized double[] getDoubleArrayAS () {
    return null;
  }

  public double[][] getDoubleArray2DAS () {
    return null;
  }


} // End of class

