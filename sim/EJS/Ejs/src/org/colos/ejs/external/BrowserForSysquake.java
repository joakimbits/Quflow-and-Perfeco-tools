package org.colos.ejs.external;

import java.util.*;
import org.colos.ejs.osejs.edition.Editor;
import com.calerga.sysquake.SysquakeLink;
import javax.swing.JComponent;
import org.colos.ejs.osejs.utils.EditorForVariables;
import java.net.URL;

public class BrowserForSysquake extends BrowserForExternal {
  private SysquakeLink sqlink=null;
  private String sqFile=null;
  private int sqID = -1;
  protected boolean fileExists=true;

  static boolean isSysquakeFile (String filename) {
    filename = filename.toLowerCase();
    if (filename.startsWith("<sysquake")) return true;
    if (filename.endsWith(".sq")) return true;
    return false;
  }

  /** This constructor launches Sysquake to read an SQ file and
   * obtain the list of variables from it.
   */

   public BrowserForSysquake (String _sqFile, URL _codebase) {
     variablesList = new Vector<String>();
     sqFile = _sqFile.trim();
     if (sqFile.toLowerCase().startsWith("<sysquake")) return;
     try {
       URL url = new URL(_codebase, sqFile);
//       System.out.println ("Reading from "+url.toString());
       url.openStream(); // Check if the file exists
     }
     catch (Exception exc) { fileExists = false; }
   }

  public StringBuffer generateCode (int _type) {
    //if (_type==Editor.GENERATE_JARS_NEEDED) return new StringBuffer("_library/ejsExternal.jar;");
    if (_type==Editor.GENERATE_DECLARATION) return new StringBuffer ("org.colos.ejs.external.EjsSysquake.class");
    if (_type==Editor.GENERATE_RESOURCES_NEEDED) {
      if (sqFile!=null && !sqFile.toLowerCase().startsWith("<sysquake")) return new StringBuffer(sqFile + ";");
      return new StringBuffer();
    }
    return new StringBuffer();
  }

  public boolean fileExists () { return fileExists || sqFile.toLowerCase().startsWith("<sysquake"); }

//  public String addPreviousCode (String _app, int _type) {
//    if (_type==Editor.GENERATE_EXTERNAL_IN)
//      return "      java.util.Vector "+_app+"NamesVector  = new java.util.Vector();\n"
//           + "      java.util.Vector "+_app+"ValuesVector = new java.util.Vector();\n";
//    return "";
//  }
//
//  public String addPostCode (String _app, int _type) {
//    if (_type==Editor.GENERATE_EXTERNAL_IN)
//      return "      ((org.colos.ejs.external.EjsSysquake) "+_app+").setValues("+_app+"NamesVector,"+_app+"ValuesVector);\n";
//    return "";
//  }

  public String addToInputCode (String _name, String _type, int _dim, String _connectedTo, String _app, String _info) {
    if (_connectedTo.length()<=0) return "";
    if (!isInputVariable(_connectedTo)) return "";
    if ( (_type.equals("String") && _dim<1) || (_type.equals("double") && _dim<3) ) {
      if (_type.equals("double") && _dim==0)
        return "      " + _app+"NamesVector.add(\""+_connectedTo+"\");"+_app+"ValuesVector.add(new Double("+_name+")); // " + _info;
      return "      " + _app+"NamesVector.add(\""+_connectedTo+"\");"+_app+"ValuesVector.add("+_name+"); // " + _info;
    }
    return "";
  }


  private void prepareToEdit () {
    if (sqFile.toLowerCase().startsWith("<sysquake>")) return;
    String dir = System.getProperty("user.home").replace('\\','/'); // "Ejs.home");
    if (!dir.endsWith("/")) dir += "/";
    if (org.opensourcephysics.display.OSPRuntime.isWindows()) {
      sqlink = new SysquakeLink(dir+"_library/external/SysquakeLink.dll");  // Connect to Sysquake (Windows)
    }
    else {
      sqlink = new SysquakeLink(dir+"_library/external/libSysquakeLink.jnilib"); // Connect to Sysquake (Unix)
    }
    try { SysquakeLink.connect(); }
    catch (Exception exc) { exc.printStackTrace(); }
    String homeDir = System.getProperty("user.home").replace('\\','/');
    if (!homeDir.endsWith("/")) homeDir = homeDir + "/";
    try {
      sqID = SysquakeLink.open(homeDir+sqFile);
     String[] varNames = SysquakeLink.variableNames(sqID);
      if (varNames!=null) for (int i=0,n=varNames.length; i<n; i++) {
        if (!varNames[i].startsWith("_")) variablesList.addElement (infoFor(sqID,varNames[i]));
      }
      SysquakeLink.disconnect();
      SysquakeLink.quit();
    }
    catch (Exception exc) { exc.printStackTrace(); }
  }

  public String edit (String _property, String _name, JComponent _target, String _value) {
    if (sqlink==null) prepareToEdit();
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

  static private String infoFor (int _sqId,String _name) {
    String type=null;
    Object val = null;
    try {
      SysquakeLink.execute("ejsType = ejsVariableType('"+_name+"');");
      Object obj = SysquakeLink.lmeVariableValue("ejsType");
      if (obj!=null) {
        String theType = obj.toString();
        if ("InputOnly".equalsIgnoreCase(theType)) type = externalInputStr;
        else if ("OutputOnly".equalsIgnoreCase(theType)) type = externalOutputStr;
        else type = externalInputStr + " + " + externalOutputStr;
      }
      else type = externalInputStr + " + " + externalOutputStr;
      val = SysquakeLink.variableValue(_sqId, _name);
    }
    catch (Exception exc) { exc.printStackTrace(); }

    if (val==null) return _name + "// " + type;

    if (val instanceof Double)  return _name + " = "+((Double)val).doubleValue()    + " // double "  + type;
    if (val instanceof Integer) return _name + " = "+((Integer)val).intValue()      + " // int "     + type;
    if (val instanceof Boolean) return _name + " = "+((Boolean)val).booleanValue()  + " // boolean " + type;
    if (val instanceof String)  return _name + " = "+((String)val)                  + " // String "  + type;
    if (val instanceof char[])  return _name + " = "+((char[])val).toString()       + " // String "  + type;
    if (val instanceof int[]) {
      int[] array = (int[]) val;
      return _name + " // int[" + array.length + "] " + type;
    }
    if (val instanceof int[][]) {
      int[][] array = (int[][]) val;
      return _name + " // int[" + array.length + "] [" + array[0].length + "] " + type;
    }
    if (val instanceof double[]) {
      double[] array = (double[]) val;
      return _name + " // double[" + array.length + "] " + type;
    }
    if (val instanceof double[][]) {
      double[][] array = (double[][]) val;
      if (array.length==1) return _name + " // double[" + array.length + "][" + array[0].length + "] | double["
          + array[0].length + "] " + type;
      return _name + " // double[" + array.length + "] [" + array[0].length + "] " + type;
    }
    return _name + " = " + val.toString() + " // " + type;
  }

} // End of class

