package org.colos.ejs.library.external;

/**
 * Defines an interface for external applications
 * that will be used from Ejs
 */
public interface ExternalApp {

  //Gonzalo 090611
  final public String connectionOK="Connection Sucessful";
  final public String connectionAuthenticationRequired="Authentication Required";
  final public String connectionAuthenticationFail="Authentication Failed";
  final public String connectionSlotFail="No Time Slot";
  final public String connectionNoServer="Server Off";
  final public String connectionNoModel="Model Not Found";  
  final public int connectionTimeOut=20000; 
  
//--------------------------------------------------------------
//Setting the connection
//--------------------------------------------------------------

  /**
   * Provides the ExternalApp with a pointer to the ExternalClient
   * that uses it.
   * @param _client ExternalClient
   */
  public void setClient (ExternalClient _client);

  /**
   * Provides the ExternalApp with a pointer to the ExternalClient
   * that uses it to link the variables.
   * @param _client ExternalClient
   */
  public boolean setVarContext(Object _client);  //Gonzalo 090611
  
  public boolean linkVariables(String _clientVar, String _externalVar); //Gonzalo 090610
  
  public String connect (); //Gonzalo 090611
  
  public void disconnect (); //Gonzalo 090611
  
  public boolean isConnected(); //Gonzalo 090611
  
  
//--------------------------------------------------------------
//Controlling the application
//--------------------------------------------------------------

  /**
   * Accepts an initialization command to use whenever the system is reset
   * @param _command String
   */
  public void setInitCommand (String _command);

  /**
   * Some external apps, such as Simulink models, require that
   * this method is called whenever Integrator blocks need to
   * accept newly provided initial conditions.
   */
  public void resetIC ();

  /**
   * Some external apps, such as Simulink models, require that
   * this method is called whenever parameter of block need to update.
   */

  public void resetParam (); // Gonzalo 060420


  /**
   * Evaluates a given command in the external application
   * @param _command String
   */
  public void eval (String _command);

  // with flushNow
  public void eval(String _command, boolean _flushNow);// Gonzalo 060420

  /**
   * Steps the application a given step or a number of times.
   * The actual meaning of the parameter dt depends on the implementing class
   * @param dt The number of (integer) steps or the delta of time to increment
   */
  public void step (double dt);

  /**
   * Resets the application
   */
  public void reset();

  /**
   * Quits the application
   */
  public void quit();

//--------------------------------------------------------------
//Accessing the variables of the application
//--------------------------------------------------------------

  /**
   * Sets the value of the given variable of the application
   * @param _variable String the variable name
   * @param _value String the desired value
   * @throws Exception
   */
  public void setValue(String _variable, String _value) throws Exception;
  public void setValue(String _variable, String _value, boolean _flushNow ) throws Exception; // Gonzalo 060420
  /**
   * Sets the value of the given variable of the application
   * @param _variable String the variable name
   * @param _value double the desired value
   * @throws Exception
   */
  public void setValue(String _variable, double _value) throws Exception;
  public void setValue(String _variable, double _value, boolean _flushNow) throws Exception; // Gonzalo 060420
  /**
   * Sets the value of the given variable of the application
   * @param _variable String the variable name
   * @param _value double[] the desired value
   * @throws Exception
   */
  public void setValue(String _variable, double[] _value) throws Exception;
  public void setValue(String _variable, double[] _value, boolean _flushNow) throws Exception; // Gonzalo 060420
  /**
   * Sets the value of the given variable of the application
   * @param _variable String the variable name
   * @param _value double[][] the desired value
   * @throws Exception
   */
  public void setValue(String _variable, double[][] _value) throws Exception;
  public void setValue(String _variable, double[][] _value, boolean _flushNow) throws Exception; // Gonzalo 060420
  /**
   * Gets the value of a String variable of the application
   * @param _variable String the variable name
   * @throws Exception
   * @return String the value
   */
  public String getString (String _variable) throws Exception;

  /**
   * Gets the value of a String variable of the remote application from Buffer
   * @throws Exception
   * @return String the value
   */
  public String getStringAS () throws Exception; // Gonzalo 060420

  /**
   * Gets the value of a double variable of the application
   * @param _variable String the variable name
   * @throws Exception
   * @return double the value
   */
  public double getDouble (String _variable) throws Exception; // Gonzalo 060420

  /**
   * Gets the value of a double variable of the remote application from Buffer
   * @throws Exception
   * @return double the value
   */
  public double getDoubleAS () throws Exception; // Gonzalo 060420

  /**
   * Gets the value of a double[] variable of the application
   * @param _variable String the variable name
   * @throws Exception
   * @return double the value
   */
  public double[] getDoubleArray (String _variable) throws Exception; // Gonzalo 060420

  /**
   * Gets the value of a double[] variable of the remote application from Buffer
   * @throws Exception
   * @return double the value
   */

  public double[] getDoubleArrayAS () throws Exception; // Gonzalo 060420

  /**
   * Gets the value of a double[][] variable of the application
   * @param _variable String the variable name
   * @throws Exception
   * @return double the value
   */
  public double[][] getDoubleArray2D (String _variable) throws Exception;

  /**
   * Gets the value of a double[][] variable of the remote application from Buffer
   * @throws Exception
   * @return double the value
   */
  public double[][] getDoubleArray2DAS () throws Exception; // Gonzalo 060420

  // Gonzalo 060420
  // ######################### REMOTE APPLICATIONS ##############################
  public  void synchronize(boolean _remove);

  public  void synchronize();

  public  void packageSize(double _package);

  public void update(String _command,String _outputvars,int _steps);

  public void update(String _command,String _outputvars,int _steps,int _package);

  public void externalVars(String _externalVars);

  public void setCommand(String _command);

  // Gonzalo 060420
  //Asynchronous Remote Simulink
  public  void stepAS (double dt);

  public  void stepAS (double dt, int _package);

  public void haltUpdate(boolean _remove);

  public void haltStepAS(boolean _remove);

}
