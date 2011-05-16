package org.colos.ejs.library.external;

import java.io.*;
import java.lang.reflect.Field;
import java.net.*;
import java.util.*;
import org.colos.ejs.unpackaged.*;

public class EjsRemoteMatlab implements ExternalApp {
//  final static private int EJS_ID_LENGTH = 4;
//  final static private String EJS_ID = "%ejs";
//  final static private String VAR_ID = "variable";
//  final static private String PARAM_ID = "parameter";
//  final static private String MODEL_ID = "model";
//  final static private String INPUT_ONLY_ID = "input";
//  final static private String OUTPUT_ONLY_ID = "output";

  static protected EjsJMatLink matlab = null;
  static protected String userDir = null;

  protected boolean needsToExtract=false; // true if any file is not in place. Then it will work in Simulations.getTemporaryDir()
  protected int id; // The intger id for the Matlab engine opened
  protected String model; // The complete name of the model
  protected String theModel; // The name of the model without directory or the '.mdl' extension
  protected String initCommand = null; // An optional initialization command required for the correct reset
//  protected String modelExtracted = null;

//  private boolean mFileExtracted = false;
//  private boolean startRequired = true; // Whether the simulation needs to be started
//  private boolean resetIC = false; // Whether to reset initial conditions of integrator blocks
  private String mFile; // The mFile as input from the user (trimmed)
  private String theMFile = null; // The mFile as a command for Matlab workspace
  protected Vector<String> paramInfo = new Vector<String>(); // The list of parameters of the model
//  private Vector<String> varInfo = new Vector<String>(); // The list of variables of the model

  public Socket jimTCP;
  public DataInputStream bufferInputTCP;
  public DataOutputStream bufferOutputTCP;
  public int SERVICE_PORT;
  public String SERVICE_IP;
  public int EjsId;
  public String commandUpdate=" ";
  public boolean removeBuffer=true;
  public boolean asynchronousSimulation=false;
  public String externalVars="";
  public boolean modeAS=true;
  public int packageSize=1;
  public boolean connected=false;
 
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

  /**
   * Package-internal constructor
   */

  /**
   * Creates a Matlab session.
   * @param _mFile The mFile to use to get information about variables,
   * parameters or Simulink model. Can be empty (i.e. "") if no information is needed.
   */
  public EjsRemoteMatlab(String _mFile) {
    
    EjsId = 2005;

    _mFile=_mFile.trim().toLowerCase().replace("<","").replace(">","");
    String[] params=_mFile.split(":");
    
    if (params.length!=3) {
      System.out.println("Error of Input Arguments");
      System.out.println("------------------------");
      System.out.println("Arguments for synchronous mode = \n <matlab:ipAddress:ipPort>");
      System.out.println("Arguments for asynchronous mode = \n <matlabas:ipAddress:ipPort>");
      return;
    }
    if (params[0].equals("matlabas")) modeAS=true; //Gonzalo 090519
    String sAdress=params[1];
    String sPort=params[2];
    SERVICE_IP = sAdress;
    try {
      SERVICE_PORT = Integer.parseInt(sPort);
    }
    catch (NumberFormatException nfe) {
      System.out.println("Error in Port:" + nfe);
    }
    /*

    EjsId = 2005;

    String sPort = _mFile.substring(_mFile.lastIndexOf(':') + 1,                                      _mFile.lastIndexOf('>'));
    String sAdress = _mFile.substring(10, _mFile.lastIndexOf(':'));

    if (_mFile.startsWith("<matlab:"))
    {
      modeAS=false;
       sPort = _mFile.substring(_mFile.lastIndexOf(':') + 1,_mFile.lastIndexOf('>'));
       sAdress = _mFile.substring(8, _mFile.lastIndexOf(':'));
    }

    SERVICE_IP = sAdress;
    try {
      SERVICE_PORT = Integer.parseInt(sPort);
    }
    catch (NumberFormatException nfe) {
      System.out.println("Error in Port:" + nfe);
    }

    try {
      jimTCP = new java.net.Socket(SERVICE_IP, SERVICE_PORT);
      bufferInputTCP = new DataInputStream(new BufferedInputStream(jimTCP.getInputStream()));
      bufferOutputTCP = new DataOutputStream(new BufferedOutputStream(jimTCP.getOutputStream()));
      jimTCP.setSoTimeout(10000);
      jimTCP.setTcpNoDelay(true);
      System.out.println("Successful Connection");
    }
    catch (IOException ioe) {
      System.err.println("Error " + ioe);
    }
    catch (Exception e2) {
      System.err.println("Error " + e2);
    }

    mFile = new String(_mFile.trim());
    if (mFile.length() == 0)return;

/* gonzalo 060420
    if (needsToExtract || ! new File (userDir+mFile).exists()) {  // Extract the mFile using the Class.loader
      needsToExtract = true;
      userDir = Utils.getTemporaryDir();
      matlab.engEvalString (id,"cd ('" + userDir + "')");
      if (!Utils.extractResource(mFile,userDir+mFile)) {
        System.out.println("Warning : the M-file "+mFile+" does not exist!");
        mFile = theMFile = null;
        return;
      }
    }
  */  // gonzalo 060420
 /*  // gonzalo 060420
  // If the mFile is aDir/aSecondDir/myFile.m ...
    // ... theMFile = myFile
    // ... cd to aDir/aSecondDir which becomes the userDir
    String localDir="";
    int index = _mFile.lastIndexOf('/');
    if (index>=0) { localDir = _mFile.substring (0,index+1); _mFile = _mFile.substring (index+1); }
    index = _mFile.lastIndexOf('.');
    if (index>=0) theMFile = _mFile.substring (0, index);
    else theMFile = _mFile;
    if (localDir.length()>0) matlab.engEvalString(id, "cd ('" + localDir + "')");

    // Process the  M file
    processMFile(userDir + mFile);

    if (model!=null) { // Make sure the model file is in place
      if (needsToExtract || ! new File(userDir+localDir+model).exists()) { // The model file must be in the same place as the m file
        if (!Utils.extractResource(localDir+model,userDir+localDir+model)) {
          System.out.println("Warning : the Simulink file "+model+" does not exist!");
          model = null;
        }
      }
    }

    openModel();
    if (theMFile != null) matlab.engEvalString(id, theMFile);

 */ //gonzalo 060420
//    matlab.setDebug(true);
    
    
  }

  public String toString() {
    return mFile;
  }

// --------------------------------------------------------
//  Implementation of ExternalApp
// --------------------------------------------------------
  public String connect (){    
    String result=connect("",""); 
    if (result.equals(connectionAuthenticationFail) || result.equals(connectionSlotFail) )
      return connectionAuthenticationRequired;
    return result;
   }

    public String connect (String user, String pwd){
        
      asynchronousSimulation=false;
      
      int result=-1;

      if (jimTCP!=null){
        if (!jimTCP.isClosed()) return connectionOK; //user ok
      }
      try {
        jimTCP = new java.net.Socket(SERVICE_IP, SERVICE_PORT);
        bufferInputTCP = new DataInputStream(new BufferedInputStream(jimTCP.getInputStream()));
        bufferOutputTCP = new DataOutputStream(new BufferedOutputStream(jimTCP.getOutputStream()));
        jimTCP.setSoTimeout(connectionTimeOut);
        jimTCP.setTcpNoDelay(true);

        //Authentication
        bufferOutputTCP.writeUTF(user);
        bufferOutputTCP.writeUTF(pwd);
        bufferOutputTCP.flush();
        result=bufferInputTCP.readInt();
//        long rtime=
          bufferInputTCP.readLong();
        if (result==2) {
          System.out.println("Successful Connection");
          //System.out.println("Time available:"+(rtime/(1000*60))+" minutes");
          connected = true;
          return connectionOK;
        }
          System.out.println("Authentication Error");        
          disconnect();
          if (result==0) return connectionAuthenticationFail;
          return connectionSlotFail;
      }
      catch (IOException ioe) {
        System.out.println("Error " + ioe);
        connected=false;
        return connectionNoServer;
      }
      catch (Exception e2) {
        System.out.println("Error " + e2);
        connected=false;
        return connectionNoServer;
      }
    }

    public void disconnect (){
    connected=false;
    if (jimTCP!=null){    
      try {
          jimTCP.close();
      }catch (IOException ioe){System.out.println("Error " + ioe);
      }catch (Exception e2){System.out.println("Error " + e2);
      }
    }
  }

  public  synchronized long getRemainingTime(){
    long result=0;
    try {
      bufferOutputTCP.writeInt(EjsId);
      bufferOutputTCP.writeUTF("getRemainingTime");
      bufferOutputTCP.flush();
      result=bufferInputTCP.readLong();
    }
    catch (Exception e) {
      System.out.println(" getRemainingTime Exception:" + e);
    }
    return result;
  }

  public boolean isConnected(){
        return connected;
    }
  
  // Gonzalo 090507
  public boolean setVarContext(Object _object){
    if (_object==null) return (false);
    varContextObject=_object; 
    varContextClass =_object.getClass();
    varContextFields=varContextClass.getFields();
    return (true);
  }
  
  // Gonzalo 090507
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
        
      if (externalVars.equals("")) externalVars=evar;
      else externalVars=externalVars+","+evar;
        
        linkVector.addElement(ivar+";"+evar);
        return (true);        
      }
    }       
    return(false);
  }
  

  public void setClient(ExternalClient _cli) {}

  public void setInitCommand(String _command) {
    initCommand = _command.trim();
    initialize();
  }

  public synchronized void eval(String _command) {
    synchronize(false);
    try {
      bufferOutputTCP.writeInt(EjsId);
      bufferOutputTCP.writeUTF("eval");
      bufferOutputTCP.writeUTF(_command);
      bufferOutputTCP.flush();
    }
    catch (Exception e) {
      System.out.println(" eval Remote Exception:" + e);
    }
  }

  public synchronized void eval(String _command, boolean _flushNow) {
    synchronize(false);
    try {
      bufferOutputTCP.writeInt(EjsId);
      bufferOutputTCP.writeUTF("eval");
      bufferOutputTCP.writeUTF(_command);
      if (_flushNow)
        bufferOutputTCP.flush();
    }
    catch (Exception e) {
      System.out.println(" eval Remote Exception:" + e);
    }

  }

  public void resetIC() {
//    resetIC = true;
  }

  public void resetParam () {
  };


  /**
   * Step the Simulink model a number of times.
   * @param dt The number of (integer) steps
   */

  public synchronized void externalVars(String _externalVars){
    externalVars=_externalVars;
  }
  
  //------------
  public void setValues(){
    
    int k=0;
    String var,evar;
    
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
   }   
      
   public void getValues(){
    //Get Values   
    int k=0;
    String var,evar;
            
    for (int i=0; i<linkVector.size(); i++){
         var=  linkVector.elementAt(i);
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
      
      
      
   public  void stepAS (double dt){  
         
     if (!asynchronousSimulation){
        
       //Set Values  
       setValues();
     
       int steps=(int)dt;
       haltUpdate(removeBuffer);

       try{

         //Escribe Identificador de EJS
         bufferOutputTCP.writeInt (EjsId);

         //Escribe identificador de Funcion Remota
         bufferOutputTCP.writeUTF ("stepMatlabAS");

         //Escribe las variables a leer
         bufferOutputTCP.writeUTF (externalVars);

         //Escribe el comando a ejecutar
         bufferOutputTCP.writeUTF (commandUpdate);

         //Escribe los pasos a ejecutar
         bufferOutputTCP.writeInt (steps);

         //Escribe el tamaño del paquete que retorna a Ejs
         bufferOutputTCP.writeInt(packageSize);

         //Envia bloque de datos a JIM
         bufferOutputTCP.flush();

       }catch (Exception e) {
         System.out.println("Remote Step Asynchronous Exception:"+e);
       }
       asynchronousSimulation=true;
     }
     
     //Get Values
     getValues();
   }


   public  void step (double dt){

     if (modeAS)
       stepAS(dt);
     else{
        stepSincronized(dt);
     }
   }

 public synchronized void stepSincronized (double dt) {
   
    //Set Values 
    setValues();
    
    //Do Step  
    int steps=(int)dt;
    for (int i=0;i<steps;i++)
        eval(commandUpdate,false); 
    
    //Get Values   
    getValues();        
 }


     //Asynchronous Remote Simulink
   public  void stepAS (double dt, int _package){}

   public synchronized void haltStepAS(boolean _remove){
   }

   public synchronized void reset() {
     stop();
     if (model != null) eval("bdclose ('all')");
     eval("clear all");
     openModel();
     if (theMFile != null) eval(theMFile);
   }

   public synchronized void quit()  {
     stop();
     eval("bdclose ('all')",false);

     try {
       bufferOutputTCP.writeInt(EjsId);
       bufferOutputTCP.writeUTF("exit");
       bufferOutputTCP.flush();
       bufferOutputTCP.close();
       bufferInputTCP.close();
       jimTCP.close();

       //stop();
       //if (model != null) matlab.engEvalString(id, "bdclose ('all')");
       //matlab.engClose(id);
       //if (modelExtracted != null)new File(modelExtracted).delete();
       //if (mFileExtracted)new File(mFile).delete();

     }catch(Exception e){
       System.out.println("Error closing Remote Matlab "+e);
     }
   }
   
  //-----------
  
   /*
   public  void stepAS (double dt){
    if (!asynchronousSimulation){
      int steps=(int)dt;
      haltUpdate(removeBuffer);

      try{

        //Escribe Identificador de EJS
        bufferOutputTCP.writeInt (EjsId);

        //Escribe identificador de Funcion Remota
        bufferOutputTCP.writeUTF ("stepMatlabAS");

        //Escribe las variables a leer
        bufferOutputTCP.writeUTF (externalVars);

        //Escribe el comando a ejecutar
        bufferOutputTCP.writeUTF (commandUpdate);

        //Escribe los pasos a ejecutar
        bufferOutputTCP.writeInt (steps);

        //Escribe el tamaño del paquete que retorna a Ejs
        bufferOutputTCP.writeInt(packageSize);

        //Envia bloque de datos a JIM
        bufferOutputTCP.flush();

      }catch (Exception e) {
        System.out.println("Remote Step Asynchronous Exception:"+e);
      }
      asynchronousSimulation=true;
    }
  }


  public  void step (double dt){

    if (modeAS)
      stepAS(dt);
    else{
      int steps=(int)dt;
      for (int i=0;i<steps;i++)
        eval(commandUpdate,false);
    }
  }


    //Asynchronous Remote Simulink
  public  void stepAS (double dt, int _package){}

  public synchronized void haltStepAS(boolean _remove){
  }

  public synchronized void reset() {
    stop();
    if (model != null) eval("bdclose ('all')");
    eval("clear all");
    openModel();
    if (theMFile != null) eval(theMFile);
  }

  public synchronized void quit()  {
    stop();
    eval("bdclose ('all')",false);

    try {
      bufferOutputTCP.writeInt(EjsId);
      bufferOutputTCP.writeUTF("exit");
      bufferOutputTCP.flush();
      bufferOutputTCP.close();
      bufferInputTCP.close();
      jimTCP.close();

      //stop();
      //if (model != null) matlab.engEvalString(id, "bdclose ('all')");
      //matlab.engClose(id);
      //if (modelExtracted != null)new File(modelExtracted).delete();
      //if (mFileExtracted)new File(mFile).delete();

    }catch(Exception e){
      System.out.println("Error closing Remote Matlab "+e);
    }
  }
*/
  public synchronized void packageSize(double _package)  {
    if (modeAS){
      synchronize(true);
      packageSize = (int) _package;
    }
  }

  public synchronized void synchronize(boolean _remove){
   removeBuffer=_remove;
   asynchronousSimulation=false;
   // resetIC();
   // resetParam();
   // haltUpdate(_remove);
  }

  public synchronized void synchronize(){
    removeBuffer=true;
    asynchronousSimulation=false;
    //resetIC();
    //resetParam();
    //haltUpdate(true);
  }


// --------------------------------------------------------------
// Accessing the variables using EjsMatlabInfo as identifier
// --------------------------------------------------------------
//+++++++++++++++SET++++++++++++++++++++

  public synchronized void setValue(String _name, String _value) {
     try {  
      if (!asynchronousSimulation){
         bufferOutputTCP.writeInt(EjsId);
         bufferOutputTCP.writeUTF("setValueString");
         bufferOutputTCP.writeUTF(_name);
         bufferOutputTCP.writeUTF(_value);
        bufferOutputTCP.flush();
        //  setParameter (_name); // In case it is a parameter
      }
     }catch(Exception e){
      System.out.println("Error closing Remote Matlab "+e);
    }
  }

  public synchronized void setValue(String _name, String _value,
                                    boolean _flushNow) {
    try {                                     
      bufferOutputTCP.writeInt(EjsId);
      bufferOutputTCP.writeUTF("setValueString");
      bufferOutputTCP.writeUTF(_name);
      bufferOutputTCP.writeUTF(_value);
      if (_flushNow)
        bufferOutputTCP.flush();
     }catch(Exception e){
      System.out.println("Error setValue Remote Matlab "+e);
    }
  }

  public synchronized void setValue(String _name, double _value) {
    try{  
      if (!asynchronousSimulation){
        bufferOutputTCP.writeInt(EjsId);
        bufferOutputTCP.writeUTF("setValueDouble");
        bufferOutputTCP.writeUTF(_name);
        bufferOutputTCP.writeDouble(_value);
        bufferOutputTCP.flush();
        //setParameter (_name); // In case it is a parameter
      }
    }catch(Exception e){
      System.out.println("Error setValue Remote Matlab "+e);
    }
      
  }

  public synchronized void setValue(String _variable, double _value, boolean _flushNow) {
     try{
        bufferOutputTCP.writeInt(EjsId);
        bufferOutputTCP.writeUTF("setValueDouble");
        bufferOutputTCP.writeUTF(_variable);
        bufferOutputTCP.writeDouble(_value);
        if (_flushNow)
          bufferOutputTCP.flush();
   }catch(Exception e){
      System.out.println("Error setValue Remote Matlab "+e);
    }
  }


  public synchronized void setValue(String _name, double[] _value) {
    try{
      if (!asynchronousSimulation){
        bufferOutputTCP.writeInt(EjsId);
        bufferOutputTCP.writeUTF("setValueD[]");
        bufferOutputTCP.writeUTF(_name);
        bufferOutputTCP.writeInt(_value.length);
        for (int i = 0; i < _value.length; i++) {
          bufferOutputTCP.writeDouble(_value[i]);
        }
        bufferOutputTCP.flush();
        //setParameter (_name); // In case it is a parameter
      }
     }catch(Exception e){
      System.out.println("Error setValue Remote Matlab "+e);
    }
  }

  public synchronized void setValue(String _name, double[] _value,
                                    boolean _flushNow) {
    try{
      bufferOutputTCP.writeInt(EjsId);
      bufferOutputTCP.writeUTF("setValueD[]");
      bufferOutputTCP.writeUTF(_name);
      bufferOutputTCP.writeInt(_value.length);
      for (int i = 0; i < _value.length; i++) {
        bufferOutputTCP.writeDouble(_value[i]);
      }
      if (_flushNow)
        bufferOutputTCP.flush();
        
        }catch(Exception e){
      System.out.println("Error setValue Remote Matlab "+e);
    }

  }

  public synchronized void setValue(String _name, double[][] _value) {
   try{
     if (!asynchronousSimulation){
       bufferOutputTCP.writeInt(EjsId);
       bufferOutputTCP.writeUTF("setValueD[][]");
       bufferOutputTCP.writeUTF(_name);

       bufferOutputTCP.writeInt(_value.length);
       bufferOutputTCP.writeInt(_value[0].length);
       for (int i = 0; i < _value.length; i++) {
         for (int j = 0; j < _value[0].length; j++) {
           bufferOutputTCP.writeDouble(_value[i][j]);
         }
       }
       bufferOutputTCP.flush();
       // setParameter (_name); // In case it is a parameter
     }
    }catch(Exception e){
      System.out.println("Error setValue Remote Matlab "+e);
    }
  }

  public synchronized void setValue(String _name, double[][] _value,
                                    boolean _flushNow) {
   try{                             
      bufferOutputTCP.writeInt(EjsId);
      bufferOutputTCP.writeUTF("setValueD[][]");
      bufferOutputTCP.writeUTF(_name);

      bufferOutputTCP.writeInt(_value.length);
      bufferOutputTCP.writeInt(_value[0].length);
      for (int i = 0; i < _value.length; i++) {
        for (int j = 0; j < _value[0].length; j++) {
          bufferOutputTCP.writeDouble(_value[i][j]);
        }
      }
      if (_flushNow)
        bufferOutputTCP.flush();
    }catch(Exception e){
      System.out.println("Error setValue Remote Matlab "+e);
    }
  }

//+++++++++++++++GET++++++++++++++++++++

   public synchronized String getString(String _variable) { // Strongly inspired in JMatLink

    if (asynchronousSimulation) return getStringAS();
    if (modeAS){
      haltUpdate(true);
    }

    String charArray = null;
   try{
    bufferOutputTCP.writeInt(EjsId);
    bufferOutputTCP.writeUTF("getString");
    bufferOutputTCP.writeUTF(_variable);
    bufferOutputTCP.flush();
    charArray = bufferInputTCP.readUTF();

   }catch(Exception e){
      System.out.println("Error getString Remote Matlab "+e);
    }
        return (charArray);
  }

// Get from Buffer
  public synchronized String getStringAS() {
    String charArray = null;
    try{
     charArray = bufferInputTCP.readUTF();
    }catch(Exception e){
      System.out.println("Error getStringAS Remote Matlab "+e);
    }   
    return (charArray);
  }

  public synchronized double getDouble(String _variable)  {
    if (asynchronousSimulation) return getDoubleAS();

    if (modeAS){
      haltUpdate(true);
    }

    double valueDouble = 0.0;
   try{
    bufferOutputTCP.writeInt(EjsId);
    bufferOutputTCP.writeUTF("getDouble");
    bufferOutputTCP.writeUTF(_variable);
    bufferOutputTCP.flush();
    valueDouble = bufferInputTCP.readDouble();
   }catch(Exception e){
      System.out.println("Error getDouble Remote Matlab "+e);
    }   
    return (valueDouble);

  }

// Get from Buffer
 public synchronized double getDoubleAS() {
   double valueDouble=0.0;
   try{
    valueDouble= bufferInputTCP.readDouble();
     }catch(Exception e){
      System.out.println("Error getDoubleAS Remote Matlab "+e);
    }   
   return(valueDouble);
 }

  public synchronized double[] getDoubleArray (String _variable)  {
    if (asynchronousSimulation) return getDoubleArrayAS();

    if (modeAS){
      haltUpdate(true);
    }

    int _dim1;
    double[] vectorDouble=null;
try{
    bufferOutputTCP.writeInt (EjsId);
    bufferOutputTCP.writeUTF("getDoubleArray");
    bufferOutputTCP.writeUTF (_variable);
    bufferOutputTCP.flush();
    _dim1=bufferInputTCP.readInt();
    vectorDouble=new double[_dim1];
    for(int i=0;i<_dim1;i++) { vectorDouble[i]=bufferInputTCP.readDouble();}
 }catch(Exception e){
      System.out.println("Error getDoubleArray Remote Matlab "+e);
    }   
    return(vectorDouble);
  }

// Get from Buffer
  public synchronized double[] getDoubleArrayAS () {

    int _dim1;
    double[] vectorDouble=null;
    try{
    _dim1=bufferInputTCP.readInt();
    vectorDouble=new double[_dim1];
    for(int i=0;i<_dim1;i++) { vectorDouble[i]=bufferInputTCP.readDouble();}
 }catch(Exception e){
      System.out.println("Error getDoubleArrayAS Remote Matlab "+e);
    }    
    return(vectorDouble);
  }


  public synchronized double[][] getDoubleArray2D (String _variable)  {
    if (asynchronousSimulation) return getDoubleArray2DAS();
    if (modeAS){
      haltUpdate(true);
    }
    int _dim1,_dim2;
    double[][] arrayDouble=null;
try{
    bufferOutputTCP.writeInt (EjsId);
    bufferOutputTCP.writeUTF ("getDoubleArray2D");
    bufferOutputTCP.writeUTF (_variable);
    bufferOutputTCP.flush();
    _dim1=bufferInputTCP.readInt();
    _dim2=bufferInputTCP.readInt();
    arrayDouble= new double[_dim1][_dim2] ;
    for(int i=0;i<_dim1;i++) {
      for(int j=0;j<_dim2;j++){ arrayDouble[i][j]=bufferInputTCP.readDouble();}
    }
 }catch(Exception e){
      System.out.println("Error getDoubleArray2D Remote Matlab "+e);
    }   
    return(arrayDouble);
  }

// Get from Buffer
  public synchronized double[][] getDoubleArray2DAS ()  {
    int _dim1,_dim2;
    double[][] arrayDouble=null;
  try{
    _dim1=bufferInputTCP.readInt();
    _dim2=bufferInputTCP.readInt();
    arrayDouble= new double[_dim1][_dim2] ;
    for(int i=0;i<_dim1;i++) {
      for(int j=0;j<_dim2;j++){
        arrayDouble[i][j]=bufferInputTCP.readDouble();
      }
    }
    
 }catch(Exception e){
      System.out.println("Error getDoubleArray2DAS Remote Matlab "+e);
    } 
    return(arrayDouble);
  }

  public synchronized void setCommand(String _command){
    synchronize(true);
    commandUpdate=_command;
  }
  
//+++++++++++++++SET++++++++++++++++++++
/*
  public synchronized void setValue(String _name, String _value) throws
      Exception {
     if (!asynchronousSimulation){
       bufferOutputTCP.writeInt(EjsId);
       bufferOutputTCP.writeUTF("setValueString");
       bufferOutputTCP.writeUTF(_name);
       bufferOutputTCP.writeUTF(_value);
       bufferOutputTCP.flush();
       //  setParameter (_name); // In case it is a parameter
     }
  }

  public synchronized void setValue(String _name, String _value,
                                    boolean _flushNow) throws Exception {
    bufferOutputTCP.writeInt(EjsId);
    bufferOutputTCP.writeUTF("setValueString");
    bufferOutputTCP.writeUTF(_name);
    bufferOutputTCP.writeUTF(_value);
    if (_flushNow)
      bufferOutputTCP.flush();
  }

  public synchronized void setValue(String _name, double _value) throws Exception {
     if (!asynchronousSimulation){
       bufferOutputTCP.writeInt(EjsId);
       bufferOutputTCP.writeUTF("setValueDouble");
       bufferOutputTCP.writeUTF(_name);
       bufferOutputTCP.writeDouble(_value);
       bufferOutputTCP.flush();
       //setParameter (_name); // In case it is a parameter
     }
  }

  public synchronized void setValue(String _variable, double _value, boolean _flushNow) throws Exception {
      bufferOutputTCP.writeInt(EjsId);
      bufferOutputTCP.writeUTF("setValueDouble");
      bufferOutputTCP.writeUTF(_variable);
      bufferOutputTCP.writeDouble(_value);
      if (_flushNow)
        bufferOutputTCP.flush();
  }


  public synchronized void setValue(String _name, double[] _value) throws
      Exception {
     if (!asynchronousSimulation){
       bufferOutputTCP.writeInt(EjsId);
       bufferOutputTCP.writeUTF("setValueD[]");
       bufferOutputTCP.writeUTF(_name);
       bufferOutputTCP.writeInt(_value.length);
       for (int i = 0; i < _value.length; i++) {
         bufferOutputTCP.writeDouble(_value[i]);
       }
       bufferOutputTCP.flush();
       //setParameter (_name); // In case it is a parameter
     }
  }

  public synchronized void setValue(String _name, double[] _value,
                                    boolean _flushNow) throws Exception {
      bufferOutputTCP.writeInt(EjsId);
      bufferOutputTCP.writeUTF("setValueD[]");
      bufferOutputTCP.writeUTF(_name);
      bufferOutputTCP.writeInt(_value.length);
      for (int i = 0; i < _value.length; i++) {
        bufferOutputTCP.writeDouble(_value[i]);
      }
      if (_flushNow)
        bufferOutputTCP.flush();

  }

  public synchronized void setValue(String _name, double[][] _value) throws
      Exception {
     if (!asynchronousSimulation){
       bufferOutputTCP.writeInt(EjsId);
       bufferOutputTCP.writeUTF("setValueD[][]");
       bufferOutputTCP.writeUTF(_name);

       bufferOutputTCP.writeInt(_value.length);
       bufferOutputTCP.writeInt(_value[0].length);
       for (int i = 0; i < _value.length; i++) {
         for (int j = 0; j < _value[0].length; j++) {
           bufferOutputTCP.writeDouble(_value[i][j]);
         }
       }
       bufferOutputTCP.flush();
       // setParameter (_name); // In case it is a parameter
     }
  }

  public synchronized void setValue(String _name, double[][] _value,
                                    boolean _flushNow) throws Exception {
      bufferOutputTCP.writeInt(EjsId);
      bufferOutputTCP.writeUTF("setValueD[][]");
      bufferOutputTCP.writeUTF(_name);

      bufferOutputTCP.writeInt(_value.length);
      bufferOutputTCP.writeInt(_value[0].length);
      for (int i = 0; i < _value.length; i++) {
        for (int j = 0; j < _value[0].length; j++) {
          bufferOutputTCP.writeDouble(_value[i][j]);
        }
      }
      if (_flushNow)
        bufferOutputTCP.flush();
  }

//+++++++++++++++GET++++++++++++++++++++

  public synchronized String getString(String _variable) throws Exception { // Strongly inspired in JMatLink
    if (asynchronousSimulation) return getStringAS();
    if (modeAS){
      haltUpdate(true);
    }

    String charArray = null;
    bufferOutputTCP.writeInt(EjsId);
    bufferOutputTCP.writeUTF("getString");
    bufferOutputTCP.writeUTF(_variable);
    bufferOutputTCP.flush();
    charArray = bufferInputTCP.readUTF();
    return (charArray);
  }

// Get from Buffer
  public synchronized String getStringAS() throws Exception {
    String charArray = null;
    charArray = bufferInputTCP.readUTF();
    return (charArray);
  }

  public synchronized double getDouble(String _variable) throws Exception {
    if (asynchronousSimulation) return getDoubleAS();

    if (modeAS){
      haltUpdate(true);
    }

    double valueDouble = 0.0;

    bufferOutputTCP.writeInt(EjsId);
    bufferOutputTCP.writeUTF("getDouble");
    bufferOutputTCP.writeUTF(_variable);
    bufferOutputTCP.flush();
    valueDouble = bufferInputTCP.readDouble();
    return (valueDouble);

  }

// Get from Buffer
 public synchronized double getDoubleAS() throws Exception {
   double valueDouble=0.0;
   valueDouble= bufferInputTCP.readDouble();
   return(valueDouble);
 }

  public synchronized double[] getDoubleArray (String _variable) throws Exception {
    if (asynchronousSimulation) return getDoubleArrayAS();

    if (modeAS){
      haltUpdate(true);
    }

    int _dim1;
    double[] vectorDouble=null;

    bufferOutputTCP.writeInt (EjsId);
    bufferOutputTCP.writeUTF("getDoubleArray");
    bufferOutputTCP.writeUTF (_variable);
    bufferOutputTCP.flush();
    _dim1=bufferInputTCP.readInt();
    vectorDouble=new double[_dim1];
    for(int i=0;i<_dim1;i++) { vectorDouble[i]=bufferInputTCP.readDouble();}
    return(vectorDouble);
  }

// Get from Buffer
  public synchronized double[] getDoubleArrayAS () throws Exception {

    int _dim1;
    double[] vectorDouble=null;
    _dim1=bufferInputTCP.readInt();
    vectorDouble=new double[_dim1];
    for(int i=0;i<_dim1;i++) { vectorDouble[i]=bufferInputTCP.readDouble();}
    return(vectorDouble);
  }


  public synchronized double[][] getDoubleArray2D (String _variable)  throws Exception  {
    if (asynchronousSimulation) return getDoubleArray2DAS();
    if (modeAS){
      haltUpdate(true);
    }
    int _dim1,_dim2;
    double[][] arrayDouble=null;

    bufferOutputTCP.writeInt (EjsId);
    bufferOutputTCP.writeUTF ("getDoubleArray2D");
    bufferOutputTCP.writeUTF (_variable);
    bufferOutputTCP.flush();
    _dim1=bufferInputTCP.readInt();
    _dim2=bufferInputTCP.readInt();
    arrayDouble= new double[_dim1][_dim2] ;
    for(int i=0;i<_dim1;i++) {
      for(int j=0;j<_dim2;j++){ arrayDouble[i][j]=bufferInputTCP.readDouble();}
    }
    return(arrayDouble);
  }

// Get from Buffer
  public synchronized double[][] getDoubleArray2DAS ()  throws Exception  {
    int _dim1,_dim2;
    double[][] arrayDouble=null;
    _dim1=bufferInputTCP.readInt();
    _dim2=bufferInputTCP.readInt();
    arrayDouble= new double[_dim1][_dim2] ;
    for(int i=0;i<_dim1;i++) {
      for(int j=0;j<_dim2;j++){
        arrayDouble[i][j]=bufferInputTCP.readDouble();
      }
    }
    return(arrayDouble);
  }

  public synchronized void setCommand(String _command){
    synchronize(true);
    commandUpdate=_command;
  }
*/
  // Execute Remote _steps times,  output vars are returned from Remote Matlab
  // -------------------------------------------------------------------------
  public synchronized void update(String _command,String _outputvars,int _steps){
    if (_steps!=0){
      try{
        bufferOutputTCP.writeInt (EjsId);
        bufferOutputTCP.writeUTF ("update");
        bufferOutputTCP.writeUTF (_command);
        bufferOutputTCP.writeUTF (_outputvars);
        bufferOutputTCP.writeInt (_steps);
        bufferOutputTCP.writeInt (1024);
        bufferOutputTCP.flush();
      }
      catch (Exception e) {
        System.out.println(" update Remote Exception:"+e);
      }
    }else System.out.println(" Error parameter STEP is  null");
  }

  // Execute Remote _steps times,  output vars are returned from Remote Matlab
  // -------------------------------------------------------------------------
  public synchronized void update(String _command,String _outputvars,int _steps,int _package){
    if (_steps!=0){
      if (_package>0){
        try{
          bufferOutputTCP.writeInt (EjsId);
          bufferOutputTCP.writeUTF ("update");
          bufferOutputTCP.writeUTF (_command);
          bufferOutputTCP.writeUTF (_outputvars);
          bufferOutputTCP.writeInt (_steps);
          bufferOutputTCP.writeInt (_package);
          bufferOutputTCP.flush();
        }
        catch (Exception e) {
          System.out.println(" update Remote Exception:"+e);
        }
      }else
        System.out.println(" Error parameter PACKAGE is  negative");
    }else
      System.out.println(" Error parameter STEP is  null");
  }

  // Halt update
  // -----------
  public synchronized void haltUpdate(boolean _remove){
    byte outFirma1=(byte)((int)(Math.random()*255)),outFirma2=(byte)((int)(Math.random()*255)),outFirma3=(byte)((int)(Math.random()*255));
    byte[] inFirma=new byte[3];
    try{
      bufferOutputTCP.writeInt (EjsId);
      bufferOutputTCP.writeUTF ("haltupdate");
      bufferOutputTCP.writeBoolean (_remove);
      bufferOutputTCP.flush();
    }
    catch (Exception e) {
      System.out.println(" halt update Remote Exception:"+e);
    }

    //remove old data
    if (_remove){
      try{
        int buffsizein= bufferInputTCP.available();
        while (buffsizein>0){
          bufferInputTCP.skip(buffsizein);
          buffsizein= bufferInputTCP.available();
        }

        bufferOutputTCP.writeByte(outFirma1);
        bufferOutputTCP.writeByte(outFirma2);
        bufferOutputTCP.writeByte(outFirma3);
        bufferOutputTCP.flush();
        inFirma[0]=bufferInputTCP.readByte();
        inFirma[1]= bufferInputTCP.readByte();
        inFirma[2]= bufferInputTCP.readByte();
        boolean synOK=false;
        while (!synOK){
          if ((inFirma[0]==outFirma1) && (inFirma[1]==outFirma2) && (inFirma[2]==outFirma3))
            synOK=true;
          else {
            inFirma[0]=inFirma[1];
            inFirma[1]=inFirma[2];
            inFirma[2]= bufferInputTCP.readByte();
          }
        }
      }catch (Exception e) {
        System.out.println(" halt update Remote Exception:"+e);
      }
    }
  }


// --------------------------------------------------------
//  Private or protected methods
// --------------------------------------------------------

  protected synchronized void openModel () {
    if (model==null) return;
    eval ("open_system ('" + model +"')");
    eval("set_param ('"+theModel+"', 'Open','Off')");
    eval("set_param ('"+theModel+"', 'SimulationCommand','Stop')");
    eval("set_param ('"+theModel+"', 'StopTime','inf')");
    createEjsSubsystem ();
    initialize();
  }

  protected synchronized void initialize () {
    if (initCommand!=null) externalVars(initCommand);
    //if (initCommand!=null) matlab.engEvalString (id,initCommand);
  }

  protected synchronized void createEjsSubsystem () { };
/*
    matlab.engEvalString(id,
      "Ejs_sub_name=['"+theModel+"','/','Ejs_sub_','"+theModel+"']; \n"
//  + "%Get a Correct Block Name \n"
//    + "number=1; \n"
//    + "root=Ejs_sub_name; \n"
//    + "while not(isempty(find_system(parent,'SearchDepth',1,'name',Ejs_sub_name))) \n"
//    + "  Ejs_sub_name=[root,num2str(number)]; \n"
//    + "  number=number+1; \n"
//    + "end; \n"
    + "add_block('built-in/subsystem',Ejs_sub_name); \n"
    + "XY=get_param('"+theModel+"','location'); \n"
    + "height=XY(4)-XY(2); \n"
    + "width=XY(3)-XY(1); \n"
    + "sXY=[width/2-16,height-48,width/2+16,height-16]; \n"

//    + " ico1=['image(ind2rgb([']; \n"
//    + " ico2=['4,4,4,4,4,4,4,4,4,4,4,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,4,4,4,4,4,4;',... \n"
//    + "       '4,4,4,4,4,4,4,4,4,4,4,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,4,4,4,4,4,4;',... \n"
 //    + "       '4,4,4,4,4,4,4,4,4,4,2,2,2,2,3,3,3,3,3,3,3,3,3,3,3,4,4,4,4,4,4,4;',... \n"
 //    + "       '4,4,4,4,4,4,4,4,4,4,2,2,2,2,3,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4;',... \n"
 //    + "       '4,4,4,4,4,4,4,4,4,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,4,4,4,4,4,4,4,4;',... \n"
 //    + "       '4,4,4,4,4,4,4,4,4,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,4,4,4,4,4,4,4,4;',... \n"
 //    + "       '4,4,4,4,4,4,4,4,4,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,4,4,4,4,4,4,4,4;',... \n"
 //    + "       '4,4,4,4,4,4,4,4,4,2,2,2,2,3,3,3,3,3,3,3,3,3,3,3,4,4,2,2,2,2,3,4;',... \n"
 //    + "       '4,4,4,4,4,4,4,4,4,2,2,2,2,3,4,4,4,4,4,4,4,4,4,4,4,4,2,2,2,2,3,4;',... \n"
 //    + "       '4,4,4,4,4,4,4,4,2,2,2,2,3,4,4,4,4,4,4,4,4,4,4,4,4,4,2,2,2,2,3,4;',... \n"
 //    + "       '4,4,4,4,4,4,4,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,4,4,4,2,2,2,2,3,4;',... \n"
 //    + "       '4,4,4,4,4,4,4,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,4,4,2,2,2,2,2,4,4;',... \n"
 //    + "       '4,4,4,4,4,4,4,4,2,2,2,2,2,2,2,3,3,3,3,3,3,3,3,4,4,2,2,2,2,3,4,4;',... \n"
 //    + "       '4,4,4,4,4,4,2,2,2,2,2,2,2,2,3,4,4,4,4,4,4,4,4,4,2,2,2,2,2,4,4,4;',... \n"
 //    + "       '4,4,4,4,4,2,2,2,2,2,2,2,2,2,2,3,4,4,4,4,4,4,4,4,1,1,1,1,3,4,4,4;',... \n"
 //    + "       '4,4,4,4,2,2,2,2,2,2,2,2,2,2,2,2,4,4,4,4,4,4,4,4,1,1,1,1,3,4,4,4;',... \n"
 //    + "       '4,4,4,2,2,2,2,2,3,3,3,2,2,2,2,2,3,4,4,4,4,4,4,1,1,1,1,1,3,4,4,4;',... \n"
 //    + "       '4,4,4,2,2,2,2,3,4,4,4,4,4,2,2,2,2,4,4,4,4,4,4,1,1,1,1,1,3,4,4,4;',... \n"
 //    + "       '4,4,4,2,2,2,2,3,4,4,4,4,4,3,3,3,3,4,4,4,4,4,4,1,1,1,1,1,3,4,4,4;',... \n"
 //    + "       '4,4,4,2,2,2,2,2,4,4,4,4,4,2,2,2,2,3,4,4,4,4,4,2,2,2,2,3,4,4,4,4;',... \n"
 //   + "       '4,4,4,4,2,2,2,2,2,2,4,4,4,2,2,2,2,3,3,4,4,4,2,2,2,2,2,3,4,4,4,4;',... \n"
 //    + "       '4,4,4,4,4,1,1,1,1,1,1,1,4,2,2,2,2,2,2,2,2,2,2,2,2,2,3,4,4,4,4,4;',... \n"
 //    + "       '4,4,4,4,4,4,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,2,2,2,3,4,4,4,4,4;',... \n"
 //   + "       '4,4,4,4,4,4,4,4,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,2,3,4,4,4,4,4,4;',... \n"
 //    + "       '4,4,4,4,4,4,4,4,4,4,2,2,2,2,2,3,3,3,3,3,3,3,3,3,3,4,4,4,4,4,4,4;',... \n"
 //    + "       '4,4,4,4,4,4,4,4,4,4,4,2,2,2,2,3,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4;',... \n"
 //    + "       '4,2,2,2,2,4,4,4,4,4,4,2,2,2,3,3,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4;',... \n"
 //   + "       '4,2,2,2,2,4,4,4,4,4,4,2,2,2,3,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4;',... \n"
 //    + "       '4,2,2,2,2,2,2,2,2,2,2,2,2,2,3,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4;',... \n"
 //   + "       '4,4,2,2,2,2,2,2,2,2,2,2,2,3,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4;',... \n"
 //    + "       '4,4,4,2,2,2,2,2,2,2,2,2,3,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4;',... \n"
 //   + "       '4,4,4,4,3,3,3,3,3,3,3,3,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4;']; \n"
 //    + " ico3=['],[1 1 0;1 0 0;0 0 0;1 1 1]))']; \n"
 //   + " set_param(Ejs_sub_name,'position',sXY,'ShowName','off','MaskDisplay',[ico1,ico2,ico3],'MaskIconFrame','off'); \n"
    + "set_param(Ejs_sub_name,'position',sXY,'MaskDisplay','image(imread(''"+userDir+"/_library/EjsIcon.jpg'',''jpg''))'); \n"
//    + "add_block('built-in/Constant',[Ejs_sub_name,'/SetDone'],'value','1','position',[30,30,70,50]);\n"
//    + "add_block('built-in/toworkspace',[Ejs_sub_name,'/DoneToWS'],'VariableName','Ejs_Done','Buffer','1','position',[150,30,200,50]);\n"
//    + "add_line(Ejs_sub_name,'SetDone/1','DoneToWS/1');"
    );
  }
*/

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
    if (theModel!=null)
      eval("set_param ('"+theModel+"', 'SimulationCommand', 'Stop')"); // This makes Simulink beep
  }


//  private void processMFile (String _filename) {
//    int a,pos;
//    String line = new String();
//    String lineLowercase;
//    StringTokenizer aux;
//    try {
//      FileReader reader = new FileReader(_filename);
//      System.out.println ("Processing mFile = "+ _filename);
//      BufferedReader in = new BufferedReader(reader);
//      while((line = in.readLine())!= null) {
//        lineLowercase = line.toLowerCase().trim();
//        a = lineLowercase.lastIndexOf(EJS_ID);
//        if (a > 0) {
//          lineLowercase = lineLowercase.substring(a+EJS_ID_LENGTH);
//          if (lineLowercase.lastIndexOf(VAR_ID)>0) {  //It's a variable
//            // Get the name. Variable has no data which is relevant at this point
//            aux = new StringTokenizer (line, "=");
//            varInfo.addElement (new String (aux.nextToken().trim()));
//          }
//          else if ((pos=lineLowercase.lastIndexOf(PARAM_ID))>0) { // It's a parameter
//            // Get the name
//            aux = new StringTokenizer (line, "=");
//            String paramName = new String (aux.nextToken().trim());
//            // Get parameter's instruction data
//            line = line.substring(pos+a+EJS_ID_LENGTH).trim();
//            pos = line.indexOf(' ');
//            if (pos>=0) line = line.substring(0,pos); // separate Parameter instruction from other data (if any)
//            aux = new StringTokenizer (line, "=:");
//            String paramCommand = null;
//            switch (aux.countTokens()) {
//              case 2 : aux.nextToken (); // ignore 'Parameter' keyword
//                       paramCommand = "', '"+aux.nextToken().trim()+"'";
//                       break;
//              case 3 : aux.nextToken (); // ignore 'Parameter' keyword
//                       paramCommand = "/"+aux.nextToken().trim()+"', '"+aux.nextToken().trim()+"'";
//                       break;
//            }
//            if (paramCommand!=null) {
//              paramInfo.addElement (paramName); // name
//              paramInfo.addElement (new String (paramCommand+",'"+paramName + "')"));
//            }
//            else System.out.println ("Error: Parameter syntax error in file " + mFile+ ". Line :"+line);
//          }
//          else if (lineLowercase.lastIndexOf(MODEL_ID)>0) { // It's a model
//            // Get the model's name. No extra data
//            aux = new StringTokenizer (line, "= ;");
//            aux.nextToken (); // ignore model's variable name
//            model = aux.nextToken().trim();
//            model = model.substring(1, model.length()-1); // remove quotes
//            theModel = model.substring(0,model.lastIndexOf('.'));
//          }
//          else System.out.println ("Warning: Syntax incomplete in file " + mFile+ ". Ignoring line :"+line);
//        }
//      }
//      in.close();
//    } catch (Exception exc) { exc.printStackTrace(); }
//  }


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

  /**
   * EjsRemoteMatlab
   */
  public EjsRemoteMatlab() {
  }
} // End of class