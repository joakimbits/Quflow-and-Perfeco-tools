
/*
 * The org.colos.ejs.library package defines foundation classes for
 * Ejs simulations.
 */

package org.colos.ejs.library;

import java.awt.GraphicsConfiguration;
import java.util.*;

import javax.swing.JOptionPane;
import org.colos.ejs.library.external.*;
import org.colos.ejs.library.utils.TranslatorUtil;

public abstract class Model implements ExternalClient {
  static protected TranslatorUtil __translatorUtil;
  static private GraphicsConfiguration __graphicConfiguration=null; 

  protected String[] __theArguments = null;
  protected LauncherApplet __theApplet = null;
  public org.opensourcephysics.tools.ToolForData _tools = org.opensourcephysics.tools.ToolForData.getTool(); // Table, Data, and Fourier tool
  // List of possible external applications
  protected ExternalAppsHandler _external = new ExternalAppsHandler(this);
//  private boolean hasBeenReset = false;

  // Lists of variables for experiments
  public Memory _memory = new Memory();
  protected Input _input = new Input();
  protected List<?> _scheduledConditionsList = new ArrayList<Object>();
  protected Hashtable<?,?> _scheduledEventsList = new Hashtable<Object,Object>();
  
  // -----------------------------
  // Static methods
  // -----------------------------

  static public final TranslatorUtil _getTranslatorUtil() { return __translatorUtil; }
  
  static public final GraphicsConfiguration getGraphicsConfiguration() { return __graphicConfiguration; }
  
  static public final org.opensourcephysics.numerics.Function _ZERO_FUNCTION = new org.opensourcephysics.numerics.Function() {
    public double evaluate(double x) {
      return 0;
    }
  };
  
  /*
  static protected PrintUtil printUtil;

  /**
   * Prints a string on any of the printers attached to this computer
   *
  static public void _printerJob (String title, String output, int fontSize) {
    printUtil.printerJob (title, output, fontSize, 0.5, 0.5, 0.5, 0.5); 
  }
  
  /**
   * Prints a string on any of the printers attached to this computer.
   * Allows to specify the margins
   *
  static public void _printerJob (String title, String output, int fontSize, double top, double left, double bottom, double right)  { 
    printUtil.printerJob (title, output, fontSize, top, left, bottom, right);
  }
  */
  
  // -----------------------------
  // Methods for translation
  // -----------------------------

  /**
   * Returns a String property (as defined in the Translation tool) in the current Locale.
   * If not found, the property is returned
   * @return
   */
  public String _getStringProperty(String _property) { return __translatorUtil.translateString(_property); }
  
//  /**
//   * Returns a String property (as defined in the Translation tool) in the current Locale.
//   * If not found, the default value is returned
//   * @return
//   */
//  public String _getStringProperty(String _property, String _default) { return __translatorUtil.translateString(_property,_default); }

  //--------------------------------------------------------
  // Abstract methods
  //--------------------------------------------------------

  abstract public View _getView();

  public View getView() { return _getView(); } // backwards compatibility
  
  abstract public Simulation _getSimulation();

  public Simulation getSimulation() { return _getSimulation(); } // backwards compatibility

  abstract public int _getPreferredStepsPerDisplay();
  
  abstract public void _resetModel();

//  abstract public void _initializeSolvers();

  abstract public void _initializeModel();

  abstract public void _resetSolvers();

  abstract public void _stepModel();

  abstract public void _updateModel();

  abstract public void _freeMemory ();
  

  //---- Control of the Thread ----

  private _ControlSimulationExperimentClass _cSE = new _ControlSimulationExperimentClass();
  private class _ControlSimulationExperimentClass {
    public synchronized void _controlForSimulation() {
      try { wait(); }
      catch (Exception _exc) {}
    }
    public synchronized void _controlForExperiment(){
      notify();
    }
  }
  //---- End of Control of the Thread ----

  // --- Utilities ---
  
  public LauncherApplet _getApplet() { return __theApplet; }

  public void _play() { _getSimulation().play(); }

  public void _playAndWait(){ _play(); _cSE._controlForSimulation(); }

  public void _pause() { 
    _getSimulation().pause(); 
    _cSE._controlForExperiment(); 
  }

  public void _step() { _pause(); _getSimulation().step(); }

  public void _setFPS(int _fps) { _getSimulation().setFPS(_fps); }

  public void _setDelay(int _delay) { _getSimulation().setDelay(_delay); }

  public int _getDelay () {
    if (_getSimulation()!=null) return _getSimulation().getDelay();
    return 0;
  }

//  public boolean _hasBeenReset() { return this.hasBeenReset; }
  
  public void _reset() {
    _external.reset();
    _getSimulation().reset();
  }

  public void _initialize() { _external.reset(); _getSimulation().initialize(); }

  public boolean _isApplet()  { return __theApplet!=null; }
  
  public boolean _isPlaying() { return _getSimulation().isPlaying(); }

  public boolean _isPaused()  { return _getSimulation().isPaused(); }


  
  public void _resetView() {
    if (_getView()!=null) {
      _getView().reset();
      _getView().initialize();
    }
  }
  
  public void _clearView() { if (_getView()!=null) _getView().initialize(); }

  public void _setStepsPerDisplay(int _steps) { _setSPD(_steps); }

  public void _setSPD(int _steps) { _getSimulation().setStepsPerDisplay(_steps); }

  public void _setUpdateView(boolean _update) { _getSimulation().setUpdateView(_update); }

  public void _showDescriptionAtStartUp(boolean _show) { _getSimulation().showDescriptionAtStartUp(_show); }
  
  public void _showDescription(boolean _show) { _getSimulation().showDescription(_show); }

  public java.net.URL _getDescriptionPageURL (String pageName) { return _getSimulation().getDescriptionPageURL (pageName); }

  public void _setParentComponent (String _parent) { _getSimulation().setParentComponent (_parent); }

  abstract public org.colos.ejs.library.utils.HtmlPageInfo _getHtmlPageInfo (String _pageName, org.colos.ejs.library.utils.LocaleItem _localeItem);

  /**
   * Calls the class _getEjsResources() method.
   * This returns a set of all resource files required by the model to run (and included in the jar file)
   * @return
   */
  abstract public java.util.Set<String> _getClassEjsResources();
  
  /**
   * Calls the class _getEjsModel() method
   * @return The XML file that contains the model.
   */
  abstract public String _getClassEjsModel();

  /**
   * Calls the class _getModelDirectory() method
   * @return The base directory from which the model was compiled. May not start with a leading "/".
   */
  abstract public String _getClassModelDirectory();

  public String[] _getArguments() { return __theArguments; }
  
  public boolean _saveState (String _filename) { return _getSimulation().saveState (_filename); }

  public boolean _readState (String _filename) { return _getSimulation().readState (_filename); }

  public boolean _saveVariables (String _filename, String _varList) {
    return _getSimulation().saveVariables (_filename,_varList);
  }

  public boolean _saveVariables (String _filename, java.util.List<String> _varList) {
    return _getSimulation().saveVariables (_filename,_varList);
  }

  public boolean _readVariables (String _filename,String _varList) {
    return _getSimulation().readVariables (_filename,(java.net.URL) null,_varList);
  }

  public boolean _readVariables (String _filename,java.util.List<String> _varList) {
    java.net.URL codebase = null;
    if (__theApplet!=null) codebase = __theApplet.getCodeBase();
    return _getSimulation().readVariables (_filename,codebase,_varList);
  }

  public boolean _saveText (String _filename, String _text) {
    return _getSimulation().saveText (_filename, _text);
  }

  public boolean _saveText (String _filename, StringBuffer _text) {
    return _getSimulation().saveText (_filename, _text);
  }

  public String  _readText (String _filename) {
    return _getSimulation().readText (_filename);
  }

  public boolean _saveImage (String _filename, String _element) { return _getSimulation().saveImage (_filename, _element); }

  
  public void _alert(String _panel, String _title, String _message) {
    if (_getView()!=null) JOptionPane.showMessageDialog(_getView().getComponent(_panel), _message, _title, JOptionPane.INFORMATION_MESSAGE);
    else                  JOptionPane.showMessageDialog(_getSimulation().getParentComponent(),_message, _title, JOptionPane.INFORMATION_MESSAGE);
  }

  public String _format(double _value, String _pattern) {
    return new java.text.DecimalFormat (_pattern).format(_value);
  }
  
  public void _print(String _txt)   {
    if (_getView()!=null) _getView().print(_txt);
    else System.out.print (_txt);
  }

  public void _println(String _txt) {
    if (_getView()!=null) _getView().println(_txt);
    else System.out.print (_txt);
  }

  public void _println() {
    if (_getView()!=null) _getView().println();
    else System.out.println ();
  }

  public void _clearMessages() { if (_getView()!=null) _getView().clearMessages(); }

  
  
  
  public String _getParameter(String _name) {
    if (__theApplet!=null) return __theApplet.getParameter(_name);
    if (__theArguments==null) return null;
    for (int i=0; i<__theArguments.length;i++) {
      if (__theArguments[i].equals("-"+_name) && (i+1)<__theArguments.length) return __theArguments[i+1];
    }
    return null;
  }
  
  public boolean _setVariables (String _command, String _delim, String _arrayDelim) {
    return _getSimulation().setVariables (_command,_delim,_arrayDelim);
  }

  public boolean _setVariables (String _command) {
    return _getSimulation().setVariables (_command);
  }

  public String _getVariable (String _varName) {
    return _getSimulation().getVariable (_varName);
  }

  
  // -------------------------------- FKH 20060903 for javascript and java connection
  /*
  public void _ejsPopup (String _url) { _getSimulation().ejsPopup (_url); }

  public void _ejsEval (String _cmd) { _getSimulation().ejsEval (_cmd); }

  public void _ejsCommand (String[] _args) { _getSimulation().ejsCommand (_args); }

  public void _ejsCommand (String _function_name,String[] _args) { _getSimulation().ejsCommand (_function_name,_args); }

  */  
  // -------------------------------------------------- END FKH

  


  
  
  public void _onExit() {
    _getSimulation().onExit();
    _external.quit();
  }

  public void _resetIC() { _resetSolvers(); }

  // --- Implementation of ExternalClient ---

  //abstract public String _externalInitCommand(String _applicationFile); //Gonzalo 090610

  /**
   * Set the value of all the variables in the external application.
   * If _any is true, then set the values of all sessions.
   */
  //abstract public void _externalSetValues(boolean _any, ExternalApp _application); //Gonzalo 090610

  /**
   * Get the value of all the variables in the external application.
   * If _any is true, then get the values of all sessions.
   */
  //abstract public void _externalGetValues(boolean _any, ExternalApp _application); //Gonzalo 090610

  public synchronized void _externalGetValuesAndUpdate(boolean _any, ExternalApp _application) { 
//   _externalGetValues(_any,_application); //Gonzalo 090611
    _getSimulation().update();
  }

  // --- End of implementation of ExternalClient ---

} // End of class UnnamedModel

