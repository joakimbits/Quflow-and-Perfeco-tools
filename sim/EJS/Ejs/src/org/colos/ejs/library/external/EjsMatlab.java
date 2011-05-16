package org.colos.ejs.library.external;

import java.util.*;
import java.io.*;

import org.colos.ejs.library.Simulation;
import org.colos.ejs.unpackaged.EjsJMatLink;
import org.opensourcephysics.tools.*;
import java.lang.reflect.*; //Gonzalo 090610

public class EjsMatlab implements ExternalApp {
  final static private int EJS_ID_LENGTH = 4;
  final static private String EJS_ID = "%ejs";
  final static private String VAR_ID = "variable";
  final static private String PARAM_ID = "parameter";
  final static private String MODEL_ID = "model";
//  final static private String INPUT_ONLY_ID = "input";
//  final static private String OUTPUT_ONLY_ID = "output";

  static private boolean alreadyLoaded = false;
  static protected EjsJMatLink matlab=null;
  static private String homeDir=null;
  static private int numberOfEngines=0, dllCounter=-1;

  protected boolean needsToExtract=false; // true if any file is not in place. Then it will work in Simulations.getTemporaryDir()
  protected int id;   // The integer id for the Matlab engine opened
  protected String model;    // The complete name of the model
  protected String theModel; // The name of the model without directory or the '.mdl' extension
  protected String initCommand = null; // An optional initialization command required for the correct reset
//  protected String modelExtracted=null; // Gonzalo 060420

//  private boolean mFileExtracted=false; // Gonzalo 060420
//  private boolean startRequired = true; // Whether the simulation needs to be started
//  private boolean resetIC = false; // Whether to reset initial conditions of integrator blocks
//  private boolean resetParam=false;//// Gonzalo 060420
  protected String userDir=null; // The users working directory for this instance
  private String mFile;    // The mFile as input from the user (trimmed)
  private String theMFile=null; // The mFile as a command for Matlab workspace
  protected Vector<String> paramInfo = new Vector<String>(); // The list of parameters of the model
  private Vector<String> varInfo   = new Vector<String>(); // The list of variables of the model
  public String commandUpdate=" "; // Gonzalo 060420

  
  //Gonzalo 090610
  public Object varContextObject=null;
  public Class<?>  varContextClass =null;
  public java.util.Vector<String> linkVector=null;
  public Field[] varContextFields=null;
  public Field varContextField;
  public int[] linkIndex;
  public int[] linkType;
  static final int DOUBLE=0,ARRAYDOUBLE=1,ARRAYDOUBLE2D=2,STRING=3;
  
// --------------------------------------------------------
//  Static methods and constructors
// --------------------------------------------------------

  static private EjsJMatLink tryThisOne (String _dll) {
    System.out.println("path:"+_dll);
//    System.err.println ("Trying Matlab at "+_dll);
    Resource res = ResourceLoader.getResource(_dll);
    if (res!=null) {
      File file = res.getFile();
      if (file != null && file.exists()) {
//        System.err.println("Matlab found at " + _dll);
        return new EjsJMatLink(file.getAbsolutePath());
      }
    }
    return null;
  }

  /**
   * Initializes Matlab
   */
  static private void initMatlab() {
    numberOfEngines++;
    if (matlab!=null) return;
//    System.out.println ("Initing matlab ");
    homeDir = null;
    if (org.opensourcephysics.display.OSPRuntime.isWindows()) {
      matlab = tryThisOne(Simulation.getPathToLibrary()+"../_ejs_external/JMatLink.dll"); // Simulation directory
      if (matlab==null) { // Not found. Extract it to the user's home directory
//        System.out.println ("Trying to extract the dll...");
        homeDir = Simulation.getTemporaryDir();
        if (!alreadyLoaded) {
          dllCounter=0;
          for (int i=1; i<=50; i++) {
            File lib = new File (homeDir+"../_ejs_external/JMatLink"+i+".dll");
            if (lib.exists()) lib.delete(); // This cleans the directory of previous runs
            else if (dllCounter==0) {
              dllCounter = i;
              Simulation.extractResource ("../_ejs_external/JMatLink.dll",lib.getPath());
            }
          }
          alreadyLoaded = true;
        }
        matlab = tryThisOne(homeDir+"../_ejs_external/JMatLink"+dllCounter+".dll");
      }
    }
    else {
      String localDir = System.getProperty("user.dir").replace('\\','/');
      if (!localDir.endsWith("/")) localDir += "/";
      if (matlab==null) matlab = new EjsJMatLink(localDir+Simulation.getPathToLibrary()+"../_ejs_external/libJMatLink.jnilib");
//        System.out.println ("Start command is "+System.getProperty("Ejs.MatlabCmd"));
//        System.out.println ("java.library.path is "+System.getProperty("java.library.path"));
    }
  }

  /**
   * Package-internal constructor
   */
  EjsMatlab () {
          
  }

  /**
   * Creates a Matlab session.
   * @param _mFile The mFile to use to get information about variables,
   * parameters or Simulink model. Can be empty (i.e. "") if no information is needed.
   */
  public EjsMatlab (String _mFile) {
    
    this();
/*
    _mFile = _mFile.trim().replace('\\','/');
    if (_mFile.startsWith("<matlab")) { // remove any trailing <matlab> keyword
      int index = _mFile.indexOf('>');
      if (index>0) _mFile = _mFile.substring(index+1).trim();
      else _mFile = "";
    }
    mFile = _mFile;
//    System.err.println("Matlab inited");
    if (mFile.length()==0) return;

    if (needsToExtract || ! new File (userDir+mFile).exists()) {  // Extract the mFile using the Class.loader
      needsToExtract = true;
      userDir = Simulation.getTemporaryDir();
      matlab.engEvalString (id,"cd ('" + userDir + "')");
      if (Simulation.extractResource(mFile,userDir+mFile)==null) {
        System.out.println("Warning : the M-file "+mFile+" does not exist!");
        mFile = theMFile = null;
        return;
      }
    }
    
    // If the mFile is aDir/aSecondDir/myFile.m ...
    // ... theMFile = myFile
    // ... cd to aDir/aSecondDir which becomes the userDir
    String localDir="";
    int index = _mFile.lastIndexOf('/');
    if (index>=0) { localDir = _mFile.substring (0,index+1); _mFile = _mFile.substring (index+1); }
    index = _mFile.lastIndexOf('.');
    if (index>=0) theMFile = _mFile.substring (0, index);
    else theMFile = mFile;
    if (localDir.length()>0) matlab.engEvalString(id, "cd ('" + localDir + "')");

    // Process the  M file
    processMFile(userDir + mFile);

    if (model!=null) { // Make sure the model file is in place
      if (needsToExtract || ! new File(userDir+localDir+model).exists()) { // The model file must be in the same place as the m file
        if (Simulation.extractResource(localDir+model,userDir+localDir+model)==null) {
          System.out.println("Warning : the Simulink file "+model+" does not exist!");
          model = null;
        }
      }
    }
    openModel ();
    if (theMFile!=null) matlab.engEvalString (id,theMFile);
//    matlab.setDebug(true);
 */
  }

  public String toString () { return mFile; }

// --------------------------------------------------------
//  Implementation of ExternalApp
// --------------------------------------------------------
  // Gonzalo 090610 
  public String connect (){   
    if (matlab==null){
      initMatlab();
      needsToExtract = (homeDir!=null);
      if (org.opensourcephysics.display.OSPRuntime.isWindows()) id = matlab.engOpenSingleUse(); // Open a Matlab session
      else id = matlab.engOpen(System.getProperty("Ejs.MatlabCmd")); // Open a Matlab session. engOpenSingleUse does not work on Unix

      // Change Matlab to the working directory
      if (needsToExtract) userDir = homeDir;
      else {
        userDir = System.getProperty("user.dir").replace('\\','/');
        if (!userDir.endsWith("/")) userDir += "/";
      }
      matlab.engEvalString (id,"cd ('" + userDir + "')");
    }
    return connectionOK;
  }

  public void disconnect (){    
    if (matlab!=null){
      stop();    
        if (model!=null) matlab.engEvalString (id,"bdclose ('all')");
      matlab.engClose(id);
      matlab=null;
    }
   }

 public boolean isConnected(){ return (id!=-1 && matlab!=null); }
  
  // Gonzalo 090610 
  public boolean setVarContext(Object _object){
    if (_object==null) return (false);
    varContextObject=_object; 
    varContextClass =_object.getClass();
    varContextFields=varContextClass.getFields();
    return (true);
  }
  
  // Gonzalo 090610
  public boolean linkVariables(String ivar, String evar){
    if (varContextObject==null) return (false);               
    int type;
    //Search if the ivar exists
    for (int i=0; i < varContextFields.length; i++) {
      
        if (ivar.equals((varContextFields[i]).getName())) {
          //Detect type
          if (varContextFields[i].getType().getName().equals("double")) type=DOUBLE;
            else if (varContextFields[i].getType().getName().equals("[D")) type=ARRAYDOUBLE;
          else if (varContextFields[i].getType().getName().equals("[[D")) type=ARRAYDOUBLE2D;
          else if (varContextFields[i].getType().getName().equals("java.lang.String")) type=STRING;
          else return (false);
                                             
            if (linkVector==null) {
          linkVector=new java.util.Vector<String>();
          linkIndex= new int[1];
          linkIndex[0]=i;
          linkType = new int[1]; 
          linkType[0]=type;         
        }else{
          int[] _linkIndex=new int[linkIndex.length+1];
            System.arraycopy(linkIndex,0,_linkIndex,0,linkIndex.length);
            _linkIndex[linkIndex.length]=i; 
          linkIndex=_linkIndex;
          int[] _linkType=new int[linkType.length+1];
            System.arraycopy(linkType,0,_linkType,0,linkType.length);
            _linkType[linkType.length]=type;  
          linkType=_linkType;         
        }
        
        linkVector.addElement(ivar+";"+evar);
        return (true);        
      }
    }       
    return(false);
  } 
  
  
  public void setClient (ExternalClient _cli) { }

  public void setInitCommand (String _command) {
    initCommand = _command.trim();
    initialize();
  }

  public synchronized void eval (String _command) {
    if (matlab==null) return;
    matlab.engEvalString (id,_command);
  }

  public synchronized void eval(String _command, boolean _flushNow) { // Gonzalo 060420
   eval (_command);
  }

  public void resetIC () { 
//    resetIC = true; 
  }

  public void resetParam () { // Gonzalo 060420
//     resetParam = true;
  };

  //Gonzalo 090611
  public synchronized void step (double dt) {    
    int k=0;
    String var,evar;
    
    //Set Values   
    for (int i=0; i<linkVector.size(); i++){
         var= linkVector.elementAt(i);
       //ivar=var.substring(0,var.indexOf(";"));
       evar=var.substring(var.indexOf(";")+1,var.length());
       try {       
         varContextField= varContextFields[linkIndex[k]];      
         switch (linkType[k++]){
         case DOUBLE: setValue(evar,varContextField.getDouble(varContextObject)); break;
         case ARRAYDOUBLE: setValue(evar,(double[])varContextField.get(varContextObject)); break;
         case ARRAYDOUBLE2D: setValue(evar,(double[][])varContextField.get(varContextObject)); break;
         case STRING: setValue(evar,(String)varContextField.get(varContextObject)); break; 
         }
         
       } catch (java.lang.IllegalAccessException e) {
         System.out.println("Error Step: setting a value " + e);
       }           
    }
    
    //Do Step  
    int steps=(int)dt;
    for (int i=0;i<steps;i++)
        eval(commandUpdate,false); 
    
    //Get Values   
    k=0;         
    for (int i=0; i<linkVector.size(); i++){
         var= linkVector.elementAt(i);
       //ivar=var.substring(0,var.indexOf(";"));
       evar=var.substring(var.indexOf(";")+1,var.length());
       try {       
         varContextField= varContextFields[linkIndex[k]];      
         switch (linkType[k++]){
         case DOUBLE: varContextField.setDouble(varContextObject,getDouble(evar)); break;
         case ARRAYDOUBLE: varContextField.set(varContextObject,getDoubleArray(evar));  break;
         case ARRAYDOUBLE2D: varContextField.set(varContextObject,getDoubleArray2D(evar)); break;
         case STRING: varContextField.set(varContextObject,getString(evar)); break;        
         }
         
       } catch (java.lang.IllegalAccessException e) {
         System.out.println("Error Step: getting a value " + e);
       }           
    }         
 }
  
  
  
  
/**
 * Step the Simulink model a number of times.
 * @param dt The number of (integer) steps
 */  
  /*
  public synchronized void step (double dt) {
//    System.out.println ("Step for "+this.toString());
    //if (theModel==null) return;
    if (theModel==null) {// Gonzalo 060420
      stepSYN(dt);
      return;
    }

    if (resetIC) {
      matlab.engEvalString (id,"Ejs__ResetIC = 1 - Ejs__ResetIC");
      startRequired = true;
      resetIC = false;
    }

    if (resetParam){// Gonzalo 060420
      matlab.engEvalString (id,"set_param('"+theModel+"','SimulationCommand','update')");
      resetParam=false;
    }

    if (startRequired) {
      startRequired = false;
      matlab.engEvalString (id,"set_param ('"+theModel+"', 'SimulationCommand','Start')");
      matlab.engEvalString(id,"EjsSimulationStatus='unknown'"); // 051123 Lausanne (This was inside the loop)
      int max=10;
      while (max>0) { // Wait 10 times for Simulink to complete the step!
//        matlab.engEvalString(id,"clear EjsSimulationStatus"); // 051123 Lausanne
        String status = waitForString (matlab,id,
          "EjsSimulationStatus","EjsSimulationStatus=get_param('"+theModel+"','SimulationStatus')");
        if ("paused".equals(status) || "stopped".equals(status)) break;
        max--;
      }
    }
    for (int i=0,times=(int) dt; i<times; i++) {

//      matlab.engEvalString (id,"Ejs_Done=0");
      matlab.engEvalString (id,"set_param ('"+theModel+"', 'SimulationCommand','Continue')");
//      while (matlab.engGetScalar(id,"Ejs_Done")==0); // Wait for Simulink to really continue!
//    This seems to make Ejs_Done unnecessary
      matlab.engEvalString(id,"EjsSimulationStatus='unknown'"); // 051123 Lausanne (This was inside the loop)
      int max = 10;
      while (max>0) { // Wait 10 times for Simulink to complete the step!
//        matlab.engEvalString(id,"clear EjsSimulationStatus"); // 051123 Lausanne
        String status = waitForString (matlab,id,"EjsSimulationStatus","EjsSimulationStatus=get_param('"+theModel+"','SimulationStatus')");
//        System.out.println("Status = "+status);
        if ("paused".equals(status) || "stopped".equals(status)) break;
        max--;
      }
    }
  }
*/
  
  public  void stepSYN (double dt){// Gonzalo 060420
    int steps=(int)dt;
    for (int i=0;i<steps;i++)
      eval(commandUpdate);
  }

  //Asynchronous Remote Simulink
  public  void stepAS (double dt){} // Gonzalo 060420
  public  void stepAS (double dt, int _package){} // Gonzalo 060420

  public synchronized void reset() {
    stop();
    if (model!=null) matlab.engEvalString (id,"bdclose ('all')");
    matlab.engEvalString (id,"clear all");
    openModel ();
    if (theMFile!=null) matlab.engEvalString (id,theMFile);
  }

  public synchronized void quit() {
//    System.out.println ("exiting matlab ");
    stop();
    if (matlab==null) return;
    if (model!=null) matlab.engEvalString (id,"bdclose ('all')");
    matlab.engClose(id);
//    System.out.println ("Quiting engine number "+numberOfEngines);
    numberOfEngines--;
    if (numberOfEngines<=0) {
      matlab = null;
      numberOfEngines = 0;
    }
  }

// --------------------------------------------------------------
// Accessing the variables using EjsMatlabInfo as identifier
// --------------------------------------------------------------

  //public synchronized void setValue (String _name, String _value) throws Exception {
  public synchronized void setValue (String _name, String _value)  { //Gonzalo 060611  
    if (matlab==null) return;
    matlab.engEvalString (id,_name + "= [" + _value + "]");
    setParameter (_name); // In case it is a parameter
  }

  //public synchronized void setValue (String _name, double _value) throws Exception { //Gonzalo 090611
  public synchronized void setValue (String _name, double _value)  {
    if (matlab==null) return;
    matlab.engPutArray(id, _name, _value);
    setParameter(_name); // In case it is a parameter
  }

//  public synchronized void setValue (String _name, double[] _value) throws Exception {
  public synchronized void setValue (String _name, double[] _value)  { //Gonzalo 090611
    if (matlab==null) return;
    matlab.engPutArray (id,_name,_value);
    setParameter (_name); // In case it is a parameter
  }

 // public synchronized void setValue (String _name, double[][] _value) throws Exception {
   public synchronized void setValue (String _name, double[][] _value) {    
    if (matlab==null) return;
    matlab.engPutArray (id,_name,_value);
    setParameter (_name); // In case it is a parameter
  }

  //public synchronized String getString (String _variable) throws Exception { // Strongly inspired in JMatLink
   public synchronized String getString (String _variable)  { // Strongly inspired in JMatLink    //Gonzalo 090611
    if (matlab==null) return "";
    matlab.engEvalString(id,"EjsengGetCharArrayD=double(" + _variable +")" );
    double[][] arrayD = matlab.engGetArray(id,"EjsengGetCharArrayD");
    if (arrayD==null) return null;
    matlab.engEvalString(id,"clear EjsengGetCharArrayD");
    String value[] = double2String(arrayD);
    if (value.length<=0) return null;
    return value[0];
  }

  public synchronized double getDouble (String _variable) {
    if (matlab==null) return 0.0;
    return matlab.engGetScalar(id,_variable);
  }

  public synchronized double[] getDoubleArray (String _variable) {
    if (matlab==null) return null;
    double[][] returnValue = matlab.engGetArray(id,_variable);
    return returnValue[0];
  }

  public synchronized double[][] getDoubleArray2D (String _variable) {
    if (matlab==null) return null;
    return matlab.engGetArray(id,_variable);
  }



  // Gonzalo 060420
  public synchronized void update(String _command,String _outputvars,int _steps){
  }
  // Gonzalo 060420
  public synchronized void update(String _command,String _outputvars,int _steps,int _package){
  }
  // Gonzalo 060420
  public synchronized void haltUpdate(boolean _remove){
  }
  // Gonzalo 060420
  public synchronized void haltStepAS(boolean _remove){
  }
  // Gonzalo 060420
  public void setValue (String _name, double[][] _value, boolean _flushNow) throws Exception {
    setValue (_name, _value);
  }
  // Gonzalo 060420
  public void setValue (String _name, double[] _value, boolean _flushNow) throws Exception {
    setValue (_name, _value);
  }
  // Gonzalo 060420
  public synchronized void setValue (String _name, String _value, boolean _flushNow) throws Exception {
    setValue (_name, _value);
  }
  // Gonzalo 060420
  public void setValue (String _name, double _value, boolean _flushNow) throws Exception {
    setValue (_name,_value);
  }
  // Gonzalo 060420
  public synchronized String getStringAS () throws Exception {
    return null;
  }
  // Gonzalo 060420
  public synchronized void externalVars(String _externalVars){
  }
  // Gonzalo 060420
  public synchronized void setCommand(String _command){
    synchronize(true);
    commandUpdate=_command;
  }
  // Gonzalo 060420
  public double getDoubleAS () {
    return 0;
  }
  // Gonzalo 060420
  public synchronized double[] getDoubleArrayAS () {
    return null;
  }
  // Gonzalo 060420
  public double[][] getDoubleArray2DAS () {
    return null;
  }
  // Gonzalo 060420
  public synchronized void packageSize(double _package)  {
  }
  // Gonzalo 060420
  public synchronized void synchronize(boolean _remove){
    // resetIC(); Gonzalo 060426 To avoid reset using setValues, getValues, etc. in an evolution page with a Simulink Model.
   // resetParam(); Gonzalo 060426 To avoid reset using setValues, getValues, etc. in an evolution page with a Simulink Mod
  }
  // Gonzalo 060420
  public synchronized void synchronize(){
    // resetIC(); Gonzalo 060426 To avoid reset using setValues, getValues, etc. in an evolution page with a Simulink Model.
   // resetParam(); Gonzalo 060426 To avoid reset using setValues, getValues, etc. in an evolution page with a Simulink Mod
  }

// --------------------------------------------------------
//  Private or protected methods
// --------------------------------------------------------

  protected synchronized void openModel () {
//    System.out.println ("Opening model = "+theModel);
    if (model==null) return;
    matlab.engEvalString (id,"open_system ('" + theModel +"')");
    matlab.engEvalString (id,"set_param ('"+theModel+"', 'Open','Off')");
    matlab.engEvalString (id,"set_param ('"+theModel+"', 'SimulationCommand','Stop')");
    matlab.engEvalString (id,"set_param ('"+theModel+"', 'StopTime','inf')");
    createEjsSubsystem ();
    initialize();
  }

  protected synchronized void initialize () {
    if (initCommand!=null) matlab.engEvalString (id,initCommand);
  }

  protected synchronized void createEjsSubsystem () { };

  // If _name is a parameter then we must change the Simulink block parameter
  // too, not just the value in the workspace.
  protected void setParameter (String _name) {
    if (model==null) return;
    int pos = paramInfo.indexOf (_name);
    if (pos>= 0) {
      // Get the command which has been previously prepared... (see processMFile below)
      // We didn't write it completely (i.e. including the model)
      // because the model can be specified on the M file after the parameter is declared.
//      System.out.println ("Command = set_param ('"+theModel+paramInfo.elementAt(pos+1));
      matlab.engEvalString (id,"set_param ('"+theModel+paramInfo.elementAt(pos+1));
    }
  }

  /**
   * Stop the Simulink model
   */
  protected synchronized void stop () {
//    startRequired = true;
    if (matlab!=null && theModel!=null)
      matlab.engEvalString (id,"set_param ('"+theModel+"', 'SimulationCommand', 'Stop')"); // This makes Simulink beep
  }

 // Note: any change in this method must match the method readMFile in ReadExternal
  @SuppressWarnings("unused")
  private void processMFile (String _filename) {
    int a,pos;
    String line = new String();
    String lineLowercase;
    StringTokenizer aux;
    try {
      FileReader reader = new FileReader(_filename);
      // System.out.println ("Processing mFile = "+ _filename);
      BufferedReader in = new BufferedReader(reader);
      while((line = in.readLine())!= null) {
        lineLowercase = line.toLowerCase().trim();
        a = lineLowercase.lastIndexOf(EJS_ID);
        if (a > 0) {
          lineLowercase = lineLowercase.substring(a+EJS_ID_LENGTH);
          if (lineLowercase.lastIndexOf(VAR_ID)>0) {  //It's a variable
            // Get the name. Variable has no data which is relevant at this point
            aux = new StringTokenizer (line, "=");
            varInfo.addElement (new String (aux.nextToken().trim()));
          }
          else if ((pos=lineLowercase.lastIndexOf(PARAM_ID))>0) { // It's a parameter
            // Get the name
            aux = new StringTokenizer (line, "=");
            String paramName = new String (aux.nextToken().trim());
            // Get parameter's instruction data
            line = line.substring(pos+a+EJS_ID_LENGTH).trim();
            pos = line.indexOf(' ');
            if (pos>=0) line = line.substring(0,pos); // separate Parameter instruction from other data (if any)
            aux = new StringTokenizer (line, "=:");
            String paramCommand = null;
            switch (aux.countTokens()) {
              case 2 : aux.nextToken (); // ignore 'Parameter' keyword
                       paramCommand = "', '"+aux.nextToken().trim()+"'";
                       break;
              case 3 : aux.nextToken (); // ignore 'Parameter' keyword
                       paramCommand = "/"+aux.nextToken().trim()+"', '"+aux.nextToken().trim()+"'";
                       break;
            }
            if (paramCommand!=null) {
              paramInfo.addElement (paramName); // name
              paramInfo.addElement (new String (paramCommand+",'"+paramName + "')"));
            }
            else System.out.println ("Error: Parameter syntax error in file " + mFile+ ". Line :"+line);
          }
          else if (lineLowercase.lastIndexOf(MODEL_ID)>0) { // It's a model
            // Get the model's name. No extra data
            aux = new StringTokenizer (line, "= ;");
            aux.nextToken (); // ignore model's variable name
            model = aux.nextToken().trim();
            model = model.substring(1, model.length()-1); // remove quotes
            theModel = model.substring(0,model.lastIndexOf('.'));
          }
          else System.out.println ("Warning: Syntax incomplete in file " + mFile+ ". Ignoring line :"+line);
        }
      }
      in.close();
    } catch (Exception exc) { exc.printStackTrace(); }
  }

// ------------------------------
//   Static utilities
// ------------------------------

  /**
   * This queries Matlab until a given String is returned
   * The string must exist (sooner or later) in Matlab or the computer will hang
   * Strongly inspired in JMatLink
   */
  static public synchronized String waitForString (EjsJMatLink _matlab, int _id, String _variable, String _command) {
    String request = "EjsGet"+_variable;
    double[][] arrayD=null;
    if (_command!=null) _matlab.engEvalString(_id,_command); // 051123 Lausanne. This was before inside the loop
    _matlab.engEvalString(_id,request+"=double(" + _variable +")" ); // 051123 Lausanne. This was before inside the loop
    int max=10;
    do {
      arrayD = _matlab.engGetArray(_id,request);
      max--;
//      System.out.println("Counter = "+counter);
    } while (arrayD==null && max>0); // A maximum of 10 times

    if (arrayD==null) return null;  //// Gonzalo 060420
    _matlab.engEvalString(_id,"clear "+request);
    String value[] = double2String(arrayD);
    if (value.length<=0) return null;
    return value[0];
  }

  static public String[] double2String(double[][] d) // Borrowed from JMatLink
  {
      String encodeS[]=new String[d.length];  // String vector

      // for all rows
      for (int n=0; n<d.length; n++){
          byte b[] = new byte[d[n].length];
          // convert row from double to byte
          for (int i=0; i<d[n].length ;i++) b[i]=(byte)d[n][i];

          // convert byte to String
          try { encodeS[n] = new String(b, "UTF8");}
          catch (UnsupportedEncodingException e) { e.printStackTrace(); }
      }
      return encodeS;
  } // end double2String

} // End of class
