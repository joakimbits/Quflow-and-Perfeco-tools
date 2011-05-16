package org.colos.ejs.library.external;

import java.util.*;
//import java.io.*;
import org.colos.ejs.library.Simulation;

import java.io.File;

/**
 * Defines a generic class that deals with one or
 * more external applications
 */
public class ExternalAppsHandler {
  private ExternalClient client; // This identifies the owner of the real data
  private Hashtable<String,ExternalApp> appList = new Hashtable<String,ExternalApp>();

// --------------------------------------------------------
//  Static methods and constructors
// --------------------------------------------------------

  /**
   * Creates an empty list of external applications for use by a given client
   * @param _client The owner of the variables (i.e., the simulation)
   */
  public ExternalAppsHandler (ExternalClient _client) {
    client = _client;
  }

  /**
   * For backwards compatibility only
   */
  public ExternalAppsHandler (String _jarFileName, ExternalClient _client) {
    this (_client);
  }

//------------------------------------
// Handling resources
//------------------------------------


  /**
   * Makes sure a file exists. Otherwise, it extracts it using the ResourceLoader to find it
   * @param _resource String The file to extract
   * @param _resource String The destination of the file in the user's local file system
   * @return boolean true if successful
   */
  public void requires (String _file) {
    String source = _file.replace('\\','/');
    if (! new File(source).exists()) {
      if (Simulation.extractResource(source,Simulation.getTemporaryDir()+_file)==null) {
        System.out.println("Warning : the required file "+_file+" could not be extracted!");
      }
    }
  }

//------------------------------------
// Handling the applications
//------------------------------------

  public ExternalApp add (Class<?> externalAppClass, String externalFile)  {
    
    try {
      externalFile = externalFile.trim();
      ExternalApp app = appList.get(externalFile);
      if (app!=null) return app;
      // Create the external class for this file using reflection
      Class<?> [] c = { String.class };
      Object[] o = { externalFile };
      java.lang.reflect.Constructor<?> constructor = externalAppClass.getDeclaredConstructor(c);
      app = (ExternalApp) constructor.newInstance(o); 
      
      app.setVarContext(client); //Gonzalo 090610


      //app.setInitCommand (client._externalInitCommand(externalFile)); //Gonzalo 090610
      
      // Add it to the hashtable of applications
      appList.put(externalFile,app);
      app.setClient(client);
      return app;
    }
    catch (Exception _exc) {
      _exc.printStackTrace();
      System.err.println ("ExternalAppsHandler: error when trying to add class "+ externalAppClass+ "!");
      return null;
    }
  }

  public void remove(String externalFile)  {
    appList.remove(externalFile);
  }

  public ExternalApp getApplication (String externalFile) {
    return appList.get(externalFile);
  }

  public ExternalClient getClient () {
    return client;
  }

//------------------------------------
// Operating with the applications
//------------------------------------

  /**
   * Lunch the external application
   * @param _appFile The file with which the application was opened
   */
  public String connect (String _appFile) {
    ExternalApp app = getApplication (_appFile);
    if (app==null) System.err.println("Error: application " + _appFile + " not found!");
    else return app.connect();
    return null;
  }

  /**
   * Lunch all external applications
   */
  public void connect () {
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); )  e.nextElement().connect();
  }
  
  /**
   * Close the external application
   * @param _appFile The file with which the application was opened
   */
  public void disconnect (String _appFile) {
    ExternalApp app = getApplication (_appFile);
    if (app==null) System.err.println("Error: application " + _appFile + " not found!");
    else  app.disconnect();
  }

  /**
   * Close all external applications
   */
  public void disconnect () {
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) e.nextElement().disconnect();
  }
  
  /**
   * Check if the external application is connected
   * @param _appFile The file with which the application was opened
   */
  public boolean isConnected (String _appFile) {
    ExternalApp app = getApplication (_appFile);
    if (app==null) System.err.println("Error: application " + _appFile + " not found!");
    else  return app.isConnected();
    return false;
  }

  /**
   * Check if all external applications are connected
   */
  public boolean isConnected () {
    boolean connected=true;
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) connected=connected && e.nextElement().isConnected();
    return connected;
  }
  
  
  
  
  
  /**
   * Evaluate an expression in a given external application
   * @param _appFile The file with which the application was opened
   * @param command The expression to evaluate
   */
  public void eval (String _appFile, String command) {
    ExternalApp app = getApplication (_appFile);
    if (app==null) System.err.println("Error: application " + _appFile + " not found!");
    else {
      try { app.eval(command); }
      catch (Exception exc) {
        System.err.println("Error evaluating <"+command+"> for application : "+app);
        exc.printStackTrace();
      }
    }
  }

  // eval with flush now // Gonzalo 060420
  public void eval (String _appFile, String command, boolean _flushNow) {
    ExternalApp app = getApplication (_appFile);
    if (app==null) System.err.println("Error: application " + _appFile + " not found!");
    else {
      try { app.eval(command,_flushNow); }
      catch (Exception exc) {
        System.err.println("Error evaluating <"+command+"> for application : "+app);
        exc.printStackTrace();
      }
    }
  }

  /**
   * Evaluate an expression in all external applications
   * @param command The expression to evaluate
   */
  public void eval (String command) {
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
      ExternalApp app = e.nextElement();
      try { app.eval(command); }
      catch (Exception exc) {
        System.err.println("Error evaluating <"+command+"> for application : "+app);
        exc.printStackTrace();
      }
    }
  }

  // eval with flush now // Gonzalo 060420
  public void eval(String _command, boolean _flushNow) {
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
      ExternalApp app = e.nextElement();
      try { app.eval(_command,_flushNow); }
      catch (Exception exc) {
        System.err.println("Error evaluating <"+_command+"> for application : "+app);
        exc.printStackTrace();
      }
    }
  }

  /**
   * Resets integrators blocks of the given app, so that they will accept the
   * newly provided initial conditions. External apps, such as Simulink models,
   * require this.
   * @param _appFile The file with which the application was opened
   */
  public void resetIC (String _appFile) {
    ExternalApp app = getApplication (_appFile);
    if (app==null) System.err.println("Error: application " + _appFile + " not found!");
    else app.resetIC();
  }

  /**
   * Resets integrators blocks so that they will accept the newly provided
   * initial conditions. External apps, such as Simulink models, require this.
   */
  public void resetIC () {
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) e.nextElement().resetIC();
  }

  /**
   * Update block's parameters
   * External apps, such as Simulink models, require this.
   */

  public void resetParam (){ // Gonzalo 060420
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) e.nextElement().resetParam();
  }


  /**
   * Steps an application a given step or a number of times.
   * It previously pushes all client variables to the application.
   * After stepping the application, it retrieves all its client variables.
   * @param _appFile The file with which the application was opened
   * @param dt The number of (integer) steps or the delta of time to increment.
   *           The actual meaning depends on the external application
   */
  public void step (String _appFile, double dt) {
    ExternalApp app = getApplication (_appFile);
    if (app==null) System.err.println("Error: application " + _appFile + " not found!");
    else {
      //client._externalSetValues (false, app); //Gonzalo 090610
      try { app.step(dt); }
      catch (Exception exc) {
        System.err.println("Error stepping application : "+app);
        exc.printStackTrace();
      }
      //client._externalGetValues (false, app);//Gonzalo 090610
    }
  }

  /**
   * Steps all application a given step or a number of times.
   * It previously pushes all client variables to the applications.
   * After stepping all applications, it retrieves all client variables.
   * @param dt The number of (integer) steps or the delta of time to increment.
   *           The actual meaning depends on the external application
   */
  public void step (double dt) {
    //client._externalSetValues (true, null);  //Gonzalo 090610
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
      ExternalApp app = e.nextElement();
      try { app.step(dt); }
      catch (Exception exc) {
        System.err.println("Error stepping application : "+app);
        exc.printStackTrace();
      }
    }
    // client._externalGetValues (true, null);  //Gonzalo 090610
  }

  /**
   * Updates the connection to an application.
   * It pushes all client variables to the application and then
   * retrieves all its client variables.
   * @param _appFile The file with which the application was opened
   */
  public void update (String _appFile) {
    ExternalApp app = getApplication (_appFile);
    if (app==null) System.err.println("Error: application " + _appFile + " not found!");
    else {
      client._externalSetValues (false, app);
      client._externalGetValues (false, app);
    }
  }

  /**
   * Updates the connection to all applications.
   * It pushes all client variables to the applications and then
   * retrieves all their client variables.
   */
  public void update () {
    client._externalSetValues (true, null);
    client._externalGetValues (true, null);
  }

  /**
   * Resets a given external application
   * @param _appFile The file with which the application was opened
   */
  public void reset(String _appFile)  {
    ExternalApp app = getApplication (_appFile);
    if (app==null) System.err.println("Error: application " + _appFile + " not found!");
    else app.reset();
  }

  /**
   * Resets all external applications
   */
  public void reset()  {
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) e.nextElement().reset();
  }

  /**
   * Quits a given external application
   * @param _appFile The file with which the application was opened
   */
  public void quit(String _appFile)  {
    ExternalApp app = getApplication (_appFile);
    if (app==null) System.err.println("Error: application " + _appFile + " not found!");
    else app.quit();
  }

  /**
   * Quits all external applications
   */
  public void quit()  {
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) e.nextElement().quit();
  }

//------------------------------------------
// Setting and getting client variables
//------------------------------------------

  /**
   * Push the value of all client variables at once to all applications
   */
  public void setValues () { client._externalSetValues(true, null); }

  /**
   * Gets the value of all client variables at once from all applications
   */
  public void getValues () { client._externalGetValues(true, null); }

//------------------------------------------
// Setting and getting particular variables
//------------------------------------------

  /**
   * Set the value of a String variable to all external applications
   * @param _name The name of the variable
   * @param _value The value
   */
  public void setValue (String _name, String _value) {
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
      ExternalApp app = e.nextElement();
      try { app.synchronize(true); // Gonzalo 060420
        app.setValue (_name,_value); }
      catch (Exception exc) {
        System.err.println("Error setting <" + _name + "> to <"+_value+"> for application : " + app);
        exc.printStackTrace();
      }
    }
  }

  // Gonzalo 060420
  public void setValue (String _name, String _value, boolean _flushNow) {
      for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
        ExternalApp app = e.nextElement();
        try {  app.synchronize(true); //##
          app.setValue (_name,_value,_flushNow); }
        catch (Exception exc) {
          System.err.println("Error setting <" + _name + "> to <"+_value+"> for application : " + app);
          exc.printStackTrace();
        }
      }
    }


  /**
   * Set the value of a String variable in a given external application
   * @param _appFile The file with which the application was opened
   * @param _name The name of the variable
   * @param _value The value
   */
  public void setValue (String _appFile, String _name, String _value) {
    ExternalApp app = getApplication (_appFile);
    if (app==null) System.err.println("Error: application " + _appFile + " not found!");
    else {
      try { app.synchronize(true); // Gonzalo 060420
        app.setValue (_name,_value); }
      catch (Exception exc) {
        System.err.println("Error setting <" + _name + "> to <"+_value+"> for application : " + _appFile);
        exc.printStackTrace();
      }
    }
  }

  // Gonzalo 060420
  public void setValue (String _appFile, String _name, String _value, boolean _flushNow) {
    ExternalApp app = getApplication (_appFile);
    if (app==null) System.err.println("Error: application " + _appFile + " not found!");
    else {
      try {  app.synchronize(true); //##
        app.setValue (_name,_value,_flushNow); }
      catch (Exception exc) {
        System.err.println("Error setting <" + _name + "> to <"+_value+"> for application : " + _appFile);
        exc.printStackTrace();
      }
    }
  }

  /**
   * Set the value of a double variable to all external applications
   * @param _name The name of the variable
   * @param _value The value
   */
  public void setValue (String _name, double _value) {
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
      ExternalApp app = e.nextElement();
      try { app.synchronize(true); // Gonzalo 060420
        app.setValue (_name,_value); }
      catch (Exception exc) {
        System.err.println("Error setting <" + _name + "> to <"+_value+"> for application : " + app);
        exc.printStackTrace();
      }
    }
  }

  // Gonzalo 060420
  public void setValue (String _name, double _value, boolean _flushNow) {
      for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
        ExternalApp app = e.nextElement();
        try {  app.synchronize(true); //##
          app.setValue (_name,_value,_flushNow); }
        catch (Exception exc) {
          System.err.println("Error setting <" + _name + "> to <"+_value+"> for application : " + app);
          exc.printStackTrace();
        }
      }
    }

  /**
   * Set the value of a double variable in a given external application
   * @param _appFile The file with which the application was opened
   * @param _name The name of the variable
   * @param _value The value
   */
  public void setValue (String _appFile, String _name, double _value) {
    ExternalApp app = getApplication (_appFile);
    if (app==null) System.err.println("Error: application " + _appFile + " not found!");
    else {
      try { app.synchronize(true); // Gonzalo 060420
        app.setValue (_name,_value); }
      catch (Exception exc) {
        System.err.println("Error setting <" + _name + "> to <"+_value+"> for application : " + _appFile);
        exc.printStackTrace();
      }
    }
  }

  // Gonzalo 060420
  public void setValue (String _appFile, String _name, double _value, boolean _flushNow) {
    ExternalApp app = getApplication (_appFile);
    if (app==null) System.err.println("Error: application " + _appFile + " not found!");
    else {
      try { app.synchronize(true); //##
        app.setValue (_name,_value,_flushNow); }
      catch (Exception exc) {
        System.err.println("Error setting <" + _name + "> to <"+_value+"> for application : " + _appFile);
        exc.printStackTrace();
      }
    }
  }

  /**
   * Set the value of a double[] variable to all external applications
   * @param _name The name of the variable
   * @param _value The value
   */
  public void setValue (String _name, double[] _value) {
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
      ExternalApp app = e.nextElement();
      try { app.synchronize(true); // Gonzalo 060420
        app.setValue (_name,_value); }
      catch (Exception exc) {
        System.err.println("Error setting <" + _name + "> to <"+_value+"> for application : " + app);
        exc.printStackTrace();
      }
    }
  }

  // Gonzalo 060420
  public void setValue (String _name, double[] _value, boolean _flushNow) {
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
      ExternalApp app = e.nextElement();
      try {  app.synchronize(true); //##
        app.setValue (_name,_value,_flushNow); }
      catch (Exception exc) {
        System.err.println("Error setting <" + _name + "> to <"+_value+"> for application : " + app);
        exc.printStackTrace();
      }
    }
  }

  /**
   * Set the value of a double[] variable in a given external application
   * @param _appFile The file with which the application was opened
   * @param _name The name of the variable
   * @param _value The value
   */
  public void setValue (String _appFile, String _name, double[] _value) {
    ExternalApp app = getApplication (_appFile);
    if (app==null) System.err.println("Error: application " + _appFile + " not found!");
    else {
      try { app.synchronize(true); // Gonzalo 060420
        app.setValue (_name,_value); }
      catch (Exception exc) {
        System.err.println("Error setting <" + _name + "> to <"+_value+"> for application : " + _appFile);
        exc.printStackTrace();
      }
    }
  }

  // Gonzalo 060420
  public void setValue (String _appFile, String _name, double[] _value, boolean _flushNow) {
     ExternalApp app = getApplication (_appFile);
     if (app==null) System.err.println("Error: application " + _appFile + " not found!");
     else {
       try {  app.synchronize(true); //##
         app.setValue (_name,_value, _flushNow); }
       catch (Exception exc) {
         System.err.println("Error setting <" + _name + "> to <"+_value+"> for application : " + _appFile);
         exc.printStackTrace();
       }
     }
   }

  /**
   * Set the value of a double[][] variable to all external applications
   * @param _name The name of the variable
   * @param _value The value
   */
  public void setValue (String _name, double[][] _value) {
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
      ExternalApp app = e.nextElement();
      try { app.synchronize(true); // Gonzalo 060420
        app.setValue (_name,_value); }
      catch (Exception exc) {
        System.err.println("Error setting <" + _name + "> to <"+_value+"> for application : " + app);
        exc.printStackTrace();
      }
    }
  }

  // Gonzalo 060420
  public void setValue (String _name, double[][] _value, boolean _flushNow) {
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
      ExternalApp app = e.nextElement();
      try { app.synchronize(true); //##
        app.setValue (_name,_value,_flushNow); }
      catch (Exception exc) {
        System.err.println("Error setting <" + _name + "> to <"+_value+"> for application : " + app);
        exc.printStackTrace();
      }
    }
  }

  /**
   * Set the value of a double[][] variable in a given external application
   * @param _appFile The file with which the application was opened
   * @param _name The name of the variable
   * @param _value The value
   */
  public void setValue (String _appFile, String _name, double[][] _value) {
    ExternalApp app = getApplication (_appFile);
    if (app==null) System.err.println("Error: application " + _appFile + " not found!");
    else {
      try { app.synchronize(true); // Gonzalo 060420
        app.setValue (_name,_value); }
      catch (Exception exc) {
        System.err.println("Error setting <" + _name + "> to <"+_value+"> for application : " + _appFile);
        exc.printStackTrace();
      }
    }
  }

  // Gonzalo 060420
  public void setValue (String _appFile, String _name, double[][] _value, boolean _flushNow) {
    ExternalApp app = getApplication (_appFile);
    if (app==null) System.err.println("Error: application " + _appFile + " not found!");
    else {
      try {  app.synchronize(true); //##
        app.setValue (_name,_value, _flushNow); }
      catch (Exception exc) {
        System.err.println("Error setting <" + _name + "> to <"+_value+"> for application : " + _appFile);
        exc.printStackTrace();
      }
    }
  }


  /**
   * Get the String value of a variable from any of the external applications
   * @param _variable The name of the variable
   * @return string
   */
  public String getString (String _variable) {
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
      ExternalApp app = e.nextElement();
      try { app.synchronize(true); // Gonzalo 060420
        return app.getString (_variable); }
      catch (Exception exc) {} // Do not complain if it doesn't exist
    }
    System.err.println("Error: variable <" + _variable + "> doesn't exists in any external application.");
    return "";
  }

  // Gonzalo 060420
  public String getStringAS () {
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
      ExternalApp app = e.nextElement();
      try { return app.getStringAS (); }
      catch (Exception exc) {} // Do not complain if it doesn't exist
    }
    System.err.println("Error: variable doesn't exists in any external application.");
    return "";
  }


  /**
   * Get the String value of a variable in a given external application
   * @param _appFile The file with which the application was opened
   * @param _variable The name of the variable
   * @return string
   */
  public String getString (String _appFile, String _variable) {
    ExternalApp app = getApplication (_appFile);
    if (app!=null)
      try { app.synchronize(true); // Gonzalo 060420
        return app.getString (_variable); }
      catch (Exception exc) {
        System.err.println("Error getting <" + _variable + "> from application : " + _appFile);
        exc.printStackTrace();
      }
    return "";
  }

  // Gonzalo 060420
  public String getStringAS (String _appFile) {
    ExternalApp app = getApplication (_appFile);
    if (app!=null)
      try { return app.getStringAS (); }
      catch (Exception exc) {
        System.err.println("Error getting buffer from application : " + _appFile);
        exc.printStackTrace();
      }
    return "";
  }

  /**
   * Get the double value of a variable from any of the external applications
   * @param _variable The name of the variable
   * @return double
   */
  public double getDouble (String _variable) {
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
      ExternalApp app = e.nextElement();
      try { app.synchronize(true); // Gonzalo 060420
        return app.getDouble (_variable); }
      catch (Exception exc) { exc.printStackTrace(); } // Do not complain if it doesn't exist
    }
    System.err.println("Error: variable <" + _variable + "> doesn't exists in any external application.");
    return 0.0;
  }

  // Gonzalo 060420
  public double getDoubleAS () {

    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
      ExternalApp app = e.nextElement();
      try { return app.getDoubleAS (); }
      catch (Exception exc) { exc.printStackTrace(); } // Do not complain if it doesn't exist
    }
    System.err.println("Error: variable can't get from Buffer in any external application.");
    return 0.0;
  }

  /**
   * Get the double value of a variable in a given external application
   * @param _appFile The file with which the application was opened
   * @param _variable The name of the variable
   * @return double
   */
  public double getDouble (String _appFile, String _variable) {
    ExternalApp app = getApplication (_appFile);
    if (app!=null)
      try { app.synchronize(true); // Gonzalo 060420
        return app.getDouble (_variable); }
      catch (Exception exc) {
        System.err.println("Error getting <" + _variable + "> from application : " + _appFile);
        exc.printStackTrace();
      }
    return 0.0;
  }

  // Gonzalo 060420
  public double getDoubleAS (String _appFile) {
    ExternalApp app = getApplication (_appFile);
     if (app!=null)
      try { return app.getDoubleAS (); }
      catch (Exception exc) {
        System.err.println("Error getting buffer from application : " + _appFile);
        exc.printStackTrace();
      }
    return 0.0;
  }


  /**
   * Get the double[] value of a variable from any of the external applications
   * @param _variable The name of the variable
   * @return double[]
   */
  public double[] getDoubleArray (String _variable) {
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
      ExternalApp app = e.nextElement();
      try { app.synchronize(true); // Gonzalo 060420
        return app.getDoubleArray (_variable); }
      catch (Exception exc) {} // Do not complain if it doesn't exist
    }
    System.err.println("Error: variable <" + _variable + "> doesn't exists in any external application.");
    return null;
  }

  // Gonzalo 060420
  public double[] getDoubleArrayAS () {
     for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
       ExternalApp app = e.nextElement();
       try { return app.getDoubleArrayAS (); }
       catch (Exception exc) {} // Do not complain if it doesn't exist
     }
     System.err.println("Error: variable doesn't exists in any external application.");
     return null;
   }

  /**
   * Get the double[] value of a variable in a given external application
   * @param _appFile The file with which the application was opened
   * @param _variable The name of the variable
   * @return double[]
   */
  public double[] getDoubleArray (String _appFile, String _variable) {
    ExternalApp app = getApplication (_appFile);
    if (app!=null)
      try { app.synchronize(true); // Gonzalo 060420
        return app.getDoubleArray (_variable); }
      catch (Exception exc) {
        System.err.println("Error getting <" + _variable + "> from application : " + _appFile);
        exc.printStackTrace();
      }
    return null;
  }

  // Gonzalo 060420
  public double[] getDoubleArrayAS (String _appFile) {
    ExternalApp app = getApplication (_appFile);
    if (app!=null)
      try { return app.getDoubleArrayAS (); }
      catch (Exception exc) {
        System.err.println("Error getting buffer from application : " + _appFile);
        exc.printStackTrace();
      }
    return null;
  }

  /**
   * Get the double[][] value of a variable from any of the external applications
   * @param _variable The name of the variable
   * @return double[][]
   */
  public double[][] getDoubleArray2D (String _variable) {
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
      ExternalApp app = e.nextElement();
      try { app.synchronize(true); // Gonzalo 060420
        return app.getDoubleArray2D (_variable); }
      catch (Exception exc) {} // Do not complain if it doesn't exist
    }
    System.err.println("Error: variable <" + _variable + "> doesn't exists in any external application.");
    return null;
  }

  // Gonzalo 060420
  public double[][] getDoubleArray2DAS () {
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
      ExternalApp app = e.nextElement();
      try { return app.getDoubleArray2DAS (); }
      catch (Exception exc) {} // Do not complain if it doesn't exist
    }
    System.err.println("Error: variable doesn't exists in any external application.");
    return null;
  }

  /**
   * Get the double[][] value of a variable in a given external application
   * @param _appFile The file with which the application was opened
   * @param _variable The name of the variable
   * @return double
   */
  public double[][] getDoubleArray2D (String _appFile, String _variable) {
    ExternalApp app = getApplication (_appFile);
    if (app!=null)
      try { app.synchronize(true); // Gonzalo 060420
        return app.getDoubleArray2D (_variable); }
      catch (Exception exc) {
        System.err.println("Error getting <" + _variable + "> from application : " + _appFile);
        exc.printStackTrace();
      }
    return null;
  }

  // Gonzalo 060420
  public double[][] getDoubleArray2DAS (String _appFile) {
    ExternalApp app = getApplication (_appFile);
    if (app!=null)
      try { return app.getDoubleArray2DAS (); }
      catch (Exception exc) {
        System.err.println("Error getting buffer from application : " + _appFile);
        exc.printStackTrace();
      }
    return null;
  }

// Gonzalo 060420
// ######################### REMOTE APPLICATIONS ##############################

  public synchronized void externalVars(String _appFile,String _externalVars){
    ExternalApp app = getApplication (_appFile);
     if (app!=null)
       try {  app.externalVars(_externalVars); }
       catch (Exception exc) {
         System.err.println("Error: can't to set external variables in any external application.");
         exc.printStackTrace();
       }
  }

  public synchronized void externalVars(String _externalVars){
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
       ExternalApp app =  e.nextElement();
       try { app.externalVars (_externalVars); }
       catch (Exception exc) {
        System.err.println("Error: can't to set external variables in any external application.");
       } // Do not complain if it doesn't exist
     }
  }





  public synchronized void synchronize(String _appFile,boolean _remove){
    ExternalApp app = getApplication (_appFile);
    if (app!=null)
      try {  app.synchronize(_remove); }
      catch (Exception exc) {
        System.err.println("Error: can't to synchronize in any external application.");
        exc.printStackTrace();
      }
  }

  public synchronized void synchronize(String _appFile){
    ExternalApp app = getApplication (_appFile);
    if (app!=null)
      try {  app.synchronize(); }
      catch (Exception exc) {
        System.err.println("Error: can't to synchronize in any external application.");
        exc.printStackTrace();
      }
  }

  public synchronized void synchronize(boolean _remove){
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
      ExternalApp app = e.nextElement();
      try { app.synchronize (_remove); }
      catch (Exception exc) {
        System.err.println("Error: can't to synchronize in any external application.");
      } // Do not complain if it doesn't exist
    }
  }

  public synchronized void synchronize(){
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
      ExternalApp app = e.nextElement();
      try { app.synchronize (); }
      catch (Exception exc) {
        System.err.println("Error: can't to synchronize in any external application.");
      } // Do not complain if it doesn't exist
    }
  }

  public synchronized void setCommand(String _command){
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
      ExternalApp app = e.nextElement();
      try { app.setCommand (_command); }
      catch (Exception exc) {
        System.err.println("Error: can't to set Command in any external application.");
      } // Do not complain if it doesn't exist
    }
  }

  public synchronized void setCommand(String _appFile, String _command){
    ExternalApp app = getApplication (_appFile);
    if (app!=null)
      try {  app.setCommand(_command); }
      catch (Exception exc) {
        System.err.println("Error: can't to synchronize in any external application.");
        exc.printStackTrace();
      }
  }




//Set package size for Remote Application
  public synchronized void packageSize(String _appFile, double _package)  {
    ExternalApp app = getApplication (_appFile);
    if (app!=null)
       try {  app.packageSize (_package); }
       catch (Exception exc) {
         System.err.println("Error: can't to set package size in any external application.");
         exc.printStackTrace();
       }
   }

//Set package size for all Remote Application
  public synchronized void packageSize(double _package)  {
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
      ExternalApp app = e.nextElement();
      try { app.packageSize (_package); }
      catch (Exception exc) {
        System.err.println("Error: can't to set package size in any external application.");
      } // Do not complain if it doesn't exist
    }
}


  // Execute Remote _steps times,  output vars are returned from Remote Matlab
  // -------------------------------------------------------------------------
  public synchronized void update(String _command,String _outputvars,int _steps){
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
      ExternalApp app = e.nextElement();
      try { app.update (_command,_outputvars,_steps); }
      catch (Exception exc) {
        System.err.println("Error: can't to update in any external application.");
      } // Do not complain if it doesn't exist
    }
  }

  public synchronized void update(String _appFile,String _command,String _outputvars,int _steps){

    ExternalApp app = getApplication (_appFile);
    if (app!=null)
      try {  app.update (_command,_outputvars,_steps); }
      catch (Exception exc) {
        System.err.println("Error: can't to update in any external application.");
        exc.printStackTrace();
      }
  }

  public synchronized void update(String _command,String _outputvars,int _steps,int _package){
    for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
      ExternalApp app = e.nextElement();
      try { app.update (_command,_outputvars,_steps,_package); }
      catch (Exception exc) {
        System.err.println("Error: can't update in any external application.");
      } // Do not complain if it doesn't exist
    }
  }

  public synchronized void update(String _appFile,String _command,String _outputvars,int _steps,int _package){
    ExternalApp app = getApplication (_appFile);
    if (app!=null)
      try {  app.update (_command,_outputvars,_steps,_package); }
      catch (Exception exc) {
        System.err.println("Error: can't to update in any external application.");
        exc.printStackTrace();
      }
  }

  //Asynchronous Remote Simulink
   public synchronized void stepAS (double dt) {
     client._externalSetValues (true, null);
     for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
       ExternalApp app = e.nextElement();
       try { app.stepAS(dt); }
       catch (Exception exc) {
         System.err.println("Error stepping application : "+app);
         exc.printStackTrace();
       }
     }
     client._externalGetValues (true, null);
   }

   public synchronized void stepAS (String _appFile,double dt) {
     client._externalSetValues (true, null);
     ExternalApp app = getApplication (_appFile);
     if (app!=null)
       try { app.stepAS(dt); }
       catch (Exception exc) {
         System.err.println("Error stepping application : "+app);
         exc.printStackTrace();
       }
     client._externalGetValues (true, null);
   }


   public synchronized void stepAS (double dt, int _package){
     client._externalSetValues (true, null);
     for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
       ExternalApp app =  e.nextElement();
       try { app.stepAS(dt,_package); }
       catch (Exception exc) {
         System.err.println("Error stepping application : "+app);
         exc.printStackTrace();
       }
     }
     client._externalGetValues (true, null);
   }

   public synchronized void stepAS (String _appFile,double dt, int _package){
     client._externalSetValues (true, null);
     ExternalApp app = getApplication (_appFile);
     if (app!=null)
       try { app.stepAS(dt,_package); }
       catch (Exception exc) {
         System.err.println("Error stepping application : "+app);
         exc.printStackTrace();
       }
     client._externalGetValues (true, null);
   }

   public synchronized void haltStepAS(boolean _remove){
     for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
       ExternalApp app = e.nextElement();
       try { app.haltStepAS (_remove); }
       catch (Exception exc) {
         System.err.println("Error: can't halt update in any external application.");
       } // Do not complain if it doesn't exist
     }
   }


   public synchronized void haltStepAS(String _appFile,boolean _remove){
     client._externalSetValues (true, null);
     ExternalApp app = getApplication (_appFile);
     if (app!=null)
       try { app.haltStepAS (_remove); }
       catch (Exception exc) {
         System.err.println("Error: can't halt update in any external application.");
       } // Do not complain if it doesn't exist
   }


   public synchronized void haltUpdate(boolean _remove){
     for (Enumeration<ExternalApp> e = appList.elements(); e.hasMoreElements(); ) {
       ExternalApp app = e.nextElement();
       try { app.haltUpdate (_remove); }
       catch (Exception exc) {
         System.err.println("Error: can't halt update in any external application.");
       } // Do not complain if it doesn't exist
     }
   }

   public synchronized void haltUpdate(String _appFile,boolean _remove){
     client._externalSetValues (true, null);
     ExternalApp app = getApplication (_appFile);
     if (app!=null)
       try { app.haltUpdate (_remove); }
       catch (Exception exc) {
         System.err.println("Error: can't halt update in any external application.");
       } // Do not complain if it doesn't exist
   }

}
