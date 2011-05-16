package org.colos.ejs.external;

import org.colos.ejs.osejs.edition.Editor;
import java.net.URL;

public class BrowserForRemoteMatlab extends BrowserForMatlab {

  public BrowserForRemoteMatlab (String _mFile, URL _codebase) {
   super (_mFile,_codebase);
  }

  static boolean isRemoteMatlabFile (String filename) {
    filename = filename.toLowerCase();
    if (filename.startsWith("<matlab:") || filename.startsWith("<matlabas:")) return true;
    return false;
  }

  public StringBuffer generateCode (int _type) {
    //if (_type==Editor.GENERATE_JARS_NEEDED) return new StringBuffer("ejsExternal.jar;");
    if (_type==Editor.GENERATE_DECLARATION) return new StringBuffer ("org.colos.ejs.external.EjsRemoteMatlab.class");
    if (_type==Editor.GENERATE_RESOURCES_NEEDED && mFileExists) return new StringBuffer(mFile + ";");
    return new StringBuffer();
  }

} // End of class

