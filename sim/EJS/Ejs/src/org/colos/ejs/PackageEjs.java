package org.colos.ejs;

import org.opensourcephysics.tools.minijar.MiniJar;
import java.util.*;

/**
 * This class packages Ejs
 * @author Francisco Esquembre
 *
 */
public class PackageEjs {
  static private final String VERBOSE = "";
  //static private final String VERBOSE = "-v";
  
  static private final String LOCALES = VERBOSE + 
  " -o distribution/bin/locales.jar -m .;ejs_lib.jar org.colos.ejs.library._EjsConstants"+
  " -s resources/es_ES -s resources/zh_TW -s resources/zh_CN -s resources/da_DK -s resources/fr_FR"+
  " -s resources/nl_NL -s resources/pl_PL -s resources/ru_RU -s resources/gr_GR -s resources/si_SI"+
  " -s resources/ca_CA" +
  " org/colos/ejs/osejs/++";

//  static final String LOOK_AND_FEEL = VERBOSE + 
//  " -o distribution/bin/lookAndFeel.jar  -m .;ejs_lib.jar org.colos.ejs.library._EjsConstants"+
//  " -s bin"+
//  " -x ++Thumbs.db"+
//  " com/digitprop/tonic/++";

  static final String EJS_CONSOLE = VERBOSE + 
  " -o distribution/EjsConsole.jar "+
  " -m .;bin/locales.jar;bin/ejs_lib.jar;bin/osp.jar"+
  " org.colos.ejs.osejs.EjsConsole"+
  " -s bin -s distribution/bin/osp.jar -c libraries/apache -c libraries/unpackaged/classes"+
  " -x ++Thumbs.db"+
  " org/colos/ejs/osejs/EjsConsole++.class org/colos/ejs/osejs/resources/++"+
  " org/opensourcephysics/resources/++ data/icons/EjsLogo.gif data/icons/ConsoleIcon.gif data/icons/edit.gif";

  static final String EJS_LIBRARY = VERBOSE + 
  "-o distribution/bin/ejs_lib.jar  -m . org.colos.ejs.library._EjsConstants"+
  " -s bin -s libraries/unpackaged/classes -s libraries/HotEqn.jar -s libraries/eJournalCommClasses.jar -c distribution/bin/osp.jar"+
  " -x distribution/bin/osp.jar -x ++Thumbs.db"+
  " -x org/colos/ejs/library/model_elements/++"+  // Exclude model elements created as part of this project
  " org/opensourcephysics/swing/++.class org/opensourcephysics/swing/images/++"+
  " org/opensourcephysics/numerics/++.class"+
  " org/opensourcephysics/tools/ToolForDataFull.class "+
  " org/colos/ejs/library/++ com/charliemouse/++.gif"+
  " libraries/eJournalCommClasses.jar libraries/HotEqn.jar";
  
  static final String EJS = VERBOSE +
  " -o distribution/bin/ejs.jar  -m .;ejs_lib.jar;osp.jar;bsh.jar org.colos.ejs.library._EjsConstants"+
  " -s bin -c libraries/apache -c libraries/unpackaged/classes -c libraries/eJournalCommClasses.jar"+
  " -c libraries/HotEqn.jar -c distribution/bin/osp.jar"+
  " -x distribution/bin/osp.jar -x distribution/bin/ejs_lib.jar -x org/colos/ejs/PackageEjs.class -x ++Thumbs.db"+
  " -x org/colos/ejs/library/model_elements/++"+  // Exclude model elements created as part of this project
  " -x org/colos/ejs/model_elements/++"+          // Exclude model elements created as part of this project
  " -f org/colos/ejs/model_elements/ModelElement.class" + // but force this interface
  " -f org/colos/ejs/model_elements/ModelElementsCollection.class" + // but force this interface
  " -f org/colos/ejs/model_elements/ModelElementSearch.class" + // but force this class
  " -f org/colos/ejs/model_elements/Utilities.class" + // but force this class
  " org/colos/ejs/++ data/++ com/++.properties com/hexidec/ekit/icons/++";
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    processCommand (LOCALES);
//    processCommand (LOOK_AND_FEEL);
    processCommand (EJS_CONSOLE);
    processCommand (EJS_LIBRARY);
    processCommand (EJS); // This one always after EJS_LIBRARY
    System.out.println ("Packaging of Ejs completed!");
    System.exit(0);
  }

  static void processCommand (String command) {
    System.out.println ("Processing "+command);
    String[] args = command.split(" ");
    // Remove ';' that should be blanks (needed for class paths in manifests) 
    for (int i=0; i<args.length; i++) args[i] = args[i].replace(';', ' ');
    MiniJar sj = new MiniJar(args);
    Set<String> missingSet = sj.compress();
    for (String missing : missingSet) System.out.println ("Missing file: "+missing); 
    System.out.println ("  ... Done processing "+command+"\n");
  }

}
