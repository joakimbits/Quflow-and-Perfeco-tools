package org.colos.ejs.library.external;

import java.io.*;
import javasci.*;

import org.colos.ejs.library.Simulation;

public class EjsScilab implements ExternalApp {
  protected String initCommand = null; // An optional initialization command required for the correct reset
  private String sciFile;              // The sciFile as input from the user (trimmed)
  private String theSciFile=null;      // The sciFile as a command for SciLab workspace

// --------------------------------------------------------
//  Static methods and constructors
// --------------------------------------------------------

  /**
   * Creates a Scilab session.
   * @param _sciFile The sciFile to use to get information about variables,
   * parameters or Simulink model. Can be empty (i.e. "" or "<scilab>") if no information is needed.
   */
  public EjsScilab (String _sciFile) {
    sciFile = _sciFile.trim();
    if (sciFile.toLowerCase().startsWith("<scilab")) {
      int index = sciFile.indexOf('>');
      if (index>0) sciFile = sciFile.substring(index+1).trim();
      else sciFile = "";
    }
    if (sciFile.length()<=0) { sciFile = null; return; }
    // Make sure the file is there
    File file = Simulation.requiresResourceFile(sciFile);
/*
    String userDir = Simulation.getUserDir();
    String command = file.getParentFile().getAbsolutePath().replace('\\','/');
    if (command.startsWith(userDir)) command = command.substring(userDir.length());
    Scilab.Exec ("chdir ("+command+")");
    */
    Scilab.Exec( "chdir (" + file.getParentFile().getAbsolutePath()+")");
    theSciFile = file.getName();
//    int index = theSciFile.lastIndexOf('.');  // remove the extension
//    if (index>=0) theSciFile = theSciFile.substring (0, index);
    Scilab.Exec ("exec " +theSciFile); // THIS DOES NOT WORK!!!!!!!
  }

  public String toString () { return sciFile; }

  protected synchronized void initialize () {
    //if (initCommand!=null && initCommand.trim().length()>0) Scilab.Exec (initCommand);
    // Gonzalo 060420
    if (initCommand!=null && initCommand.trim().length()>0) Scilab.Exec (initCommand.substring(0,initCommand.lastIndexOf(";")));
  }

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
  
  public void setClient (ExternalClient _cli) { }

  public void setInitCommand (String _command) {
    initCommand = _command.trim();
    initialize();
  }

  public synchronized void eval (String _command) { Scilab.Exec(_command); }

  // Gonzalo 060420
  public synchronized void eval(String _command, boolean _flushNow) {
   eval (_command);
  }

  public void resetIC () { }
  public void resetParam () { }; // Gonzalo 060420

/**
 * Step the model a number of times.
 * @param dt The number of (integer) steps
 */
  public synchronized void step (double dt) { }

  // Gonzalo 060420
  //Asynchronous Remote Simulink
  public  void stepAS (double dt){}
  public  void stepAS (double dt, int _package){}

  public synchronized void reset() {
    Scilab.Exec ("clear");
    if (theSciFile!=null) Scilab.Exec (theSciFile);
  }

  public synchronized void quit() {
  }

// --------------------------------------------------------------
// Accessing the variables using EjsMatlabInfo as identifier
// --------------------------------------------------------------

  public synchronized void setValue (String _name, String _value) throws Exception {
    new SciString(_name,_value); // This includes a Send()
  }

  public void setValue (String _name, double _value) throws Exception {
    new SciDouble(_name,_value); // This includes a Send()
  }

  public void setValue (String _name, double[] _value) throws Exception {
    new SciDoubleArray(_name,1,_value.length,_value);
  }

  public void setValue (String _name, double[][] _value) throws Exception {
    int row=_value.length;
    int col=_value[0].length;
    int k=0;
    double[] temp=new double[row*col];
    for(int j=0; j<col; j++) for(int i=0; i<row; i++) temp[k++]=_value[i][j];
    new SciDoubleArray(_name,row,col,temp);
  }

  public synchronized String getString (String _variable) throws Exception {
    return (new SciString(_variable)).getData();
  }

  public double getDouble (String _variable) {
    return new SciDouble(_variable).getData();
  }

  public synchronized double[] getDoubleArray (String _variable) {
    Scilab.Exec("EjsSciLength=size("+_variable+");");
    double[] size = (new SciDoubleArray("EjsSciLength",1,2)).getData();
    return (new SciDoubleArray(_variable,(int) size[1],1)).getData();
  }

  public double[][] getDoubleArray2D (String _variable) {
    Scilab.Exec("EjsSciLength=size("+_variable+")");
    double[] size  = (new SciDoubleArray("EjsSciLength",1,2)).getData();
    int row=(int)size[0];
    int col=(int)size[1];
    SciDoubleArray var = new SciDoubleArray(_variable,row,col);
    var.Get();
    double[] auxValue=var.getData();
    double[][] value=new double[row][col];
    int _k=0;
    for(int j=0; j<col; j++) for(int i=0; i<row; i++) value[i][j]=auxValue[_k++];
    return value;
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

  public synchronized void synchronize(boolean _remove){
  }

  public synchronized void synchronize(){
  }

  public synchronized void externalVars(String _externalVars){
  }

  public synchronized void setCommand(String _command){
  }

  public double getDoubleAS () {
    return 0;
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

