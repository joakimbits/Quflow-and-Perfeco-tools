package org.colos.ejs.external;

import java.util.*;
import java.io.*;
import org.colos.ejs.osejs.edition.Editor;
import java.net.URL;

public class BrowserForMatlab extends BrowserForExternal {
  // These static variables identify possible comments
  final static private int EJS_ID_LENGTH = 4;
  final static private String EJS_ID = "%ejs";
  final static private String VAR_ID = "variable";
  final static private String PARAM_ID = "parameter";
  final static private String MODEL_ID = "model";
  final static private String INPUT_ONLY_ID = "input";
  final static private String OUTPUT_ONLY_ID = "output";

  protected String mFile=null,mdlFile=null;
  protected boolean mFileExists=true;

  public boolean fileExists () { return mFileExists; }

  public StringBuffer generateCode (int _type) {
    //if (_type==Editor.GENERATE_JARS_NEEDED) return new StringBuffer("ejsExternal.jar;");
    //if (_type==Editor.GENERATE_DECLARATION) return new StringBuffer ("org.colos.ejs.external.EjsMatlab.class"); /Gonzalo 090610
    if (_type==Editor.GENERATE_DECLARATION) return new StringBuffer ("org.colos.ejs.library.external.EjsMatlab.class");
    if (_type==Editor.GENERATE_RESOURCES_NEEDED) {
      if (mdlFile!=null) return new StringBuffer(mFile + ";" + mdlFile + ".mdl;");
      else if (mFileExists) return new StringBuffer(mFile + ";");
      else return new StringBuffer();
    }
    return new StringBuffer();
  }

 /** This constructor reads an M file and creates a vector with a list of the
  *  variables/parameters of the model, including information about them,
  *  and their values.
  *  Vector sintax: name=value; % Comments // [Input] + [Output]
  */
 // Note: any change in this method must be matched with the method processMFile in
 // matlab.EjsMatlab

  public BrowserForMatlab (String _mFile, URL _codebase) {
    variablesList = new Vector<String>();
    _mFile = _mFile.trim();
    if (_mFile.toLowerCase().startsWith("<matlab")) {
      int index = _mFile.indexOf('>');
      if (index>0) _mFile = _mFile.substring(index+1);
      else _mFile = "";
    }
    mFile = _mFile.trim();
    if (mFile.length()<=0) return;
    String line = new String(), lineLowercase;
    String comment;

    try {
      URL url = new URL(_codebase, mFile);
      url.openStream(); // Check if the file exists
      BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
      while((line = in.readLine())!= null) {
        lineLowercase = line.toLowerCase().trim();
        int a = lineLowercase.lastIndexOf(EJS_ID);
        if (a > 0) {
          lineLowercase = lineLowercase.substring(a+EJS_ID_LENGTH);
          if (lineLowercase.lastIndexOf(VAR_ID)>0 || lineLowercase.lastIndexOf(PARAM_ID)>0) {
            if (lineLowercase.lastIndexOf(INPUT_ONLY_ID)>0)  comment = " // " + externalInputStr;
            else if (lineLowercase.lastIndexOf(OUTPUT_ONLY_ID)>0)  comment = " // " + externalOutputStr;
            else comment = " // " + externalInputStr + " + " + externalOutputStr;
            line = line.substring(0,a).trim() + comment;
            variablesList.addElement (new String (line));
          }
          else if (lineLowercase.lastIndexOf(MODEL_ID)>0) {        // It's a model
            // Get the model's name. No extra data
            StringTokenizer aux = new StringTokenizer (line, "= ;");
            aux.nextToken (); // ignore model's variable name
            String model = aux.nextToken().trim();
            model = model.substring(1, model.length()-1); // remove quotes
            mdlFile = model.substring(0,model.lastIndexOf('.'));
            int index = mFile.lastIndexOf('/');
            if (index>=0) mdlFile = mFile.substring (0,index+1) + mdlFile;
          }
        }
      }
      in.close();
    } catch (Exception exc) { mFileExists = false; }
  }

} // End of class

