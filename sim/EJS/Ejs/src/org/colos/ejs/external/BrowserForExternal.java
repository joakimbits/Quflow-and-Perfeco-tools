package org.colos.ejs.external;

import java.util.*;
import javax.swing.JComponent;
import org.colos.ejs.osejs.utils.EditorForVariables;
import org.colos.ejs.osejs.utils.ResourceUtil;

public abstract class BrowserForExternal {
  static private ResourceUtil res = new ResourceUtil ("Resources");
  static protected final String externalInputStr = res.getString("External.Input");
  static protected final String externalOutputStr = res.getString("External.Output");

  protected Vector<String> variablesList;  // For the standard implementation

  public static BrowserForExternal readFile (String _file, java.net.URL _userCodebase) {    
    if (BrowserForScilab.isScilabFile(_file))     return new BrowserForScilab(_file,_userCodebase);
    if (BrowserForSysquake.isSysquakeFile(_file)) return new BrowserForSysquake(_file,_userCodebase);
    // Order is important in what follows!
    if (BrowserForRemoteSimulink.isRemoteSimulinkFile(_file)) return new BrowserForRemoteSimulink(_file); // Gonzalo 060420
    if (BrowserForRemoteMatlab.isRemoteMatlabFile(_file)) return new BrowserForRemoteMatlab(_file,_userCodebase); // Gonzalo 060420
    if (BrowserForSimulink.isSimulinkFile(_file)) return new BrowserForSimulink(_file,_userCodebase);
    return new BrowserForMatlab(_file,_userCodebase);
  }

  abstract public StringBuffer generateCode (int _type);
  abstract public boolean fileExists ();

  // Standard behaviour (used by Matlab)

  public String edit (String _property, String _name, JComponent _target, String _value) {
    String option = EditorForVariables.edit (variablesList, _property,_name,_target,_value);
    if (option!=null) {
      int pos1 = option.indexOf('=');
      int pos2 = option.indexOf("//");
      if (pos1>=0) option = option.substring(0,pos1).trim();
      else if (pos2>=0) option = option.substring(0,pos2).trim();
      else option = option.trim();
    }
    return option;
  }

// Methods specially suited for BrowserForSimulink

  public boolean isChanged () { return false; }

  public void setChanged (boolean _change) { }

  public String saveString () { return ""; }

  public void readString (String _code) { }

  public Vector<String> prepareInitCode () { return new Vector<String>(); }

  public Vector<String> addToInitCode (String _name, String _connected, double _value) { return new Vector<String>(); }

// End of Methods specially suited for BrowserForSimulink

  // Methods specially suited for BrowserForSysquake

  public String addPreviousCode (String _app, int _type) { return ""; }
  public String addPostCode (String _app, int _type) { return ""; }

  // End of Methods specially suited for BrowserForSysquake

  public String addToInputCode (String _name, String _type, int _dim, String _connectedTo, String _app, String _info) {
    if (_connectedTo.length()<=0) return "";
    if (!isInputVariable(_connectedTo)) return "";
    if ( (_type.equals("String") && _dim<1) || (_type.equals("double") && _dim<3) ) 
      return "      " + _app+".setValue(\""+_connectedTo+"\","+_name+"); // " + _info;
    return "";
  }

  public String addToOutputCode (String _name, String _type, int _dim, String _connectedTo, String _app, String _info) {
    if (_connectedTo.length()<=0) return "";
    if (!isOutputVariable(_connectedTo)) return "";
    String line="";
    if (_type.equals("String") && _dim<1) {
      line = "      " + _name + " = "+ _app + ".getString(\""+_connectedTo+"\"); // "+_info;
    }
    else if (_type.equals("double") && _dim<3) {
      line = "      "+_name+" = "+ _app + ".";
      switch (_dim) {
        case 0 : line += "getDouble"; break;
        case 1 : line += "getDoubleArray"; break;
        case 2 : line += "getDoubleArray2D"; break;
        }
      line += "(\""+_connectedTo+"\"); // "+_info;
    }
    return line;
  }

  protected boolean isInputVariable (String _connected) {
    if (_connected.trim().length()<=0) return false;
    boolean isInput=true;
    for (int j=0, n=variablesList.size(); j<n; j++) {
      String varLine = variablesList.get(j);
      String varName;
      int pos1 = varLine.indexOf('=');
      int pos2 = varLine.indexOf("//");
      if (pos1>=0) varName = varLine.substring(0,pos1).trim();
      else if (pos2>=0) varName = varLine.substring(0,pos2).trim();
      else varName = varLine.trim(); // But this is really a syntax error
      if (varName.equals(_connected) && pos2>=0) {
        varLine = varLine.substring(pos2);
        isInput = (varLine.indexOf(externalInputStr)>=0);
      }
    }
    return isInput;
  }

  protected boolean isOutputVariable (String _connected) {
    if (_connected.trim().length()<=0) return false;
    boolean isOutput=true;
    for (int j=0, n=variablesList.size(); j<n; j++) {
      String varLine = variablesList.get(j);
      String varName;
      int pos1 = varLine.indexOf('=');
      int pos2 = varLine.indexOf("//");
      if (pos1>=0) varName = varLine.substring(0,pos1).trim();
      else if (pos2>=0) varName = varLine.substring(0,pos2).trim();
      else varName = varLine.trim(); // But this is really a syntax error
      if (varName.equals(_connected) && pos2>=0) {
        varLine = varLine.substring(pos2);
        isOutput = (varLine.indexOf(externalOutputStr)>=0);
      }
    }
    return isOutput;
  }

} // End of class

