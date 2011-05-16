package org.colos.ejs.external;

import org.colos.ejs.osejs.edition.Editor;
import java.net.URL;
import org.opensourcephysics.tools.*;

public class BrowserForScilab extends BrowserForExternal {
  private String sciFile;

  public BrowserForScilab (String _sciFile, URL _codebase) {
    sciFile = _sciFile.trim();
    if (sciFile.toLowerCase().startsWith("<scilab")) {
      int index = sciFile.indexOf('>');
      if (index>0) sciFile = sciFile.substring(index+1).trim();
      else sciFile = "";
    }
    if (sciFile.length()<=0) sciFile = null;
  }

  static boolean isScilabFile (String filename) {
    filename = filename.toLowerCase();
    if (filename.startsWith("<scilab")) return true;
    if (filename.endsWith(".sci")) return true;
    if (filename.endsWith(".cos")) return true;
    return false;
  }

  public boolean fileExists () {
    if (sciFile==null) return true;
    return (ResourceLoader.getResource(sciFile)!=null);
  }

  public StringBuffer generateCode (int _type) {
    //if (_type==Editor.GENERATE_JARS_NEEDED) return new StringBuffer("ejsExternal.jar;");
    if (_type==Editor.GENERATE_DECLARATION) return new StringBuffer ("org.colos.ejs.external.EjsScilab.class");
    if (_type==Editor.GENERATE_RESOURCES_NEEDED && sciFile!=null) return new StringBuffer(sciFile + ";");
    return new StringBuffer();
  }

} // End of class

