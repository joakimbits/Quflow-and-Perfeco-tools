/**
 * The package contains the main functionality of Osejs
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs;

import org.colos.ejs.library._EjsConstants;
import org.colos.ejs.library.utils.LocaleItem;
import org.colos.ejs.osejs.utils.*;
import org.colos.ejs.osejs.edition.Editor;
import org.colos.ejs.osejs.edition.TabbedEvolutionEditor;
import org.colos.ejs.osejs.edition.html.HtmlEditor;
import org.colos.ejs.osejs.edition.html.OneHtmlPage;
import org.opensourcephysics.tools.minijar.MiniJar;
import org.opensourcephysics.tools.minijar.PathAndFile;
import org.opensourcephysics.controls.Cryptic;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.awt.*;
import javax.swing.*;


//--------------------

public class Generate {
  static private final ResourceUtil res    = new ResourceUtil("Resources");
  
  /**
   * Generates a simulation and all its auxiliary files
   * @param _ejs Osejs
   * @param _filename String The Java file for the simulation
   * @param _relativePath String The full path of the XML file
   * @return File the main class file generated
   */
  static public File generate (Osejs _ejs) {

    String binDirPath = FileUtils.getPath(_ejs.getBinDirectory());
    String srcDirPath = FileUtils.getPath(_ejs.getSourceDirectory());

    // Get the filename and relative path information
    final File xmlFile = _ejs.getCurrentXMLFile();
    final TwoStrings fileNameAndAxtension = FileUtils.getPlainNameAndExtension(xmlFile);
    final String filename = fileNameAndAxtension.getFirstString(); // the plain filename
    final String relativePath = FileUtils.getRelativePath(xmlFile,srcDirPath,false); // The relative path of the XML file
    String parentPath; // The relative path of the parent directory
    String pathToLib = ""; // Levels up to get to the _ejs_library

    int index = relativePath.lastIndexOf('/');
    if (index>=0) parentPath = relativePath.substring(0,index+1); // including the '/'
    else parentPath = "";
    char[] pathChars = relativePath.toCharArray();
    for (int i=0; i<pathChars.length; i++) if (pathChars[i]=='/') pathToLib += "../";
    if (_ejs.isVerbose()) {
      System.out.println("Generating:\n");
      System.out.println("  name "+filename);
      System.out.println("  relative path "+relativePath);
      System.out.println("  parent path "+parentPath);
      System.out.println("  path to lib "+pathToLib);
    }
    if (_ejs.isJustCompiling()) System.out.println(relativePath+": "+res.getString("Generate.Compiling"));
    // Prepare to start
    _ejs.getOutputArea().println(res.getString("Generate.Compiling")+ " "+ filename + "...");
    File generationDirectory = new File(_ejs.getOutputDirectory(),parentPath);
    generationDirectory.mkdirs();
    
   String classname = OsejsCommon.getValidIdentifier(filename);  // a legal name for the Java class to create. Must match PackageBuilder !
    switch (classname.length()) { // make sure the name is at least three characters long. CreateTempFile requires this
      case 1 : classname += "__"; break;
      case 2 : classname += "_"; break;
      default: break; // do nothing
    }
    String packageName = getPackageName (xmlFile,classname,parentPath); // a legal name for the package of the class to create
    Set<PathAndFile> jarList = getPathAndFile(_ejs, xmlFile.getParentFile(), parentPath, _ejs.getSimInfoEditor().getUserJars()); // user JAR files
    Set<PathAndFile> resList = getPathAndFile(_ejs, xmlFile.getParentFile(), parentPath, _ejs.getSimInfoEditor().getAuxiliaryFilenames()); // auxiliary files (includes the translation files)
    
    File jarFile = new File (generationDirectory,filename+".jar");

    StringBuffer buffer = new StringBuffer(); // hold the execution path // getExecPath(jarFile,binDirPath,srcDirPath,outDirPath,jarList,false);
    buffer.append(binDirPath+"osp.jar"+File.pathSeparator);
    buffer.append(binDirPath+"ejs_lib.jar"+File.pathSeparator);
    // Add extension files
    String extDirPath = binDirPath + OsejsCommon.EXTENSIONS_DIR_PATH + "/";
    for (PathAndFile paf : OsejsCommon.getLibraryFiles(new File (_ejs.getBinDirectory(),OsejsCommon.EXTENSIONS_DIR_PATH))) {
      buffer.append(extDirPath+paf.getPath()+File.pathSeparator);
    }
    buffer.append(FileUtils.getPath(jarFile)+File.pathSeparator);
    for (PathAndFile paf : jarList) buffer.append(srcDirPath+paf.getPath()+File.pathSeparator);
    //_ejs.setExecutionParameters (metadata.getClassname(),buffer.toString());    
 
    Metadata metadata = new Metadata(_ejs.getSimInfoEditor().saveString(), 
        packageName +"."+classname, 
        buffer.toString(), 
        jarList,
        _ejs.getViewEditor().generateCode(Editor.GENERATE_RESOURCES_NEEDED_BY_PACKAGE,"").toString(),
        _ejs.getModelEditor().getElementsEditor().generateCode(Editor.GENERATE_RESOURCES_NEEDED_BY_PACKAGE,"").toString(),
        _ejs.getViewEditor().getTree().getMainWindowDimension());

    // Here we go
    File classesDir = null;
    try {
      String mainFrame = _ejs.getViewEditor().generateCode(Editor.GENERATE_MAIN_WINDOW, "").toString().trim();
      if (mainFrame.length()<=0) mainFrame = "\"EmptyFrame\"";
      // Create and save the Java files
      java.util.List<File> generatedFiles = new ArrayList<File>();
      String qualifiedClassname;
      int indexOfDot = packageName.lastIndexOf('.');
      if (indexOfDot>=0) qualifiedClassname = packageName.substring(index+1) +"/"+classname;
      else qualifiedClassname = packageName + "/" + classname;
      generatedFiles.add(FileUtils.saveToFile(new File(generationDirectory, qualifiedClassname + ".java"), null, // the model file
        generateModel (_ejs,classname,packageName,relativePath,parentPath,generationDirectory,resList,mainFrame)));
      generatedFiles.add(FileUtils.saveToFile(new File(generationDirectory, qualifiedClassname + "Simulation.java"), null, // the simulation file
        generateSimulation(_ejs,classname,packageName, filename, mainFrame)));
      generatedFiles.add(FileUtils.saveToFile(new File(generationDirectory, qualifiedClassname + "View.java"), null, // the view file
        generateView(_ejs,classname,packageName)));
      generatedFiles.add(FileUtils.saveToFile(new File(generationDirectory, qualifiedClassname + "Applet.java"), null, // the Applet file
        generateApplet(_ejs,classname, packageName, parentPath, mainFrame)));
      //CJB for collaborative
      if(_ejs.getSimInfoEditor().addAppletColSupport()){
      generatedFiles.add(FileUtils.saveToFile(new File(generationDirectory, qualifiedClassname + "AppletStudent.java"), null, // the AppletStudent file
    	generateAppletStudent(_ejs,classname, packageName, parentPath, mainFrame)));
      }
      //CJB for collaborative
      
      // Compilation
      classesDir = File.createTempFile(classname, ".tmp", _ejs.getOutputDirectory()); // Get a unique temporary file
      classesDir.delete(); // remove the created file, we were only interested in getting a unique filename
      if (!classesDir.mkdirs()) {
        _ejs.getOutputArea().println (res.getString("Package.NotTempDirError")+" "+FileUtils.getPath(classesDir));
        if (_ejs.isJustCompiling()) {
          String message = res.getString("Package.NotTempDirError")+" "+FileUtils.getPath(classesDir);
          System.out.println(message);
          FileUtils.saveToFile(new File (generationDirectory,"error.txt"),null, message+"\n");
        }
        return null;
      }

      // Add language resources, if needed
//      Set<File> languageResourcesFiles = new HashSet<File>(); // Keep the list of them so that they will be added to packages
      if (_ejs.getSimInfoEditor().addTranslatorTool()) {
        File resourcesDir = new File(classesDir,packageName.replace('.', '/'));
        for (LocaleItem item : _ejs.getTranslationEditor().getDesiredTranslations()) {
          String lrFilename = item.isDefaultItem() ? classname + ".properties" : classname + "_"+item.getKeyword()+".properties";
          FileUtils.saveToFile(new File(resourcesDir,lrFilename), LocaleItem.getCharset(), _ejs.getTranslationEditor().getResources(item));
        }
      }

      String genDirPath = FileUtils.getPath(generationDirectory);
      java.util.List<File> compileFiles = new ArrayList<File>(generatedFiles);
      // Add the external libraries
      String externalFiles = _ejs.getModelEditor().getLibraryEditor().generateCode(Editor.GENERATE_RESOURCES_NEEDED, "").toString();
      StringTokenizer tkn = new StringTokenizer(externalFiles,";");
      while (tkn.hasMoreTokens()) {
        String externalFilename =  tkn.nextToken();
        File externalFile = ResourceLoader.getResource(externalFilename).getFile();
        if (externalFile.exists()) compileFiles.add(externalFile);
        else {
          _ejs.getOutputArea().println (res.getString("Generate.JarFileResourceNotFound")+" "+externalFilename);
          if (_ejs.isJustCompiling()) System.out.println(relativePath+": "+res.getString("Generate.JarFileResourceNotFound")+" "+externalFilename);
        }
      }
      
      boolean ok = compile(_ejs,FileUtils.getPath(classesDir),compileFiles,getClasspath(_ejs.getBinDirectory(),binDirPath,srcDirPath,jarList));

      // Remove Java files, if configured to do so
      if (_ejs.getOptions().removeJavaFile()) for (File file : generatedFiles) file.delete();
      else for (File file : generatedFiles) metadata.addFileCreated(file,genDirPath);

      if (!ok) { // Compilation failed
        String message = res.getString("Generate.CompilationError");
        _ejs.getOutputArea().println (message);
        if (_ejs.isJustCompiling()) {
          System.out.println(relativePath+": "+message);
          FileUtils.saveToFile(new File (generationDirectory,"error.txt"),null, message+FileUtils.getPath(xmlFile)+"\n");
        }
        JarTool.remove(classesDir);
        return null;
      }

      // Compress the classes directory
      if (!JarTool.compress(classesDir, jarFile, null)) {
        _ejs.getOutputArea().println (res.getString("Package.JarFileNotCreated")+" "+FileUtils.getPath(jarFile));
        if (_ejs.isJustCompiling()) {
          String message = res.getString("Package.JarFileNotCreated")+" "+FileUtils.getPath(jarFile);
          System.out.println(message);
          FileUtils.saveToFile(new File (generationDirectory,"error.txt"),null, message+"\n");
        }
        JarTool.remove(classesDir);
        return null;
      }
      metadata.addFileCreated(jarFile,genDirPath);
      JarTool.remove(classesDir);
      _ejs.getOutputArea().println (res.getString("Generate.CompilationSuccessful"));
      
      // Generate HTML files
      buffer = new StringBuffer (); // holds the archive tag string
      buffer.append("common.jar,"+parentPath+filename+".jar"+",");
      for (PathAndFile paf : jarList) buffer.append(paf.getPath()+",");
      String archiveTag = buffer.toString();
      if (archiveTag.endsWith(",")) archiveTag = archiveTag.substring(0,archiveTag.length()-1);
      if (_ejs.getOptions().generateHtml()!=EjsOptions.GENERATE_NONE) {
        Hashtable<String,StringBuffer> htmlTable = new Hashtable<String,StringBuffer>();
        switch (_ejs.getOptions().generateHtml()) {
          case EjsOptions.GENERATE_ONE_PAGE : 
            addNoFramesHtml(htmlTable, _ejs,filename,classname,packageName,pathToLib,archiveTag); 
            break;
          case EjsOptions.GENERATE_LEFT_FRAME : 
          case EjsOptions.GENERATE_TOP_FRAME : 
            addFramesHtml(htmlTable, _ejs,filename,classname,packageName,pathToLib,archiveTag); 
            break;
        }
        // In any case, add all HTML files
        addDescriptionHtml(htmlTable, _ejs,filename,classname,packageName,pathToLib,archiveTag);
        // Now, save them all
        for (String key : htmlTable.keySet()) 
          FileUtils.saveToFile (metadata.addFileCreated(new File (generationDirectory,filename+key+".html"),genDirPath), null, 
                                htmlTable.get(key).toString());
      }
//      switch (_ejs.getOptions().generateHtml()) {
//        case org.colos.ejs.osejs.EjsOptions.GENERATE_ONE_PAGE :
//          FileUtils.saveToFile(metadata.addFileCreated(new File(generationDirectory,filename+".html"),genDirPath), null, 
//                               generateHtmlNoFrames(_ejs,filename,classname,packageName,pathToLib,archiveTag));
//          break;
//        case org.colos.ejs.osejs.EjsOptions.GENERATE_TOP_FRAME :
//        case org.colos.ejs.osejs.EjsOptions.GENERATE_LEFT_FRAME :
//          Hashtable<String,StringBuffer> htmlTable = generateHtml(_ejs,filename,classname,packageName,pathToLib,archiveTag);
//          for (String key : htmlTable.keySet()) 
//            FileUtils.saveToFile (metadata.addFileCreated(new File (generationDirectory,filename+key+".html"),genDirPath), null, 
//                                  htmlTable.get(key).toString());
//          break;
//        default : break; // Do nothing
//      }

      /*
      if (_ejs.getOptions().eMersionEnabled()) {
        File eMersionFile = new File (generationDirectory,filename+"_Emersion.html");
        FileUtils.saveToFile (eMersionFile, generateEmersionHtml(_ejs,filename,classname,packageName,
            _ejs.getOptions().eMersionGetURL(),pathToLib,buffer.toString()) );
        metadata.addFileCreated(eMersionFile);
      }
*/

      // Include the XML model
      if (_ejs.getOptions().includeModel()) {
        String modelName = filename+"."+fileNameAndAxtension.getSecondString(); // Use same extension as the original
        JarTool.copy(xmlFile, metadata.addFileCreated(new File (generationDirectory,modelName),genDirPath));
      }

      // Copy auxiliary files
      Set<String> copyFiles = new HashSet<String>();
      Set<String> missingAuxFiles = new HashSet<String>();
      
      // Get a unique list of files to copy (so that not to repeat a file)
      for (String auxPath : metadata.getAuxiliaryFiles()) {
        File auxFile;
        if (auxPath.startsWith("./")) auxFile = new File(xmlFile.getParentFile(),auxPath.substring(2));
        else auxFile = new File(_ejs.getSourceDirectory(),auxPath);
        if (!auxFile.exists()) missingAuxFiles.add(auxPath);
        else if (auxFile.isDirectory()) { // It is a complete directory
          if (!auxPath.endsWith("/")) auxPath += "/";
          for (File file : JarTool.getContents(auxFile)) copyFiles.add(auxPath+"/"+FileUtils.getRelativePath(file, auxFile, false));
        }
        else copyFiles.add(auxPath);
      }
      // Now do the copying
      for (String auxPath : copyFiles) {
        File sourceFile, targetFile;
        if (auxPath.startsWith("./")) {
          String path = auxPath.substring(2);
          sourceFile = new File(xmlFile.getParentFile(),path);
          targetFile = new File(generationDirectory,path);
        }
        else {
          sourceFile = new File(_ejs.getSourceDirectory(),auxPath);
          targetFile = new File(_ejs.getOutputDirectory(),auxPath);
        }
        if (!sourceFile.exists()) missingAuxFiles.add(auxPath);
        else JarTool.copy(sourceFile, targetFile);
      }
      
      OsejsCommon.warnAboutFiles(_ejs.getMainPanel(),missingAuxFiles,"SimInfoEditor.RequiredFileNotFound");

      // Add all the files in the library directory 
      File binConfigDir = new File (_ejs.getBinDirectory(),OsejsCommon.CONFIG_DIR_PATH);
      String binConfigDirPath = FileUtils.getPath(binConfigDir);
      for (File file : JarTool.getContents(new File(binConfigDir,OsejsCommon.EJS_LIBRARY_DIR_PATH))) {
        String destName = FileUtils.getRelativePath(file, binConfigDirPath, false);
        JarTool.copy(file, new File (_ejs.getOutputDirectory(),destName));
      }

      // Overwrite files in the library directory with user defined files (if any)
      String configDirPath = FileUtils.getPath(_ejs.getConfigDirectory());
      for (File file : JarTool.getContents(new File(_ejs.getConfigDirectory(),OsejsCommon.EJS_LIBRARY_DIR_PATH))) {
        String destName = FileUtils.getRelativePath(file, configDirPath, false);
        JarTool.copy(file, new File (_ejs.getOutputDirectory(),destName));
      }
      
      // Save the meta data
      File metadataFile = new File (generationDirectory,filename+Metadata.EXTENSION);
      metadata.saveToFile(metadata.addFileCreated(metadataFile,genDirPath));

      // Report generation done correctly
      _ejs.getOutputArea().println(res.getString("Generate.GenerationOk"));
      if (_ejs.isJustCompiling()) System.out.println(relativePath+": "+res.getString("Generate.GenerationOk"));
      
      return metadataFile;
    }
    catch (IOException ex) {
      _ejs.getOutputArea().println(res.getString("Generate.GenerationError"));
      _ejs.getOutputArea().println("System says :\n  " + ex.getMessage());
      if (_ejs.isJustCompiling()) {
        String message = res.getString("Generate.GenerationError");
        System.out.println(relativePath+": "+message);
        try {  FileUtils.saveToFile(new File (generationDirectory,"error.txt"), null, message+FileUtils.getPath(xmlFile)+"\n"); }
        catch (Exception exc2) { exc2.printStackTrace(); }
      }
      if (classesDir!=null) JarTool.remove(classesDir);
      return null;
    }
  }

  /**
   * Compiles the generated Java files
   * @param _ejs Osejs The calling EJS
   * @param _javaFiles File[] The Java files for the simulation's code
   * @param _classpath String The class path to use
   * @return boolean true if successful, false otherwise.
   */
  static private boolean compile (Osejs _ejs, String _classesDirPath, java.util.List<File> _javaFiles, String _classpath) {
    int offset = _javaFiles.size();
    
    //CJB for collaborative
    for(int j=0;j<_javaFiles.size();j++)
    	if(_javaFiles.get(j).getPath().equals(""))
    		offset--;
    //CJB for collaborative
    
    String args[] = new String[7+offset];
    for (int i=0; i<offset; i++) args[i] = _javaFiles.get(i).getPath();
    args[offset] = "-classpath";
    args[offset+1] = _classpath;
    args[offset+2] = "-d";
    args[offset+3] = _classesDirPath;
    args[offset+4] = "-source";
    args[offset+5] = _ejs.getOptions().targetVM();
    args[offset+6] = "-Xlint:unchecked";
//    for (int i=0; i<args.length; i++) System.out.println ("Args["+i+"] = "+args[i]);
    boolean ok = com.sun.tools.javac.Main.compile(args,new PrintWriter(_ejs.getOutputArea()))==0;
    // if (!ok) JarTool.remove(_classesDir);
    return ok;
  }

  /**
   * Run a generated simulation locally.
   * @param _ejs Osejs The calling EJS
   * @param _filename String The name of the file to run
   * @return boolean true if successful, false otherwise.
   */
  static public boolean run (Osejs _ejs) {
    GeneratedUtil runable = new GeneratedUtil(_ejs);
    if (SwingUtilities.isEventDispatchThread()) {
      java.lang.Thread thread = new Thread(runable);
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.start();
    }
    else SwingUtilities.invokeLater(runable);
    return true;
  }

  // ----------------------------
  // Options of the package button
  // ----------------------------

  static private void addResources (Osejs _ejs, MiniJar _minijar) {
    _minijar.addDesired("org/opensourcephysics/resources/++");
    _minijar.addExclude("org/opensourcephysics/resources/tools/launcher++");
    _minijar.addDesired("org/colos/ejs/library/resources/++");
    if (!_ejs.getSimInfoEditor().addToolsForData()) {
      _minijar.addExclude("org/opensourcephysics/resources/tools/html/data_builder_help.html"); 
      _minijar.addExclude("org/opensourcephysics/resources/tools/html/data_tool_help.html"); 
      _minijar.addExclude("org/opensourcephysics/resources/tools/html/fit_builder_help.html"); 
      _minijar.addExclude("org/opensourcephysics/resources/tools/images/++"); 
    }
    _minijar.addExclude("org/opensourcephysics/resources/tools/html/translator_tool_help.html");
//    if (_ejs.getSimInfoEditor().addTranslatorTool()) {
//      _minijar.addForced("org/opensourcephysics/resources/tools/images/save.gif");
//    }
//    else {
//      _minijar.addExclude("org/opensourcephysics/resources/tools/html/translator_tool_help.html");
//    }
  }

  static public void packageCurrentSimulation(Osejs _ejs, File _targetFile) {
    ArrayList<PathAndFile> list = new ArrayList<PathAndFile>();
    File currentFile = _ejs.getCurrentMetadataFile();
    list.add(new PathAndFile(_ejs.getPathRelativeToSourceDirectory(currentFile),currentFile));
    packageSeveralSimulations(_ejs,list,_targetFile,true);
  }

//  static public void packageCurrentSimulationNO(Osejs _ejs, File _targetFile) {
//    _ejs.getExportDirectory().mkdirs(); // In case it doesn't exist
//    //System.out.println ("packaging "+_targetFile.getAbsolutePath());
//    _ejs.getOutputArea().message("Package.PackagingJarFile",_targetFile.getName());
//    // Prepare directory names
//    File metadataFile = _ejs.getCurrentMetadataFile();
////    String plainName = FileUtils.getPlainName(metadataFile);
//    File generationDir = metadataFile.getParentFile();
//    String relativePath = FileUtils.getRelativePath(generationDir, _ejs.getOutputDirectory(), false); 
//
//    // Read the meta data
//    Metadata metadata = Metadata.readFile(metadataFile, relativePath);
//    if (metadata==null) {
//      String[] message=new String[]{res.getString("Package.JarFileNotCreated"),res.getString("Package.IncorrectMetadata")};
//      JOptionPane.showMessageDialog(_ejs.getMainPanel(),message,res.getString("Package.Error"),JOptionPane.WARNING_MESSAGE);
//      return;
//    }
//
//    // Create an instance of MiniJar and prepare it
//    MiniJar minijar= new MiniJar();
//    minijar.setOutputFile(_targetFile);
//    minijar.setManifestFile(MiniJar.createManifest(".",metadata.getClassname()));
//    minijar.addExclude ("++Thumbs.db");
//    minijar.addExclude ("++.DS_Store");
//    addResources(_ejs,minijar);
//    
//    // Add resources needed by the view elements when packaging. For example, CamImage adds  "com/charliemouse/++.gif"
//    for (String resource : metadata.getResourcePatterns()) minijar.addDesired(resource);
//
//    // Add source paths according to the execution path
//    StringTokenizer tkn = new StringTokenizer (metadata.getExecpath(),File.pathSeparator);
//    while (tkn.hasMoreTokens()) minijar.addSourcePath(tkn.nextToken());
//
//    // Now add the main class and the Applet class as well
//    String classname = metadata.getClassname().replace('.', '/');
//    minijar.addDesired(classname+".class");
//    minijar.addDesired(classname+"Applet.class");
//
//    // Get matches
//    Set<PathAndFile> matches = minijar.getMatches();
//    
//    for (PathAndFile paf : matches) { // add classes which seem to use reflection
//      String path = paf.getPath(); 
////      if (path.startsWith("com/sun/j3d/loaders/vrml97/")) System.err.println (path);
//      if (path.endsWith("/ElementObjectVRML.class")) { // Add loader libraries
//        //minijar.addForced("com/sun/j3d/loaders/vrml97/++");
//        minijar.addForced("loaders/vrml/++");
//      }
//      else if (path.endsWith("/ElementObject3DS.class")) { // Add loader libraries
//        minijar.addForced("loaders/3ds3/++");
//      }
//    }
//    
//    matches = minijar.getMatches(); // a second round is required for loaders which use reflection
////    for (PathAndFile paf : matches) {  
////      String path = paf.getPath(); 
////      if (path.startsWith("com/sun/j3d/loaders/vrml97/")) System.err.println (path);
////    }
//
////    for (PathAndFile paf : matches) System.out.println ("Found file: "+paf.getPath()); 
//
//    // Add the XML file
////    File xmlFile = new File (generationDir, plainName+".ejs");
////    if (!xmlFile.exists()) xmlFile = new File (generationDir, plainName+".xml"); // Try older format
////    if (xmlFile.exists()) matches.add(new PathAndFile (relativePath+xmlFile.getName(),xmlFile));
//    for (String filename : metadata.getFilesCreated())
//      if (filename.endsWith(".xml") || filename.endsWith(".ejs")) {
//        File xmlFile = new File (generationDir, filename);
//        if (xmlFile.exists()) matches.add(new PathAndFile(relativePath+filename,xmlFile));
//      }
//
//    // Add the _Intro HTML files
//    String prefix = OsejsCommon.getHTMLFilenamePrefix(_ejs.getCurrentXMLFile());
//    for (String filename : metadata.getFilesCreated())
//      if (filename.startsWith(prefix) && filename.endsWith(".html"))
//        matches.add(new PathAndFile(relativePath+filename, new File (generationDir, filename)));
//    
//    // Add auxiliary files
//    Set<String> missingAuxFiles = new HashSet<String>();
//    for (String auxPath : metadata.getAuxiliaryFiles()) {
//      File auxFile = new File(_ejs.getSourceDirectory(),auxPath);
//      if (!auxFile.exists()) missingAuxFiles.add(auxPath);
//      else if (auxFile.isDirectory()) { // It is a complete directory
//         for (File file : JarTool.getContents(auxFile)) {
//           matches.add(new PathAndFile(FileUtils.getRelativePath(file,_ejs.getSourceDirectory(),false),file));
//         }
//      }
//      else matches.add(new PathAndFile(auxPath,auxFile));
//    }
//    
//    // Add user jars files. These won't get as jars inside the target jar, but their contents will
//    // We need them as proper jar files in order to extract them later on
//    for (String jarPath : metadata.getJarFiles()) {
//      File jarFile = new File(_ejs.getSourceDirectory(),jarPath);
//      if (jarFile.exists()) matches.add(new PathAndFile(jarPath,jarFile));
//      else missingAuxFiles.add(jarPath);
//    }
//    
//    // Overwrite files in the library directory with user defined files (if any)
//    String configDirPath = FileUtils.getPath(_ejs.getConfigDirectory());
//    for (File file : JarTool.getContents(new File(_ejs.getConfigDirectory(),OsejsCommon.EJS_LIBRARY_DIR_PATH)))
//      matches.add(new PathAndFile(FileUtils.getRelativePath(file, configDirPath, false),file));
//
//    // Add all the files in the library directory 
//    File binConfigDir = new File (_ejs.getBinDirectory(),OsejsCommon.CONFIG_DIR_PATH);
//    String binConfigDirPath = FileUtils.getPath(binConfigDir);
//    for (File file : JarTool.getContents(new File(binConfigDir,OsejsCommon.EJS_LIBRARY_DIR_PATH)))
//      matches.add(new PathAndFile(FileUtils.getRelativePath(file, binConfigDirPath, false),file));
//
//    
//    if (!_ejs.getSimInfoEditor().addTranslatorTool()) {
//      Set<PathAndFile> trimmedSet = new HashSet<PathAndFile>();
//      String ownLocale = Locale.getDefault().getLanguage();
////      System.out.println ("Language is "+ownLocale);
//      for (PathAndFile paf : matches) {
//        String path = paf.getPath(); 
//        if (!path.endsWith(".properties")) trimmedSet.add(paf);
//        else {
//          int resIndex = path.indexOf("_res"); // ejs_res and others
//          int _index;
//          if (resIndex<0) _index = path.indexOf('_');
//          else _index = path.indexOf('_', resIndex+1);
//          if (_index<0) trimmedSet.add(paf);
//          else {
//            String pathLocale = path.substring(_index+1,_index+3);
//            if (pathLocale.equals(ownLocale)) {
////              System.out.println ("Adding "+path);
//              trimmedSet.add(paf);
//            }
////            else System.out.println ("Excluding "+path);
//          }
//        }
//      }
//      matches = trimmedSet;
//    }
//    
//    // Create the jar file
//    Set<String> missing = minijar.compress(matches);
//    
//    // Print missing files
//    if (_ejs.isVerbose()) {
//      for (Iterator<String> it=missing.iterator(); it.hasNext(); ) System.out.println ("Missing file: "+it.next()); 
//    }
// 
//    _ejs.getOutputArea().message("Package.JarFileCreated",_targetFile.getName());
////    JOptionPane.showMessageDialog(_ejs.getMainPanel(), res.getString("Package.JarFileCreated"+" "+_targetFile.getName()), 
////        res.getString("Package.PackagingJarFile"), JOptionPane.INFORMATION_MESSAGE);
//    OsejsCommon.warnAboutFiles(_ejs.getMainPanel(),missingAuxFiles,"SimInfoEditor.RequiredFileNotFound");
//  }

  /**
   * Compresses a list of simulations in a single JAR file, without Launcher capabilities
   * @param _ejs
   * @param _listOfMetadataFiles 
   * @param _targetFile
   */
  static private void packageSeveralSimulations(Osejs _ejs, java.util.List<PathAndFile> _listOfMetadataFiles, File _targetFile, boolean _abortOnError) {
    _ejs.getExportDirectory().mkdirs(); // In case it doesn't exist
    //System.out.println ("packaging "+_targetFile.getAbsolutePath());
    _ejs.getOutputArea().message("Package.PackagingJarFile",_targetFile.getName());

    // Create an instance of MiniJar and prepare it
    MiniJar minijar= new MiniJar();
    minijar.setOutputFile(_targetFile);
//    minijar.setManifestFile(MiniJar.createManifest(".","org.colos.ejs.library._EjsConstants"));
    minijar.addExclude ("++Thumbs.db");
    minijar.addExclude ("++.DS_Store");
    addResources(_ejs,minijar);
    boolean manifestNotSet = true;
    if (_listOfMetadataFiles.size()>1) {
      minijar.addDesired("org/colos/ejs/library/utils/SimulationChooser.class");
//      minijar.addDesired("org/opensourcephysics/resources/ejs/++");
      minijar.setManifestFile(MiniJar.createManifest(".","org.colos.ejs.library.utils.SimulationChooser"));
      manifestNotSet = false;
    }
    
    Set<PathAndFile> extraFiles = new HashSet<PathAndFile>();
    Set<String> missingAuxFiles = new HashSet<String>();
    
    StringBuffer pckgBuffer = new StringBuffer();
    pckgBuffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    pckgBuffer.append("<package name=\""+_targetFile.getName()+"\">\n");

    for (PathAndFile paf : _listOfMetadataFiles) {
      File metadataFile = paf.getFile();
//      String plainName = FileUtils.getPlainName(metadataFile);
      File generationDir = metadataFile.getParentFile();
      String relativePath = FileUtils.getRelativePath(generationDir, _ejs.getOutputDirectory(), false); 

      // Read the meta data
      Metadata metadata = Metadata.readFile(metadataFile, relativePath);
      if (metadata==null) {
        if (_abortOnError) {
          String[] message=new String[]{res.getString("Package.JarFileNotCreated"),res.getString("Package.IncorrectMetadata")};
          JOptionPane.showMessageDialog(_ejs.getMainPanel(),message,res.getString("Package.Error"),JOptionPane.WARNING_MESSAGE);
          return;
        }
        String[] message=new String[]{res.getString("Package.IgnoringSimulation"),res.getString("Package.IncorrectMetadata")};
        JOptionPane.showMessageDialog(_ejs.getMainPanel(),message,res.getString("Package.Error"),JOptionPane.WARNING_MESSAGE);
        continue;
      }

      if (manifestNotSet) {
        minijar.setManifestFile(MiniJar.createManifest(".",metadata.getClassname()));
        manifestNotSet = false;
      }

      pckgBuffer.append("  <simulation name=\""+FileUtils.getPlainName(metadataFile)+"\">\n");
      pckgBuffer.append("    <title>"+metadata.getTitle()+"</title>\n");
      pckgBuffer.append("    <class>"+metadata.getClassname()+"</class>\n");
      pckgBuffer.append("    <applet>"+metadata.getClassname()+"Applet</applet>\n");
      pckgBuffer.append("  </simulation>\n");

      // Add resources needed by the view elements when packaging. For example, CamImage adds  "com/charliemouse/++.gif"
      for (String resource : metadata.getResourcePatterns()) minijar.addDesired(resource);

      // Add source paths according to the execution path
      StringTokenizer tkn = new StringTokenizer (metadata.getExecpath(),File.pathSeparator);
      while (tkn.hasMoreTokens()) minijar.addSourcePath(tkn.nextToken());

      // Now add the main class and the Applet class as well
      String classname = metadata.getClassname().replace('.', '/');
      minijar.addDesired(classname+".class");
      minijar.addDesired(classname+"Applet.class");
      if(_ejs.getSimInfoEditor().addAppletColSupport()) minijar.addDesired(classname+"AppletStudent.class");

      if (_ejs.getSimInfoEditor().addTranslatorTool()) minijar.addDesired(classname+"+.properties");
      
      
      // Add the XML file
//      File xmlFile = new File (generationDir, plainName+".ejs");
//      if (!xmlFile.exists()) xmlFile = new File (generationDir, plainName+".xml"); // Try older format
//      if (xmlFile.exists()) extraFiles.add(new PathAndFile (relativePath+xmlFile.getName(),xmlFile));
      for (String filename : metadata.getFilesCreated())
        if (filename.endsWith(".xml") || filename.endsWith(".ejs")) {
          File xmlFile = new File (generationDir, filename);
          if (xmlFile.exists()) extraFiles.add(new PathAndFile(relativePath+filename,xmlFile));
        }

      // Add the _Intro HTML files
      String prefix = OsejsCommon.getHTMLFilenamePrefix(metadataFile);
      for (String filename : metadata.getFilesCreated())
        if (filename.startsWith(prefix) && filename.endsWith(".html"))
          extraFiles.add(new PathAndFile(relativePath+filename, new File (generationDir, filename)));
      
      // Add auxiliary files
      for (String auxPath : metadata.getAuxiliaryFiles()) {
        File auxFile = new File(_ejs.getSourceDirectory(),auxPath);
        if (!auxFile.exists()) missingAuxFiles.add(auxPath);
        else if (auxFile.isDirectory()) { // It is a complete directory
           for (File file : JarTool.getContents(auxFile)) {
             extraFiles.add(new PathAndFile(FileUtils.getRelativePath(file,_ejs.getSourceDirectory(),false),file));
           }
        }
        else extraFiles.add(new PathAndFile(auxPath,auxFile));
      }
      
      // Add user jars files. These won't get as jars inside the target jar, but their contents will
      // We need them as proper jar files in order to extract them later on
      for (String jarPath : metadata.getJarFiles()) {
        File jarFile = new File(_ejs.getSourceDirectory(),jarPath);
        if (jarFile.exists()) extraFiles.add(new PathAndFile(jarPath,jarFile));
        else missingAuxFiles.add(jarPath);
      }

      // Add files the user marked as non-discoverable files in JAR files which need to go into the jar. 
      // A good example are GIF or DLLs in th emodel element JARS
      for (String filename : metadata.getPackageList()) {
//        System.out.println ("Adding package list file : "+filename);
        minijar.addDesired(filename);
      }

    }
    
    // Get matches
    Set<PathAndFile> matches = minijar.getMatches();
    
    for (PathAndFile paf : matches) { // add classes which seem to use reflection
      String path = paf.getPath(); 
//      if (path.startsWith("com/sun/j3d/loaders/vrml97/")) System.err.println (path);
      if (path.endsWith("/ElementObjectVRML.class")) { // Add loader libraries
//        minijar.addForced("com/sun/j3d/loaders/vrml97/++");
        minijar.addForced("loaders/vrml/++");
      }
      else if (path.endsWith("/ElementObject3DS.class")) { // Add loader libraries
        minijar.addForced("loaders/3ds3/++");
      }
    }
    matches = minijar.getMatches(); // a second round is required for loaders which use reflection
   
    matches.addAll(extraFiles);

    pckgBuffer.append("</package>\n");
    try {  
      File tmpFile = File.createTempFile("ejs_", ".xml");
      FileUtils.saveToFile(tmpFile, null, pckgBuffer.toString());
      matches.add(new PathAndFile("EJSSimulationList.xml",tmpFile));
    }
    catch (Exception exc) {
      exc.printStackTrace();
      _ejs.getOutputArea().println(res.getString("Osejs.File.CantCreateFile")+" EJSSimulationList.xml");
    }

    // Overwrite files in the library directory with user defined files (if any)
    String configDirPath = FileUtils.getPath(_ejs.getConfigDirectory());
    for (File file : JarTool.getContents(new File(_ejs.getConfigDirectory(),OsejsCommon.EJS_LIBRARY_DIR_PATH)))
      matches.add(new PathAndFile(FileUtils.getRelativePath(file, configDirPath, false),file));

    // Add all the files in the library directory 
    File binConfigDir = new File (_ejs.getBinDirectory(),OsejsCommon.CONFIG_DIR_PATH);
    String binConfigDirPath = FileUtils.getPath(binConfigDir);
    for (File file : JarTool.getContents(new File(binConfigDir,OsejsCommon.EJS_LIBRARY_DIR_PATH)))
      matches.add(new PathAndFile(FileUtils.getRelativePath(file, binConfigDirPath, false),file));

    
    if (!_ejs.getSimInfoEditor().addTranslatorTool()) { // Add translation of core properties ONLY for the current locale
      Set<PathAndFile> trimmedSet = new HashSet<PathAndFile>();
      String ownLocale = java.util.Locale.getDefault().getLanguage();
//      System.out.println ("Language is "+ownLocale);
      for (PathAndFile paf : matches) {
        String path = paf.getPath(); 
        if (!path.endsWith(".properties")) trimmedSet.add(paf);
        else {
          int resIndex = path.indexOf("_res"); // ejs_res and others
          int _index;
          if (resIndex<0) _index = path.indexOf('_');
          else _index = path.indexOf('_', resIndex+1);
          if (_index<0) trimmedSet.add(paf);
          else {
            String pathLocale = path.substring(_index+1,_index+3);
            if (pathLocale.equals(ownLocale)) {
//              System.out.println ("Adding "+path);
              trimmedSet.add(paf);
            }
//            else System.out.println ("Excluding "+path);
          }
        }
      }
      matches = trimmedSet;
    }
    
    // Create the jar file
    Set<String> missing = minijar.compress(matches);
    
    // Print missing files
    if (_ejs.isVerbose()) {
      for (Iterator<String> it=missing.iterator(); it.hasNext(); ) System.out.println ("Missing file: "+it.next()); 
    }
 
    _ejs.getOutputArea().message("Package.JarFileCreated",_targetFile.getName());
//    JOptionPane.showMessageDialog(_ejs.getMainPanel(), res.getString("Package.JarFileCreated"+" "+_targetFile.getName()), 
//        res.getString("Package.PackagingJarFile"), JOptionPane.INFORMATION_MESSAGE);
    OsejsCommon.warnAboutFiles(_ejs.getMainPanel(),missingAuxFiles,"SimInfoEditor.RequiredFileNotFound");
  }
  
  /**
   * This compresses the simulation XML file and its auxiliary files
   * @param _ejs Osejs
   * @param _filename String
   * @return String
   */
  static public void zipCurrentSimulation(Osejs _ejs, File targetFile) {
    _ejs.getExportDirectory().mkdirs(); // In case it doesn't exist

    File xmlFile = _ejs.getCurrentXMLFile();
    if (!xmlFile.exists()) {
      JOptionPane.showMessageDialog(_ejs.getMainPanel(), res.getString("Package.NoSimulations"),
        res.getString("Osejs.File.Error"), JOptionPane.INFORMATION_MESSAGE);
      return;
    }

    _ejs.getOutputArea().message("Package.PackagingJarFile",targetFile.getName());
    
    Set<PathAndFile> list = new HashSet<PathAndFile>();
    list.add(new PathAndFile(xmlFile.getName(),xmlFile));
    
    Set<String> missingAuxFiles = new HashSet<String>();
    Set<String> absoluteAuxFiles = new HashSet<String>();
    for (PathAndFile paf : _ejs.getSimInfoEditor().getAuxiliaryPathAndFiles()) {
      if (!paf.getFile().exists()) missingAuxFiles.add(paf.getPath());
      else if (paf.getFile().isDirectory()) { // It is a complete directory
        String prefix = paf.getPath();
        if (prefix.startsWith("./")) prefix = prefix.substring(2);
        else absoluteAuxFiles.add(prefix);
        for (File file : JarTool.getContents(paf.getFile())) {
          list.add(new PathAndFile(prefix+FileUtils.getRelativePath(file,paf.getFile(),false),file));
        }
      }
      else {
        if (paf.getPath().startsWith("./")) list.add(new PathAndFile(paf.getPath().substring(2),paf.getFile()));
        else {
          absoluteAuxFiles.add(paf.getPath());
          list.add(paf);
        }
      }
    }

    if (org.opensourcephysics.tools.minijar.MiniJar.compress(list, targetFile, null)) {
      _ejs.getOutputArea().println(res.getString("Package.JarFileCreated")+" "+targetFile.getName());
    }
    else _ejs.getOutputArea().println(res.getString("Package.JarFileNotCreated"));
    OsejsCommon.warnAboutFiles(_ejs.getMainPanel(),missingAuxFiles,"SimInfoEditor.RequiredFileNotFound");
    OsejsCommon.warnAboutFiles(_ejs.getMainPanel(),absoluteAuxFiles,"Generate.AbsolutePathsFound");
  }

  /**
   * This compresses several simulation XML files and their auxiliary files
   * @param _ejs Osejs
   */
  static public void zipSeveralSimulations(Osejs _ejs) {
    // Select simulation files or directories to ZIP 
    JFileChooser chooser=OSPRuntime.createChooser(res.getString("View.FileDescription.xmlfile"), new String[]{"xml","ejs"},_ejs.getSourceDirectory().getParentFile());
    org.colos.ejs.library.utils.FileUtils.updateComponentTreeUI(chooser);
    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    chooser.setMultiSelectionEnabled(true);
    chooser.setCurrentDirectory(_ejs.getFileDialog(null).getCurrentDirectory());
    if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;
    final File[] dirs = chooser.getSelectedFiles();
    if (dirs==null || dirs.length<=0) {
      System.out.println (res.getString("ProcessCanceled"));
      return;
    }
    
    File baseDir = chooser.getCurrentDirectory();
    java.util.List<PathAndFile> list = new ArrayList<PathAndFile>();
    for (int i=0,n=dirs.length; i<n; i++) {
      File file = dirs[i];
      if (file.isDirectory()) {
        for (File subFile : JarTool.getContents(file)) 
          if (OsejsCommon.isEJSfile(subFile)) list.add(new PathAndFile(FileUtils.getRelativePath (subFile, baseDir, false),subFile));
      }
      else {
        if (OsejsCommon.isEJSfile(file)) list.add(new PathAndFile(FileUtils.getRelativePath (file, baseDir, false),file));
      }
    }

    java.util.List<?> confirmedList = EjsTool.ejsConfirmList(null,res.getDimension("Package.ConfirmList.Size"),
        res.getString("Package.CompressList"),res.getString("Package.CompressTitle"),list,null);
    if (confirmedList==null || confirmedList.isEmpty()) return;

    // Choose target
    _ejs.getExportDirectory().mkdirs(); // In case it doesn't exist
    File targetFile = new File(_ejs.getExportDirectory(),"ejs_simulations.zip");
    chooser = OSPRuntime.createChooser("ZIP",new String[]{"zip"},_ejs.getSourceDirectory().getParentFile());
    org.colos.ejs.library.utils.FileUtils.updateComponentTreeUI(chooser);
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setCurrentDirectory(_ejs.getExportDirectory());
    chooser.setSelectedFile(targetFile);
    String targetName = OSPRuntime.chooseFilename(chooser,_ejs.getMainPanel(), true);
    if (targetName==null) return;
    
    targetFile = new File(targetName);
    boolean warnBeforeOverwritting = true;
    if (! targetName.toLowerCase().endsWith(".zip")) targetName = targetName + ".zip";
    else warnBeforeOverwritting = false; // the chooser already checked if the target file exists
    if (warnBeforeOverwritting && targetFile.exists()) {
      int selected = JOptionPane.showConfirmDialog(_ejs.getMainPanel(),DisplayRes.getString("DrawingFrame.ReplaceExisting_message") + " " +
          targetFile.getName() +DisplayRes.getString("DrawingFrame.QuestionMark"),
          DisplayRes.getString("DrawingFrame.ReplaceFile_option_title"), JOptionPane.YES_NO_CANCEL_OPTION);
      if (selected != JOptionPane.YES_OPTION) return;
    }
    
    // Now, do it
    _ejs.getOutputArea().message("Package.PackagingJarFile",targetFile.getName());
    
    Set<String> missingAuxFiles = new HashSet<String>();
    Set<String> absoluteAuxFiles = new HashSet<String>();
    Set<PathAndFile> packingList = new HashSet<PathAndFile>();
    for (Object xmlObject : confirmedList) {
      PathAndFile xmlPaf = (PathAndFile) xmlObject;
      packingList.add(xmlPaf);
      String xmlName = xmlPaf.getFile().getName();
      String xmlPrefix = xmlPaf.getPath().substring(0,xmlPaf.getPath().indexOf(xmlName));
      for (PathAndFile paf : OsejsCommon.getAuxiliaryFiles(xmlPaf.getFile(),_ejs.getSourceDirectory(),_ejs.getMainPanel())) {
        if (!paf.getFile().exists()) missingAuxFiles.add(paf.getPath());
        else if (paf.getFile().isDirectory()) { // It is a complete directory
          String prefix = paf.getPath();
          if (prefix.startsWith("./")) prefix = xmlPrefix + prefix.substring(2);
          else absoluteAuxFiles.add(prefix);
          for (File file : JarTool.getContents(paf.getFile())) {
            packingList.add(new PathAndFile(prefix+FileUtils.getRelativePath(file,paf.getFile(),false),file));
          }
        }
        else {
          if (paf.getPath().startsWith("./")) packingList.add(new PathAndFile(xmlPrefix+paf.getPath().substring(2),paf.getFile()));
          else {
            absoluteAuxFiles.add(paf.getPath());
            packingList.add(paf);
          }
        }
      }
    }
    
//    for (PathAndFile paf : packingList) System.out.println ("Will pack: "+paf.getPath());
    
    if (org.opensourcephysics.tools.minijar.MiniJar.compress(packingList, targetFile, null)) {
      _ejs.getOutputArea().println(res.getString("Package.JarFileCreated")+" "+targetFile.getName());
    }
    else _ejs.getOutputArea().println(res.getString("Package.JarFileNotCreated"));
    OsejsCommon.warnAboutFiles(_ejs.getMainPanel(),missingAuxFiles,"SimInfoEditor.RequiredFileNotFound");
    OsejsCommon.warnAboutFiles(_ejs.getMainPanel(),absoluteAuxFiles,"Generate.AbsolutePathsFound");
  }
  
  
  static public void cleanSimulations(Osejs _ejs) {
    java.util.List<PathAndFile> list = OsejsCommon.getSimulationsMetadataFiles(_ejs.getMainPanel(),_ejs.getOutputDirectory(),
        res.getDimension("Package.ConfirmList.Size"), res.getString("Package.CleanSimulationsMessage"),
        res.getString("Package.CleanSimulations"),true,false);
    if (list==null || list.size()<=0) return;
    boolean result = true;
    for (PathAndFile paf : list) {
      Metadata metadata = Metadata.readFile(paf.getFile(), null);
      // Clean all files created during the compilation process
      for (String filename : metadata.getFilesCreated()) {
        File file = new File (paf.getFile().getParentFile(),filename);
        if (!file.delete()) {
          JOptionPane.showMessageDialog(_ejs.getMainPanel(), 
            res.getString("Package.CouldNotDeleteDir")+" "+FileUtils.getPath(file),
            res.getString("Package.Error"), JOptionPane.INFORMATION_MESSAGE);
          result = false;
        }
      }
    }
    if (result) {
      if (_ejs.getOutputDirectory().exists())
        org.colos.ejs.library.Simulation.removeEmptyDirs(_ejs.getOutputDirectory(),false); // false = Do not remove the "output" directory itself.
      _ejs.getOutputArea().println(res.getString("Package.SimulationsDeleted"));
    }
    else _ejs.getOutputArea().println(res.getString("Package.SimulationsNotDeleted"));
  }

  static public void packageSeveralSimulations(Osejs _ejs) {
    java.util.List<PathAndFile> list = OsejsCommon.getSimulationsMetadataFiles (_ejs.getMainPanel(),_ejs.getOutputDirectory(),
        res.getDimension("Package.ConfirmList.Size"), res.getString("Package.PackageAllSimulationsMessage"),
        res.getString("Package.PackageAllSimulations"),true,false);
    if (list==null || list.size()==0) return;
    
    // Choose target
    _ejs.getExportDirectory().mkdirs(); // In case it doesn't exist
    File targetFile = new File(_ejs.getExportDirectory(),"ejs_package.jar");
    JFileChooser chooser = OSPRuntime.createChooser("JAR",new String[]{"jar"},_ejs.getSourceDirectory().getParentFile());
    org.colos.ejs.library.utils.FileUtils.updateComponentTreeUI(chooser);
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setCurrentDirectory(_ejs.getExportDirectory());
    chooser.setSelectedFile(targetFile);
    String targetName = OSPRuntime.chooseFilename(chooser,_ejs.getMainPanel(), true);
    if (targetName==null) return;
    
    targetFile = new File(targetName);
    boolean warnBeforeOverwritting = true;
    if (! targetName.toLowerCase().endsWith(".jar")) targetName = targetName + ".jar";
    else warnBeforeOverwritting = false; // the chooser already checked if the target file exists
    if (warnBeforeOverwritting && targetFile.exists()) {
      int selected = JOptionPane.showConfirmDialog(_ejs.getMainPanel(),DisplayRes.getString("DrawingFrame.ReplaceExisting_message") + " " +
          targetFile.getName() +DisplayRes.getString("DrawingFrame.QuestionMark"),
          DisplayRes.getString("DrawingFrame.ReplaceFile_option_title"), JOptionPane.YES_NO_CANCEL_OPTION);
      if (selected != JOptionPane.YES_OPTION) return;
    }
    // do it
    packageSeveralSimulations(_ejs,list,targetFile,false);
  }

  
  
  static public void packageAllSimulations(Osejs _ejs) {
    _ejs.getExportDirectory().mkdirs(); // In case it doesn't exist
    File target = new File ( _ejs.getExportDirectory(),"ejs_launcher.jar");
    java.util.List<PathAndFile> list = OsejsCommon.getSimulationsMetadataFiles (_ejs.getMainPanel(),_ejs.getOutputDirectory(),
        res.getDimension("Package.ConfirmList.Size"), res.getString("Package.PackageAllSimulationsMessage"),
        res.getString("Package.PackageAllSimulations"),true,false);
    if (list!=null && list.size()>0) {
      PackagerBuilder.create(list, _ejs.getBinDirectory().getParentFile(), _ejs.getSourceDirectory().getParentFile(), 
          target, _ejs.getProcessDialog(), _ejs.getOutputArea().textArea(), _ejs.getMainPanel(),_ejs.getMainFrame());
    }
  }

//  static public void editLauncherPackage(Osejs _ejs) {
//    _ejs.getExportDirectory().mkdirs(); // In case it doesn't exist
//
//    JFileChooser chooser=OSPRuntime.createChooser("JAR",new String[]{"jar"},_ejs.getSourceDirectory().getParentFile());
//    org.colos.ejs.library.utils.FileUtils.updateComponentTreeUI(chooser);
//    chooser.setCurrentDirectory(_ejs.getExportDirectory());
//    String sourceName = OSPRuntime.chooseFilename(chooser,_ejs.getMainPanel(),false); // false = to read
//    
//    if (sourceName==null) return;
//    if (!sourceName.toLowerCase().endsWith(".jar")) sourceName = sourceName + ".jar";
//    File source = new File(sourceName);
//    if (!PackagerBuilder.canBeRebuilt(source)){
//      JOptionPane.showMessageDialog(_ejs.getMainPanel(),res.getString("Package.FileNotExistingError") + ": " +
//          source.getName(),DisplayRes.getString("Package.Error"),JOptionPane.ERROR_MESSAGE);
//      return;
//    }
//
//    // Add already compiled simulations to the list
//    java.util.List<File> existingList = OsejsCommon.getSimulationsMetadataFiles (_ejs.getMainPanel(),_ejs.getOutputDirectory(),
//        res.getDimension("Package.ConfirmList.Size"), res.getString("EjsConsole.RebuildPackage"),
//        res.getString("Package.PackageExtraSimulationsMessage"),false,false);
//    if (existingList==null) {
//      _ejs.getOutputArea().println (res.getString("ProcessCanceled"));
//      return;
//    }
//
//    //_ejs.getOutputArea().clear();
//    _ejs.getOutputArea().println(res.getString("EjsConsole.UncompressingJAR")+" "+source.getName());
//    File rebuildTmpDir = PackagerBuilder.uncompressToTemp(source);
//    if (rebuildTmpDir==null){
//      String[] message=new String[]{res.getString("Package.JarFileNotCreated"),res.getString("Package.NotTempDirError")};
//      JOptionPane.showMessageDialog(_ejs.getMainPanel(),message,res.getString("Package.Error"),JOptionPane.WARNING_MESSAGE);
//      return;
//    }
//    PackagerBuilder.rebuild (_ejs.getProcessDialog(),new ArrayList<File>(existingList), existingList, 
//        _ejs.getBinDirectory(), _ejs.getOutputDirectory(), _ejs.getSourceDirectory(), _ejs.getConfigDirectory(),
//        rebuildTmpDir, source, _ejs.getOutputArea().textArea(),_ejs.getMainPanel(),_ejs.getMainFrame());
//  }

  static public void createGroupHTML(Osejs _ejs) {
    _ejs.getExportDirectory().mkdirs(); // In case it doesn't exist
    java.util.List<PathAndFile> list = OsejsCommon.getSimulationsMetadataFiles (_ejs.getMainPanel(),_ejs.getOutputDirectory(),
        res.getDimension("Package.ConfirmList.Size"), res.getString("Package.CreateGroupHTMLMessage"),
        res.getString("Package.CreateGroupHTML"),true,true);
    if (list==null || list.size()<=0) return;

    JPanel fullJarPanel = new JPanel(new GridLayout(0,1));
    JLabel fullJarLabel = new JLabel(res.getString("Package.HTMLJARCreate"));
    JRadioButton fullJarButton = new JRadioButton(res.getString("Package.IndependentJAR"),true);
    JRadioButton commonJarButton = new JRadioButton(res.getString("Package.CommonJAR"),false);
    fullJarPanel.add(fullJarLabel);
    fullJarPanel.add(fullJarButton);
    fullJarPanel.add(commonJarButton);
    ButtonGroup fullJarGroup = new ButtonGroup();
    fullJarGroup.add(fullJarButton);
    fullJarGroup.add(commonJarButton);
    
    JPanel fullJarTopPanel = new JPanel (new BorderLayout());
    fullJarTopPanel.add(fullJarPanel,BorderLayout.NORTH);
    
    // Choose the target HTML file
    File targetFile = new File (_ejs.getExportDirectory(),"ejs_group.html");
    JFileChooser chooser=OSPRuntime.createChooser("HTML",new String[]{"html", "htm"},_ejs.getSourceDirectory().getParentFile());
    org.colos.ejs.library.utils.FileUtils.updateComponentTreeUI(chooser);
    chooser.setCurrentDirectory(targetFile.getParentFile());
    chooser.setSelectedFile(targetFile);
    chooser.setAccessory(fullJarTopPanel);
    
    String targetFilename = OSPRuntime.chooseFilename(chooser,_ejs.getMainPanel(),true);
    if (targetFilename==null) return;

    // Checking for existence of the target HTML file and associated directory
    boolean warnBeforeOverwritting = true;
    if (! (targetFilename.toLowerCase().endsWith(".html") || targetFilename.toLowerCase().endsWith(".htm")) ) 
      targetFilename = targetFilename + ".html";
    else warnBeforeOverwritting = false; // the chooser already checked if the target file exists
    targetFile = new File(targetFilename);
    File groupDirectory = new File (targetFile.getParent(),FileUtils.getPlainName(targetFile)+".files");
    if (warnBeforeOverwritting && targetFile.exists()) {
      int selected = JOptionPane.showConfirmDialog(_ejs.getMainPanel(),DisplayRes.getString("DrawingFrame.ReplaceExisting_message") + " " +
        targetFile.getName() + DisplayRes.getString("DrawingFrame.QuestionMark"),
        DisplayRes.getString("DrawingFrame.ReplaceFile_option_title"),JOptionPane.YES_NO_CANCEL_OPTION);
      if (selected != JOptionPane.YES_OPTION) {
        _ejs.getOutputArea().println(res.getString("Package.JarFileNotCreated"));
        return;
      }
    }

    // Ready to do it
    if (groupDirectory.exists()) JarTool.remove(groupDirectory);
    groupDirectory.mkdirs();

    try { 
      FileUtils.saveToFile(targetFile, null, generateGroupHtml(_ejs, list, groupDirectory, FileUtils.getPlainName(targetFile),fullJarButton.isSelected())); 
    }
    catch (Exception exc) {
      JOptionPane.showMessageDialog(_ejs.getMainPanel(), res.getString("Osejs.File.CantCreateFile"),
        res.getString("Package.CantCreateError"), JOptionPane.INFORMATION_MESSAGE);
      exc.printStackTrace();
      return;
    }

    // Copy all files in the library directory 
    File binConfigDir = new File (_ejs.getBinDirectory(),OsejsCommon.CONFIG_DIR_PATH);
    String binConfigDirPath = FileUtils.getPath(binConfigDir);
    for (File file : JarTool.getContents(new File(binConfigDir,OsejsCommon.EJS_LIBRARY_DIR_PATH))) {
      File destFile = new File(groupDirectory,FileUtils.getRelativePath(FileUtils.getPath(file), binConfigDirPath, false));
      if (!JarTool.copy(file,destFile)) {
        JOptionPane.showMessageDialog(_ejs.getMainPanel(), res.getString("Osejs.File.CantCreateFile")+" "+destFile.getAbsolutePath(),
            res.getString("Package.CantCreateError"), JOptionPane.INFORMATION_MESSAGE);
        _ejs.getOutputArea().message("Osejs.File.CantCreateFile",destFile.getAbsolutePath());
        return;
      }
    }

    // But overwrite them with user defined library files
    String configDirPath = FileUtils.getPath(_ejs.getConfigDirectory());
    for (File file : JarTool.getContents(new File(_ejs.getConfigDirectory(),OsejsCommon.EJS_LIBRARY_DIR_PATH))) {
      File destFile = new File(groupDirectory,FileUtils.getRelativePath(FileUtils.getPath(file), configDirPath, false));
      if (!JarTool.copy(file,destFile)) {
        JOptionPane.showMessageDialog(_ejs.getMainPanel(), res.getString("Osejs.File.CantCreateFile")+" "+destFile.getAbsolutePath(),
            res.getString("Package.CantCreateError"), JOptionPane.INFORMATION_MESSAGE);
        _ejs.getOutputArea().message("Osejs.File.CantCreateFile",destFile.getAbsolutePath());
        return;
      }
    }
    
    // Overwrite files in the library directory with user defined files (if any)
    // CJB for collaborative
//    if(_ejs.getSimInfoEditor().addAppletColSupport()){
//    	for (File file : JarTool.getContents(new File(binConfigDir,"Collaborative"))) {
//    		String destName = FileUtils.getRelativePath(file, binConfigDirPath, false);
//    		JarTool.copy(file, new File (_ejs.getOutputDirectory(),destName));
//    	}
//    	for (File file : JarTool.getContents(new File(_ejs.getConfigDirectory(),"Collaborative"))) {
//    		String destName = FileUtils.getRelativePath(file, configDirPath, false);
//    		JarTool.copy(file, new File (_ejs.getOutputDirectory(),destName));
//    	}
//    }
    //CJB for collaborative
    
    // Done
    _ejs.getOutputArea().message("Package.GroupHTMLOk",FileUtils.getRelativePath(targetFile,_ejs.getExportDirectory(),false));
  }

  // ----------------------------
  // Utilities
  // ----------------------------


  /**
   * Gets the package for the java file
   * @param _className String The valid plain name, i.e. Whatever_sim
   * @param _fullName String The relative full path, i.e. _users/murcia/fem/Whatever sim.xml
   * @return String
   */
  static private String getPackageName (File _xmlFile, String _className, String _relativePath) {
    //String lastPackageName = "_"+OsejsCommon.firstToLower(_className)+"_";
    String lastPackageName = _className+"_pkg";
    // Avoid conflicts with existing directories (or files)
    // Commented out because this prevented the Translated properties files to be added correctly
//    int counter = 0;
//    while (new File (_xmlFile.getParentFile(),lastPackageName).exists() && counter<50) lastPackageName = _className+"_pkg_" + (++counter);
    
    if (_relativePath.length()<=0) return lastPackageName;
    String packageName = new String();
    StringTokenizer tkn = new StringTokenizer(_relativePath.replace('.','_'),"/");
    while (tkn.hasMoreTokens()) packageName += OsejsCommon.getValidIdentifier(tkn.nextToken()) + ".";
    return packageName + lastPackageName;
  }

  /**
   * Get the class path for compilation
   * @param _list List The list of all user required jar files
   * @param _generationDir File The base directory for generated files
   * @return String
   */
  static private String getClasspath (File _binDir, String _binDirPath, String _srcDirPath, Collection<PathAndFile> _list) {
    StringBuffer textList = new StringBuffer();
    textList.append(_binDirPath+"osp.jar"+ File.pathSeparatorChar);
    textList.append(_binDirPath+"ejs_lib.jar"+ File.pathSeparatorChar);
    String extDirPath = _binDirPath + OsejsCommon.EXTENSIONS_DIR_PATH + "/";
    for (PathAndFile paf : OsejsCommon.getLibraryFiles(new File (_binDir,OsejsCommon.EXTENSIONS_DIR_PATH))) {
      textList.append(extDirPath+paf.getPath()+File.pathSeparatorChar);
    }
    for (PathAndFile paf : _list) textList.append(_srcDirPath+paf.getPath()+File.pathSeparatorChar);
    return textList.toString().replace('/',File.separatorChar);
  }


  /**
   * Converts a collection of filenames into a list of MyPathAndFile
   * @param _ejs
   * @param _xmlFile
   * @param _relativeParent
   * @param _pathList
   * @return
   */
  static public Set<PathAndFile> getPathAndFile (Osejs _ejs, File _parentDir, String _relativeParent, Collection<String> _pathList) {
    Set<PathAndFile> list = new HashSet<PathAndFile>();
    for (String path : _pathList) {
      String fullPath = path;
      File file;
      if (fullPath.startsWith("./")) {
        file = new File (_parentDir,fullPath); // Search relatively to the xmlFile location
        fullPath = _relativeParent + fullPath.substring(2); // complete the relative path 
      }
      else file = new File (_ejs.getSourceDirectory(),fullPath); // Search absolutely in EJS user directory
      if (file.exists()) list.add(new PathAndFile(fullPath,file));
      else _ejs.getOutputArea().println(res.getString("Generate.JarFileResourceNotFound")+": "+path);
    }
    return list;
  }


  // ----------------------------------------------------
  // Generation of HTML code
  // ----------------------------------------------------

  static private class SimInfo {
    String name, fullPath, path, classpath, jarPath;  
  }
  
  static private String generateGroupHtml (Osejs _ejs, java.util.List<PathAndFile> _metadataFilesList, File _groupDirectory, 
                                           String _targetName, boolean _fullJars) {
    String ret = System.getProperty("line.separator");
    File binDir = _ejs.getBinDirectory();
    String binDirPath = FileUtils.getPath(binDir);
    String outputDirPath = FileUtils.getPath(_ejs.getOutputDirectory());
    
    // Create the empty list of common files and a dictionary with information
    Set<PathAndFile> commonList = null;
    Set<SimInfo> infoList = new HashSet<SimInfo>();
        
    // --- Begin of the HTML page for the table of contents
    StringBuffer code = new StringBuffer();
    code.append("<html>"+ret);
    code.append("  <head>"+ret);
    code.append("    <title>Group page</title>"+ret);
    code.append("    <base target=\"_self\">" + ret);
    code.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\""+_groupDirectory.getName()+"/_ejs_library/css/ejsGroupPage.css\"></link>" + ret);
    code.append("  </head>"+ret);
    code.append("  <body "+_ejs.getOptions().getHtmlBody()+"> "+ret);
    code.append("    <h1>" + _targetName + "</h1>"+ret);
    code.append("    <div class=\"contents\">"+ret);

    // Possible missing auxiliary files
    Set<String> missingAuxFiles = new HashSet<String>();

    for (PathAndFile metadataPathAndFile : _metadataFilesList) {
      SimInfo info = new SimInfo();
      File generatedDir = metadataPathAndFile.getFile().getParentFile();
      info.fullPath = FileUtils.getPath(generatedDir);
      info.name = FileUtils.getPlainName(metadataPathAndFile.getFile());
      info.path = FileUtils.getRelativePath(info.fullPath,outputDirPath,false);
      info.jarPath = FileUtils.getPath(new File(generatedDir,info.name+".jar"));
      
      // See if there are HTML files for this simulation
      File simHTMLFile = new File (_ejs.getOutputDirectory(),info.path+info.name+".html");
      if (!simHTMLFile.exists()) {
        _ejs.getOutputArea().message("Package.IgnoringSimulation",info.path+info.name);
        continue;
      }
      _ejs.getOutputArea().message("Package.ProcessingSimulation",info.path+info.name);

      // read the meta data
      Metadata metadata = Metadata.readFile(metadataPathAndFile.getFile(), info.path);

      // Process the class path
      info.classpath = metadata.getClassname().replace('.','/');
      
      // Save the information
      infoList.add(info);

      // Create the entry for this simulation in the common HTML file
      code.append("      <div class=\"simulation\"><b>"+res.getString("Generate.HtmlSimulation") +":</b> <a href=\""+ 
          _groupDirectory.getName()+ "/" + info.path + info.name+".html\" target=\"blank\">" + info.name+"</a></div>"+ret);
      
      String abstractText = metadata.getAbstract();
      //if (abstractText.length()>0) 
        code.append("      <div class=\"abstract\"><b>"+res.getString("SimInfoEditor.Abstract")+"</b> " + abstractText+"</div>"+ret);

      // Add the HTML files
      for (String filename : metadata.getFilesCreated())
        if (filename.endsWith(".html")) {
          File file = new File (generatedDir,filename);
          File targetFile = new File(_groupDirectory,info.path+filename);
          if (_fullJars && filename.endsWith("_Simulation.html")) { // remove the common.jar entry in the archive parameter of the applet tag 
            String content = FileUtils.readTextFile(file,null);
            content = FileUtils.replaceString(content, "archive=\"common.jar,", "archive=\"");
            try { FileUtils.saveToFile(targetFile,null,content); }
            catch (IOException ioe) {
              _ejs.getOutputArea().println(res.getString("Package.CopyError") + " " + FileUtils.getPath(file));
              return null;
            }
          }
          else if (!JarTool.copy(file,targetFile)) {
            _ejs.getOutputArea().println(res.getString("Package.CopyError") + " " + FileUtils.getPath(file));
            return null;
          }
        }

      // Copy auxiliary files
      for (String auxPath : metadata.getAuxiliaryFiles()) {
        File auxFile = new File(_ejs.getSourceDirectory(),auxPath);
        if (!auxFile.exists()) {
          _ejs.getOutputArea().println(res.getString("Generate.JarFileResourceNotFound")+": "+auxPath);
          missingAuxFiles.add(auxPath);
        }
        else if (auxFile.isDirectory()) { // It is a complete directory
          for (File file : JarTool.getContents(auxFile)) {
            if (!JarTool.copy(file, new File(_groupDirectory,FileUtils.getRelativePath(file,_ejs.getSourceDirectory(),false)))) {
              _ejs.getOutputArea().println(res.getString("Package.CopyError") + " " + FileUtils.getPath(auxFile));
              return null;
            }
          }
        }
        else if (!JarTool.copy(auxFile, new File(_groupDirectory,auxPath))) {
          _ejs.getOutputArea().println(res.getString("Package.CopyError") + " " + FileUtils.getPath(auxFile));
          return null;
        }
      }

      if (!_fullJars) { // Do this only if you don't want to extract the common.jar
        // Create an instance of MiniJar for this simulation
        MiniJar minijarParticular= new MiniJar();
        minijarParticular.addExclude ("++Thumbs.db");
        minijarParticular.addExclude ("++.DS_Store");
        addResources(_ejs,minijarParticular);
        
        // Add resources needed by the view elements when packaging. For example, CamImage adds  "com/charliemouse/++.gif"
//        for (String resource : metadata.getResourcePatterns()) minijarParticular.addDesired(resource);

        minijarParticular.addSourcePath(binDirPath+"osp.jar");
        minijarParticular.addSourcePath(binDirPath+"ejs_lib.jar");
        for (PathAndFile paf : OsejsCommon.getLibraryFiles(new File (binDir,OsejsCommon.EXTENSIONS_DIR_PATH))) {
          minijarParticular.addSourcePath(FileUtils.getPath(paf.getFile()));
        }
        minijarParticular.addSourcePath(info.jarPath);

        // Add the applet class for this simulation
        minijarParticular.addDesired(info.classpath+"Applet.class");
        
        //CJB for collaborative
        if(_ejs.getSimInfoEditor().addAppletColSupport()){
            File collabDir = new File (_ejs.getBinDirectory(),OsejsCommon.CONFIG_DIR_PATH+"/Collaborative");
            String collabDirPath = FileUtils.getPath(collabDir);
        	minijarParticular.addDesired(info.classpath+"AppletStudent.class");
        	minijarParticular.addDesired("org/colos/ejs/library/collaborative/images/++");
        	for (File file : JarTool.getContents(collabDir)) {
        		//System.err.println ("Copiando "+file.getAbsolutePath());
          		String destName = FileUtils.getRelativePath(file, collabDirPath, false);
          		JarTool.copy(file, new File (_groupDirectory,destName));
        	}
        }
        //CJB for collaborative
        
        // get the matches
        Set<PathAndFile> matches = minijarParticular.getMatches();

        // Check against the common MiniJar
        Set<PathAndFile> newList = new HashSet<PathAndFile>();
        String packagePath = info.classpath;

        if (commonList==null) { // Exclude the files particular to this simulation
          for (PathAndFile paf : matches) if (!paf.getPath().startsWith(packagePath)) newList.add(paf);  
        }
        else { // Exclude the files particular to this simulation
          for (PathAndFile paf : matches) {
            if (paf.getPath().startsWith(packagePath)) continue; // exclude those of this simulation
            if (commonList.contains(paf)) newList.add(paf);  
          }
        }
        commonList = newList;
      }

    } // end of first 'for' for simulations
    
    File commonJarFile = new File(_groupDirectory,"common.jar");
    if (!_fullJars) { // Create the common jar using MiniJar
      MiniJar.compress(commonList, commonJarFile, MiniJar.createManifest(".","org.colos.ejs.library._EjsConstants"));
    }
    
    // Now do a second for to create the individual jar files
    for (SimInfo info : infoList) {
      // Create an instance of MiniJar for this simulation
      MiniJar minijarParticular= new MiniJar();
      minijarParticular.setOutputFile(new File (_groupDirectory,info.path+info.name+".jar"));
      //minijarParticular.setManifestFile(null);
      minijarParticular.addExclude ("++Thumbs.db");
      minijarParticular.addExclude ("++.DS_Store");
      addResources(_ejs,minijarParticular);
      minijarParticular.addSourcePath(binDirPath+"osp.jar");
      minijarParticular.addSourcePath(binDirPath+"ejs_lib.jar");
      for (PathAndFile paf : OsejsCommon.getLibraryFiles(new File (binDir,OsejsCommon.EXTENSIONS_DIR_PATH))) {
        minijarParticular.addSourcePath(FileUtils.getPath(paf.getFile()));
      }
      minijarParticular.addSourcePath(info.jarPath);
      if (!_fullJars) minijarParticular.addExclude (commonJarFile);

      // Add resources needed by the view elements when packaging. For example, CamImage adds  "com/charliemouse/++.gif"
      //for (String resource : metadata.getResourcePatterns()) minijar.addDesired(resource);

      // Add the applet class for this simulation
      minijarParticular.addDesired(info.classpath+"Applet.class");
      
      //CJB for collaborative
      if(_ejs.getSimInfoEditor().addAppletColSupport()){
    	  minijarParticular.addDesired(info.classpath+"AppletStudent.class");
    	  minijarParticular.addDesired("org/colos/ejs/library/collaborative/images/++");
    	  File collabDir = new File (_ejs.getBinDirectory(),OsejsCommon.CONFIG_DIR_PATH+"/Collaborative");
    	  String collabDirPath = FileUtils.getPath(collabDir);
    	  for (File file : JarTool.getContents(collabDir)) {
    		  //System.err.println ("Copiando "+file.getAbsolutePath());
    		  String destName = FileUtils.getRelativePath(file, collabDirPath, false);
    		  JarTool.copy(file, new File (_groupDirectory,destName));
    	  }
      }
    
      //CJB for collaborative
      
      // get the matches and compress
      minijarParticular.compress();
    }

    code.append("    </div>"+ret); // End of contents
    code.append("  </body>"+ret);
    code.append("</html>"+ret);
    OsejsCommon.warnAboutFiles(_ejs.getMainPanel(),missingAuxFiles,"SimInfoEditor.RequiredFileNotFound");
    return code.toString();
  }
   
  /**
   * Returns the list of locales desired
   * @param _ejs
   * @return
   */
  static private Set<LocaleItem> getLocalesDesired (Osejs _ejs) {
    if (_ejs.getSimInfoEditor().addTranslatorTool()) return _ejs.getTranslationEditor().getDesiredTranslations();
    Set<LocaleItem> localesDesired = new HashSet<LocaleItem>();
    localesDesired.add(LocaleItem.getDefaultItem());
    return localesDesired;
  }
  
  /**
   * Adds the set of HTML files required for a frame access to the simulation description pages.
   * The key in the table is the name of the HTML file. The calling method must process these files.
   * @param _htmlTable Hashtable<String,StringBuffer> The table of HTML files
   * @param _ejs Osejs The calling Ejs
   * @param _simulationName String The name of the simulation
   * @param _filename String The base name for the HTML files (different from _simulationName if generating in a server)
   * @param _classpath String The classpath required to run the simulation.
   * @return Hashtable<String,String>
   * @throws IOException
   */
  static private void addFramesHtml (Hashtable<String,StringBuffer> _htmlTable, Osejs _ejs,
                                     String _simulationName, String _javaName, String _packageName,
                                     String _pathToLib, String _archiveStr) {
    String ret = System.getProperty("line.separator");
    boolean left = true;  // Whether to place the content frame at the left frame (true) or at the top frame (false)
    if (_ejs.getOptions().generateHtml()==EjsOptions.GENERATE_TOP_FRAME) left = false;

    // --- BEGIN OF the HTML page for the table of contents
    StringBuffer code = new StringBuffer();
    code.append("<html>"+ret);
    code.append("  <head>"+ret);
    code.append("    <title>Contents</title>"+ret);
    code.append("    <base target=\"_self\">" + ret);
    if (left) code.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\""+_pathToLib+"_ejs_library/css/ejsContentsLeft.css\"></link>" + ret);
    else      code.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\""+_pathToLib+"_ejs_library/css/ejsContentsTop.css\"></link>" + ret);
    code.append("  </head>"+ret);
    code.append("  <body "+_ejs.getOptions().getHtmlBody()+"> "+ret);
    code.append("    <h1>" + _simulationName + "</h1>"+ret);
    code.append("    <h2>" + res.getString("Generate.HtmlContents") + "</h2>"+ret);
    code.append("    <div class=\"contents\">"+ret);
    
    // Add an entry for each Introduction page created by the user
    OneHtmlPage firstPage = null;
    Vector<Editor> pageList = _ejs.getDescriptionEditor().getPages();
    int counter = 0;
    Set<LocaleItem> localesDesired = getLocalesDesired(_ejs);
    for (java.util.Enumeration<Editor> e = pageList.elements(); e.hasMoreElements(); ) {
      HtmlEditor htmlEditor = (HtmlEditor) e.nextElement();
      if (htmlEditor.isActive()) {
        counter++;
        for (LocaleItem item : localesDesired) {
          OneHtmlPage htmlPage = htmlEditor.getHtmlPage(item);
          if (htmlPage==null || htmlPage.isEmpty()) continue;
          String link;
          if (htmlPage.isExternal()) link = _pathToLib + htmlPage.getLink();
          else link = _simulationName+"_Intro "+counter+ (item.isDefaultItem() ? "" : "_"+item.getKeyword()) + ".html";
          if (firstPage==null) firstPage = htmlPage;
          code.append("      <div class=\"intro\"><a href=\""+ link+"\" target=\"central\">" + htmlPage.getTitle()+"</a></div>"+ret);
        }
      }
    } // end of for
    
    // And an extra link for the simulation itself!
    code.append("      <div class=\"simulation\"><a href=\""+ _simulationName+"_Simulation.html\" target=\"central\">" + res.getString("Generate.HtmlSimulation")+"</a></div>"+ret);
    code.append("    </div>"+ret); // End of contents
    // ---- Now the logo
    code.append("    <div class=\"signature\">"+ res.getString("Generate.HtmlEjsGenerated") + " "
               + "<a href=\"http://fem.um.es/Ejs\" target=\"_blank\">Easy Java Simulations</a></div>"+ret);
    code.append("  </body>"+ret);
    code.append("</html>"+ret);

    _htmlTable.put ("_Contents",code);
    
    // --- END OF the HTML page for the table of contents

    // --- The main HTML page
    code = new StringBuffer();
    code.append("<html>"+ret);
    code.append("  <head>"+ret);
    code.append("    <title> " + res.getString("Generate.HtmlFor") + " " + _simulationName + "</title>"+ret);
    code.append("  </head>"+ret);
    if (left) code.append("  <frameset cols=\"25%,*\">"+ret);
    else      code.append("  <frameset rows=\"90,*\">"+ret);
    code.append("    <frame src=\""+_simulationName+"_Contents.html\" name=\"contents\" scrolling=\"auto\" target=\"_self\">"+ret);
//    if (firstOne!=null) code.append("    <frame src=\""+_filename+"_"+firstOne+".html\"");
    if (firstPage!=null) {
      String link;
      if (firstPage.isExternal()) link = _pathToLib + firstPage.getLink();
      else link = _simulationName+"_Intro "+counter+ ".html";
      code.append("    <frame src=\""+link+"\"");
    }
    else code.append("    <frame src=\""+_simulationName+"_Simulation.html\"");
    code.append(" name=\"central\" scrolling=\"auto\" target=\"_self\">"+ret);
    code.append("    <noframes>"+ret);
    code.append("      Gee! Your browser is really old and doesn't support frames. You better update!!!"+ret);
    code.append("    </noframes>"+ret);
    code.append("  </frameset> "+ret);
    code.append("</html>"+ret);
    
    _htmlTable.put ("",code);

    // --- An HTML page for the simulation itself
    code = new StringBuffer();
    code.append("<html>"+ret);
    code.append("  <head>"+ret);
    //code.append("    <base href=\".\" />"+ret);
    code.append("    <title> " + res.getString("Generate.HtmlFor") + " " + _simulationName + "</title>"+ret);
    code.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\""+_pathToLib+"_ejs_library/css/ejsSimulation.css\"></link>" + ret);
    code.append("  </head>"+ret);
    code.append("  <body "+_ejs.getOptions().getHtmlBody()+"> "+ret);
    code.append(generateHtmlForSimulation (_ejs, _simulationName, _javaName, _packageName, _pathToLib, _archiveStr, null)); // null for no Emersion
    code.append("  </body>"+ret);
    code.append("</html>"+ret);
    
    _htmlTable.put ("_Simulation",code);

    // And this makes the set of html files required for a frame configuration
  }


  /**
   * Adds the set of HTML files for the simulation description pages.
   * The key in the table is the name of the HTML file. The calling method must process these files.
   * @param _htmlTable Hashtable<String,StringBuffer> The table of HTML files
   * @param _ejs Osejs The calling Ejs
   * @param _simulationName String The name of the simulation
   * @param _filename String The base name for the HTML files (different from _simulationName if generating in a server)
   * @param _classpath String The classpath required to run the simulation.
   * @return Hashtable<String,String>
   * @throws IOException
   */
  static private void addDescriptionHtml (Hashtable<String,StringBuffer> _htmlTable, Osejs _ejs,
                                     String _simulationName, String _javaName, String _packageName,
                                     String _pathToLib, String _archiveStr) {

    String ret = System.getProperty("line.separator");
    Set<LocaleItem> localesDesired = getLocalesDesired(_ejs);
    // --- An HTML page for each introduction page
    int counter = 0;
    for (java.util.Enumeration<Editor> e = _ejs.getDescriptionEditor().getPages().elements(); e.hasMoreElements(); ) {
      HtmlEditor htmlEditor = (HtmlEditor) e.nextElement();
      if (!htmlEditor.isActive()) continue;
      counter++;
      for (LocaleItem item : localesDesired) {
        OneHtmlPage htmlPage = htmlEditor.getHtmlPage(item);
        if (htmlPage==null || htmlPage.isExternal() || htmlPage.isEmpty()) continue;
        StringBuffer code = new StringBuffer();
        code.append("<html>"+ret);
        code.append("  <head>"+ret);
        //code.append("    <base href=\""+_pathToLib+"\" />"+ret);
        code.append("    <title> " + htmlPage.getTitle() + "</title>"+ret);
        code.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\""+_pathToLib+"_ejs_library/css/ejsPage.css\"></link>" + ret);
        code.append("  </head>"+ret);
        code.append("  <body "+_ejs.getOptions().getHtmlBody()+"> "+ret);
        code.append(htmlPage.getHtmlCode (null));
        code.append("  </body>"+ret);
        code.append("</html>"+ret);
        _htmlTable.put ("_Intro "+counter+ (item.isDefaultItem() ? "" : "_"+item.getKeyword()),code);
      }
    }
  }

  /**
   * Generates the code for the HTML page for eMersion
   * @param _ejs Osejs The calling EJS
   * @param _simulationName String The name of the simulation
   * @param _filename String The base name for the HTML files (different from _name if generating in a server)
   * @param _classpath String The class path required to run the simulation.
   * @param _url String The URL for the simulation in the eMersion server
   * @return StringBuffer A StringBuffer with the code
   * @throws IOException
   *
  static private String generateEmersionHtml (Osejs _ejs, String _simulationName, String _javaName, String _packageName,
                                              String _pathToHTML, String _pathToLib, String _archiveStr) throws IOException {
    String ret = System.getProperty("line.separator");
    StringBuffer code = new StringBuffer();
    code.append("<html>"+ret);
    code.append("  <head>"+ret);
    code.append("    <title> " + res.getString("Generate.HtmlFor") + " " + _simulationName + "</title>"+ret);
    code.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\""+_pathToLib+"_ejs_library/css/ejsSimulation.css\"></link>" + ret);
    code.append("  </head>"+ret);
    code.append("  <body "+_ejs.getOptions().getHtmlBody()+"> "+ret);
    code.append(generateHtmlForSimulation (_ejs, _simulationName, _javaName, _packageName, _pathToLib, _archiveStr, _pathToHTML));
    code.append("  </body>"+ret);
    code.append("</html>"+ret);
    return code.toString();
  }
*/
  
  /**
   * Generates the code for a single HTML page with everything, and adds it to a table of HTML pages
   * @param _htmlTable Hashtable<String,StringBuffer> The table of html pages 
   * @param _ejs Osejs The calling Ejs
   * @param _simulationName String The name of the simulation
   * @param _filename String The base name for the HTML files (different from _name if generating in a server)
   * @param _classpath String The classpath required to run the simulation.
   * @return StringBuffer A StringBuffer with the code
   * @throws IOException
   */
  static private void addNoFramesHtml (Hashtable<String,StringBuffer> _htmlTable, Osejs _ejs, String _simulationName, String _javaName, String _packageName,
                                              String _pathToLib, String _archiveStr) {
    String ret = System.getProperty("line.separator");
    Set<LocaleItem> localesDesired = getLocalesDesired(_ejs);

    StringBuffer code = new StringBuffer();
    code.append("<html>"+ret);
    code.append("  <head>"+ret);
    //code.append("    <base href=\""+_pathToLib+"\" />"+ret);
    code.append("    <title> " + res.getString("Generate.HtmlFor") + " " + _simulationName + "</title>"+ret);
    code.append("    <link rel=\"stylesheet\" type=\"text/css\" href=\""+_pathToLib+"_ejs_library/css/ejsPage.css\"></link>" + ret);
    code.append("  </head>"+ret);
    code.append("  <body "+_ejs.getOptions().getHtmlBody()+"> "+ret);
    code.append(res.getString("Generate.HtmlUserCode")+ret);
    
    // --- An HTML page for each introduction page
    int counter = 0;
    String info = "<!--- " + res.getString("Osejs.Main.Description") + ".";
    String separator = ret + "<br><hr width=\"100%\" size=\"2\"><br>" + ret;
    for (java.util.Enumeration<Editor> e = _ejs.getDescriptionEditor().getPages().elements(); e.hasMoreElements(); ) {
      HtmlEditor htmlEditor = (HtmlEditor) e.nextElement();
      if (!htmlEditor.isActive()) continue;
      counter++;
      for (LocaleItem item : localesDesired) {
        OneHtmlPage htmlPage = htmlEditor.getHtmlPage(item);
        if (htmlPage==null || htmlPage.isExternal() || htmlPage.isEmpty()) continue;
        code.append(info);
        code.append(htmlEditor.getName() + (item.isDefaultItem() ? "" : item.toString()) + " --->" + ret);
        code.append(htmlPage.getHtmlCode (null));
        code.append(separator);
      }
    }
//    code.append(_ejs.getDescriptionEditor().generateCode(Editor.GENERATE_CODE,res.getString("Osejs.Main.Description")));

    code.append(res.getString("Generate.HtmlHereComesTheApplet")+ret);
    code.append(generateHtmlForSimulation (_ejs,_simulationName, _javaName, _packageName,_pathToLib,_archiveStr, null)); // null for no Emersion
    code.append("  </body>"+ret);
    code.append("</html>"+ret);
    
    _htmlTable.put ("",code);
  }

  /**
   * Generates the code for the applet tag which includes the simulation in an HTML page
   * @param _ejs Osejs The calling EJS
   * @param _simulationName String The name of the simulation
   * @param _packageName String The package of the class
   * @param _classpath String The class path required to run the simulation as an applet
   * @param _eMersionCodebase String The URL for the codebase in case the applet is run from eMersion (null otherwise)
   * @return StringBuffer A StringBuffer with the code
   */
  static private String generateHtmlForSimulation (Osejs _ejs, String _simulationName, String _javaName, String _packageName,
                                                   String _pathToLib, String _archiveStr, String _pathForEmersion) {
    String ret = System.getProperty("line.separator");
    String captureTxt = _ejs.getViewEditor().generateCode(Editor.GENERATE_CAPTURE_WINDOW,"").toString();
    StringBuffer code = new StringBuffer();
    code.append("    <div class=\"appletSection\">"+ret);
    if (_pathForEmersion==null) { // not for Emersion
      if (captureTxt.trim().length()<=0) code.append("      <h3>"+res.getString("Generate.HtmlHereItIsNot")+"</h3>"+ret);
      else code.append("      <h3>"+res.getString("Generate.HtmlHereItIs")+"</h3>"+ret);
    }
    code.append("      <applet code=\""+ _packageName + "." + _javaName+"Applet.class\"" +ret);

    if (_pathForEmersion!=null) { // Whenever there is a codebase, the full classpath is required
      String eMersionCodebase = ""; //_ejs.getOptions().eMersionGetURL();
      if (eMersionCodebase.length()<=0) eMersionCodebase = "http://localhost:8080";
      code.append("              codebase=\""+eMersionCodebase+"/"+_pathForEmersion+"\" ");
      code.append("archive=\""+_archiveStr +FileUtils.correctUrlString(OsejsCommon.firstToLower(_simulationName)) + ".jar\""+ret);
    }
    else {
      if (_pathToLib.length()<=0) code.append("              codebase=\".\"");
      else code.append("              codebase=\""+_pathToLib+"\"");
      code.append(" archive=\""+_archiveStr +"\"" +ret);
    }
    code.append("              name=\"" + _javaName + "\"  id=\"" + _javaName + "\""+ret);
    if (captureTxt.trim().length()<=0) code.append("              width=\"0\" height=\"0\"");
    else code.append("      "+captureTxt);

    code.append(">"+ret);
    code.append("        <param name=\"draggable\" value=\"true\">"+ret);
    code.append("      </applet>"+ret);
    code.append("    </div>"+ret);
    if (_pathForEmersion!=null) return code.toString();

    if (_ejs.getOptions().experimentsEnabled() && _ejs.getExperimentEditor().getActivePageCount()>0) {
      code.append("    <div class=\"experimentsSection\">"+ret);
      code.append("      <h3>" + res.getString("Generate.TheExperiments")+"</h3>"+ret);
      code.append(_ejs.getExperimentEditor().generateCode(Editor.GENERATE_DECLARATION,_javaName));
//      code.append("<input type=\"BUTTON\" value=\"" + ToolsRes.getString("MenuItem.KillExperiment") + "\"" +
//                  " onclick=\"document."+_javaName+"._simulation.killExperiment();\";>");
      code.append("      <div class=\"killExperiment\"><a href=\"javascript:document."+_javaName+"._simulation.killExperiment();\">"+
                      ToolsRes.getString("MenuItem.KillExperiment") + "</a></div>"+ret);
      code.append("    </div>"+ret);
    }

    code.append("      "+res.getString("Generate.HtmlHereComesTheJavaScript")+ret);
    code.append("    <div class=\"jsSection\">"+ret);
    code.append("      <h3>" + res.getString("Generate.HtmlJSControl")+"</h3>"+ret);
    code.append(jsCommand(_javaName,"Generate.HtmlPlay","_play()")+ret);
    code.append(jsCommand(_javaName,"Generate.HtmlPause","_pause()")+ret);
    code.append(jsCommand(_javaName,"Generate.HtmlReset","_reset()")+ret);
    code.append(jsCommand(_javaName,"Generate.HtmlStep","_step()")+ret);
    code.append(jsCommand(_javaName,"Generate.HtmlSlow","_setDelay(1000)")+ret);
    code.append(jsCommand(_javaName,"Generate.HtmlFast","_setDelay(100)")+ret);
    code.append(jsCommand(_javaName,"Generate.HtmlFaster","_setDelay(10)")+ret);
    //CJB for collaborative
    if(_ejs.getSimInfoEditor().addAppletColSupport())
    	code.append(jsCommand(_javaName,"Generate.HtmlStartCollaboration","_simulation.startColMethod()")+ret);
    //CJB for collaborative
    code.append("    </div>"+ret);
/*20060903
    if(_ejs.ejsCommand){ //FKH20060402
      code.append("<script>"+ret);
      code.append("function ejsCommand(cmd){"+ret);
      code.append(" eval(cmd);"+ret);
      code.append("}"+ret);
      code.append("</script>"+ret);
    }
*/
    return code.toString();
  }

  static private String jsCommand (String _javaName, String _label, String _method) {
//    return "<input type=\"BUTTON\" value=\"" + res.getString(_label)  +
//      "\" onclick=\"document." + _javaName + "."+_method+";document." + _javaName + "._simulation.update();\";>";
    return "      <a href=\"javascript:document."+_javaName+"."+_method+";document."+_javaName+"._simulation.update();\">"+
              res.getString(_label)  + "</a> ";
  }

  /**
   * Generates the JNLP file required to run the simulation using Java Web Start
   * @param _simulationName String The name of the simulation
   * @param _filename String The base name for the HTML files (different from _name if generating in a server)
   * @param _jarList AbstractList The list of jar files required to run the simulation
   * @param _jnlpURL String The URL for the Java Web Start server
   * @return StringBuffer A StringBuffer with the code
   *
  static private String generateJNLP (Osejs _ejs, String _path, 
                                            String _simulationName, String _classname, String _packageName,
                                            List<PathAndFile> _jarList) {
    String jnlpURL = FileUtils.correctUrlString(_ejs.getOptions().jnlpURL());
    StringBuffer code = new StringBuffer();
    String pathCorrected = FileUtils.correctUrlString(_path);
    String ret = System.getProperty("line.separator");
    if (jnlpURL.length()<=0) jnlpURL = "http://localhost/jaws";
    code.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>"+ret);
    code.append("<jnlp spec=\"1.0\" codebase=\""+jnlpURL+"/"+pathCorrected+
      "\" href=\"" + FileUtils.correctUrlString(_simulationName)+".jnlp\">"+ret);
    code.append("  <information>"+ret);
    code.append("    <title>"+_simulationName+"</title>"+ret);
    code.append("    <vendor>"+res.getString("Generate.HtmlEjsGenerated")+ " Ejs</vendor>"+ret);
    code.append("    <homepage href=\"http://fem.um.es/Ejs\"/>"+ret);
    code.append("    <description>"+res.getString("Generate.HtmlEjsGenerated")+ " Ejs</description>"+ret);
    code.append("    <icon href=\"_library/EjsIcon.gif\"/>"+ret);
    code.append("    <offline-allowed/>"+ret);
    code.append("  </information>"+ret);
    code.append("  <resources>"+ret);
    code.append("    <j2se version=\"1.4.2+\" href=\"http://java.sun.com/products/autodl/j2se\"/>"+ret);
    code.append("    <jar href=\""+FileUtils.correctUrlString(OsejsCommon.firstToLower(_simulationName))+".jar\"/>"+ret);
    for (Iterator it = _jarList.iterator(); it.hasNext(); ) {
      code.append("    <jar href=\"" + FileUtils.correctUrlString((String) it.next()) + "\"/>" + ret);
    }
    code.append("    <!-- Simulations use the following codebase to find resources -->"+ret);
    code.append("    <property name=\"jnlp.codebase\" value=\""+jnlpURL+"/"+pathCorrected+"\"/>"+ret);
    code.append("  </resources>"+ret);
    code.append("  <application-desc main-class=\""+ _packageName+"."+_classname+"\"/>"+ret);
    code.append("</jnlp>"+ret);
    return  code.toString();
  }
*/
  // ----------------------------------------------------
  // Generation of Java code
  // ----------------------------------------------------

  /**
   * Generates the header for the Java classes
   * @param _filename String The name of the simulation file
   * @param _suffix String Either "" for the simulation or "Applet" for the applet
   * @return StringBuffer
   */
  static private String generateHeader (Osejs _ejs, String _classname, String _packageName, String _whichClass) {
    StringBuffer txt = new StringBuffer();
    txt.append("/*\n");
    txt.append(" * Class : "+_classname+_whichClass+".java\n");
    txt.append(" *  Generated using ");
    txt.append(" *  Easy Java Simulations Version "+_EjsConstants.VERSION+", build "+_EjsConstants.VERSION_DATE+". Visit "+_EjsConstants.WEB_SITE+"\n");
    txt.append(" */ \n\n");
    txt.append("package " + _packageName + ";\n\n");
    txt.append("import org.colos.ejs.library._EjsConstants;\n\n");
    for (String anImport : _ejs.getSimInfoEditor().getImportsList()) txt.append("import "+anImport+"\n");
    return txt.toString();
  }

  
  static private void addCheckPasswordCode(Osejs _ejs, String _xmlName, String _returnValue, StringBuffer _buffer) {
    String execPassword = _ejs.getSimInfoEditor().getExecPassword();
    if (execPassword.length()>0) {
      String codedPassword = new Cryptic(execPassword).getCryptic();
      _buffer.append("      try {\n");
      _buffer.append("        boolean __identified=false;\n");
      _buffer.append("        String __codedPassword = System.getProperty(\"launcher.password\");\n");
//      _buffer.append("        System.out.println (\"Password is \"+__codedPassword);\n");
      _buffer.append("        if (__codedPassword!=null) {\n");
//      _buffer.append("          __codedPassword = __codedPassword.substring(1,__codedPassword.length()-1);\n");
//      _buffer.append("          System.out.println (\"Final password is \"+__codedPassword);\n");
      _buffer.append("          __identified = \""+codedPassword+"\".equals(__codedPassword);\n");
      _buffer.append("        }\n");
      _buffer.append("        if (!__identified) { // Ask the user for the password\n");
      _buffer.append("          java.awt.Window infoWindow = null;\n");

      Vector<Editor> pageList = _ejs.getDescriptionEditor().getPages();
      int counter = 0;
      for (java.util.Enumeration<Editor> e = pageList.elements(); e.hasMoreElements(); ) {
        HtmlEditor htmlEditor = (HtmlEditor) e.nextElement();
        if (htmlEditor.isActive()) {
          counter++;
          java.awt.Dimension size = htmlEditor.getComponent().getSize();
          if (size.width<=0 || size.height<=0) size = _ejs.getOptions().getHtmlPanelSize(); // This happens in batch compilation
          _buffer.append("          org.colos.ejs.library.utils.HtmlPageInfo htmlPageInfo = "+_xmlName+".getHtmlPageClassInfo(_pageName, org.colos.ejs.library.utils.LocaleItem.getDefaultItem());\n");
          _buffer.append("          if (htmlPageInfo!=null) org.colos.ejs.library.utils.PasswordDialog.showInformationPage (\""+_xmlName+"\", htmlPageInfo.getTitle(),htmlPageInfo.getLink(),"+
//          _buffer.append("          infoWindow = org.colos.ejs.library.utils.PasswordDialog.showInformationPage(\""+_xmlName+"\",\""+htmlEditor.getName()+"\","+
              size.width+","+size.height+");\n");
          break; // Only one page
        }
      }
      _buffer.append("          if (org.colos.ejs.library.utils.PasswordDialog.checkPassword(\""+codedPassword+"\",null,infoWindow)==null) return "+_returnValue+";\n");
      _buffer.append("        }\n");
      _buffer.append("      } catch (Exception exc) {} // do nothing on error\n");
    }
  }
  
  /**
   * Generates the Java code for the applet
   * @param _filename String The name of the simulation file
   * @param _mainFrame String The name of the main frame in Ejs' view
   * @return StringBuffer
   */
  static private String generateApplet (Osejs _ejs, String _classname, String _packageName, String _parentPath, String _mainFrame) {
    StringBuffer code = new StringBuffer();
    code.append(generateHeader(_ejs, _classname, _packageName, "Applet"));
    code.append("public class " + _classname + "Applet extends org.colos.ejs.library.LauncherApplet {\n\n");

    code.append("  static {\n");
//    if (_ejs.getSimInfoEditor().addTranslatorTool()) code.append("    org.opensourcephysics.display.OSPRuntime.loadTranslatorTool = !org.opensourcephysics.display.OSPRuntime.appletMode;\n");
//    else code.append("    org.opensourcephysics.display.OSPRuntime.loadTranslatorTool = false;\n");
    code.append("    org.opensourcephysics.display.OSPRuntime.loadTranslatorTool = false;\n");
    if (!_ejs.getSimInfoEditor().addCaptureTools())   code.append("    org.opensourcephysics.display.OSPRuntime.loadVideoTool = false;\n");
    if (!_ejs.getSimInfoEditor().addToolsForData())   {
      code.append("    org.opensourcephysics.display.OSPRuntime.loadDataTool = false;\n");
      code.append("    org.opensourcephysics.display.OSPRuntime.loadFourierTool = false;\n");
    }
    code.append("    org.opensourcephysics.display.OSPRuntime.loadExportTool = false;\n");
    code.append("  }\n\n");
    
    //code.append("  private java.awt.Component mainComponent=null;\n\n");
    code.append("  public void init () {\n");
    code.append("    super.init();\n");
    code.append("    org.opensourcephysics.tools.ResourceLoader.addAppletSearchPath(\"/"+_parentPath +"\");\n");
    code.append("    org.opensourcephysics.tools.ResourceLoader.addSearchPath(getCodeBase()+\""+_parentPath+"\"); // This is for relative files\n");
    code.append("    org.opensourcephysics.tools.ResourceLoader.addSearchPath(\""+_parentPath+"\"); // This is for relative files, too\n");
    code.append("    //org.colos.ejs.library.Simulation.setPathToLibrary(getCodeBase()); // This is for classes (such as EjsMatlab) which needs to know where the library is\n");
    addHtmlPagesMapCode (_ejs,_classname, code);
    addCheckPasswordCode (_ejs,_classname,"",code);

    code.append("    if (getParentFrame()!=null) {\n");
    code.append("      _model = new "+_classname+" ("+_mainFrame+",getParentFrame(),getCodeBase(),this,(String[])null,true);\n");
    code.append("      _simulation = _model._getSimulation();\n");
    code.append("      _view = _model._getView();\n");
    //code.append("      mainComponent = captureWindow (_model._getView(),"+_mainFrame+");\n");
    code.append("    }\n");
    code.append("    else {\n");
    code.append("      _model = new "+_classname+" (null,null,getCodeBase(),this,(String[])null,true);\n");
    code.append("      _simulation = _model._getSimulation();\n");
    code.append("      _view = _model._getView();\n");
    code.append("    }\n");
//    code.append("    try {\n");
//    code.append("      String param = getParameter (\"init\");\n");
//    code.append("      if (param!=null) {\n");
//    code.append("         (("+_name+")_model).__initMethod = new org.colos.ejs.library.control.MethodWithOneParameter (0,_model,param,null,null,this);\n");
//    code.append("         (("+_name+")_model).__initMethod.invoke(0,this);\n");
//    code.append("      }\n");
//    code.append("    }\n");
//    code.append("    catch (Exception e) { e.printStackTrace (); }\n");
    //code.append("    _simulation.setParentComponent(mainComponent);\n");
    code.append("    _simulation.initEmersion();\n");
    code.append("  }\n");
//    code.append("  public java.awt.Component _getMainComponent() { return mainComponent; }\n");
//    code.append("  public void _setMainComponent(java.awt.Component _comp) { mainComponent = _comp; }\n");
    code.append("  public void _reset() { (("+_classname+")_model)._reset(); }\n");
    code.append("  public void _initialize() { (("+_classname+")_model)._initialize(); }\n");
    code.append("  public void stop() { (("+_classname+")_model)._onExit(); }\n");
    code.append("} // End of class " + _classname + "Applet\n\n");
    return code.toString();
  }

  //CJB for collaborative
  /**
   * Generates the Java code for the applet student collaborative
   * @param _filename String The name of the simulation file
   * @param _mainFrame String The name of the main frame in Ejs' view
   * @return String
   */
  static private String generateAppletStudent(Osejs _ejs, String _classname, String _packageName, String _parentPath, String _mainFrame) {
	    StringBuffer code = new StringBuffer();
	    code.append(generateHeader(_ejs, _classname, _packageName, "AppletStudent"));
	    code.append("public class " + _classname + "AppletStudent extends org.colos.ejs.library.collaborative.LauncherAppletCollaborative {\n\n");
	    code.append("  static {\n");
	    
	    code.append("    org.opensourcephysics.display.OSPRuntime.loadTranslatorTool = false;\n");
	    if (!_ejs.getSimInfoEditor().addCaptureTools())   code.append("    org.opensourcephysics.display.OSPRuntime.loadVideoTool = false;\n");
	    if (!_ejs.getSimInfoEditor().addToolsForData())   {
	      code.append("    org.opensourcephysics.display.OSPRuntime.loadDataTool = false;\n");
	      code.append("    org.opensourcephysics.display.OSPRuntime.loadFourierTool = false;\n");
	    }
	    code.append("    org.opensourcephysics.display.OSPRuntime.loadExportTool = false;\n");
	    code.append("  }\n\n");
	    
	    code.append("  public void init () {\n");
	    code.append("    super.init();\n");
	    code.append("    org.opensourcephysics.tools.ResourceLoader.addAppletSearchPath(\"/"+_parentPath +"\");\n");
	    code.append("    org.opensourcephysics.tools.ResourceLoader.addSearchPath(getCodeBase()+\""+_parentPath+"\"); // This is for relative files\n");
	    code.append("    org.opensourcephysics.tools.ResourceLoader.addSearchPath(\""+_parentPath+"\"); // This is for relative files, too\n");
	    addHtmlPagesMapCode (_ejs,_classname, code);
	    addCheckPasswordCode (_ejs,_classname,"",code);
	    
	    code.append("    if (getParentFrame()!=null) {\n");
	    code.append("      _model = new "+_classname+" ("+_mainFrame+",getParentFrame(),getCodeBase(),this,(String[])null, true);\n");
	    code.append("      _simulation = _model.getSimulationCollaborative();\n");
	    code.append("      _view = _model.getView();\n");
	    code.append("    }\n");
	    code.append("    else {\n");
	    code.append("      _model = new "+_classname+" (null,null,getCodeBase(),this,(String[])null, true);\n");
	    code.append("      _simulation = _model.getSimulationCollaborative();\n");
	    code.append("      _view = _model.getView();\n");
	    code.append("    }\n");
	    code.append("    _simulation.initEmersion();\n");
	    code.append("    try {\n");
	    code.append("      String IP_Teacher = getParameter (\"IP_Teacher\");\n");
	    code.append("      String Port_Teacher = getParameter (\"Port_Teacher\");\n");
	    code.append("      String Package_Teacher = getParameter (\"Package_Teacher\");\n");
	    code.append("      String MainFrame_Teacher = getParameter (\"MainFrame_Teacher\");\n");
	    code.append("      String[] _argsCol = {IP_Teacher, Port_Teacher, Package_Teacher, MainFrame_Teacher};\n");
	    //code.append("      _simulation.setParentComponent(mainComponent);\n");
	    //code.append("      _simulation.init();\n");
	    code.append("      _simulation.setIsThereTeacher(true);\n");
	    code.append("      _simulation.setStudentSim(true);\n");
	    code.append("      _simulation.setArgsCol(_argsCol);\n");
	    code.append("    }\n");
	    code.append("    catch (Exception e) { e.printStackTrace (); }\n");
	    code.append("  }\n");
	    code.append("  public void _reset() { (("+_classname+")_model)._reset(); }\n");
	    code.append("  public void _initialize() { (("+_classname+")_model)._initialize(); }\n");
	    code.append("  public void stop() { (("+_classname+")_model)._onExit(); }\n");
	    code.append("} // End of class " + _classname + "AppletStudent\n\n");
	    return code.toString();
  }
  //CJB for collaborative
  
  
  /**
   * Generates the Java code for the model of the simulation
   */
  static private String generateModel (Osejs _ejs, String _classname, String _packageName, 
                                       String _sourceXML, String _parentPath, 
                                       File _generatedDirectory, Collection<PathAndFile> _resList, String _mainFrame) {

    StringBuffer code = new StringBuffer();
    code.append(generateHeader(_ejs,_classname,_packageName,"")); 

    TabbedEvolutionEditor evolutionEditor = _ejs.getModelEditor().getEvolutionEditor();
    
    Editor initializationEditor = _ejs.getModelEditor().getInitializationEditor();
    Editor constraintsEditor = _ejs.getModelEditor().getConstraintsEditor();
    
    code.append("import javax.swing.event.*;\n");
    code.append("import javax.swing.*;\n");
    code.append("import java.awt.event.*;\n");
    code.append("import java.awt.*;\n");
    code.append("import java.net.*;\n");
    code.append("import java.util.*;\n");
    code.append("import java.io.*;\n");
    code.append("import java.lang.*;\n\n");
    //for (String anImport : _ejs.getSimInfoEditor().getImportsList()) code.append("import "+anImport+"\n");
    //code.append("@SuppressWarnings(\"unchecked\")\n");
    
    //CJB for collaborative
    if(_ejs.getSimInfoEditor().addAppletColSupport())
    	 code.append("public class " + _classname + " extends org.colos.ejs.library.collaborative.AbstractModelCollaborative {\n\n");
    else
    	 code.append("public class " + _classname + " extends org.colos.ejs.library.Model {\n\n");
    //CJB for collaborative
    
   
    code.append("  static {\n");
    if (_ejs.getSimInfoEditor().addToolsForData()) {
      code.append("    org.opensourcephysics.tools.ToolForData.setTool(new org.opensourcephysics.tools.ToolForDataFull());\n");
    }
    if (_ejs.getSimInfoEditor().addTranslatorTool()) {
      code.append("    __translatorUtil = new org.colos.ejs.library.utils.TranslatorResourceUtil(\""+_packageName+"."+_classname+"\");\n");
      for (LocaleItem item : _ejs.getTranslationEditor().getDesiredTranslations()) 
        if (!item.isDefaultItem()) code.append("    __translatorUtil.addTranslation(\""+item.getKeyword()+"\");\n");
    }
    else {
      code.append("    __translatorUtil = new org.colos.ejs.library.utils.TranslatorUtil();\n");
      for (TwoStrings ts : _ejs.getTranslationEditor().getResourceDefaultPairs()) 
        code.append ("    __translatorUtil.addString(\""+ts.getFirstString()+"\",\""+ts.getSecondString()+"\");\n");
    }
    code.append("  }\n\n");

//    code.append("  static {\n");
//    if (_ejs.getOptions().addPrintTool()) code.append("    printUtil = new org.colos.ejs.library.utils.PrintUtilClass();\n");
//    else                                  code.append("    printUtil = new org.colos.ejs.library.utils.PrintUtil();\n");
//    code.append("  }\n\n");

    code.append("  public " + _classname + "Simulation _simulation=null;\n");
    code.append("  public " + _classname + "View _view=null;\n");
    code.append("  public " + _classname + " _model=this;\n\n");
  
    code.append("  // -------------------------- \n");
    code.append("  // Information on HTML pages\n");
    code.append("  // -------------------------- \n\n");

    code.append("  static private java.util.Map<String,java.util.Set<org.colos.ejs.library.utils.HtmlPageInfo>> __htmlPagesMap =\n" +
        "    new java.util.HashMap<String,java.util.Set<org.colos.ejs.library.utils.HtmlPageInfo>>();\n\n");

    code.append("  /**\n");
    code.append("   * Adds info about an html on the model\n");
    code.append("   */\n");
    code.append("  static public void _addHtmlPageInfo(String _pageName, String _localeStr, String _title, String _link) {\n");
    code.append("    java.util.Set<org.colos.ejs.library.utils.HtmlPageInfo> pages = __htmlPagesMap.get(_pageName);\n");
    code.append("    if (pages==null) {\n");
    code.append("      pages = new java.util.HashSet<org.colos.ejs.library.utils.HtmlPageInfo>();\n");
    code.append("      __htmlPagesMap.put(_pageName, pages);\n");
    code.append("    }\n");
    code.append("    org.colos.ejs.library.utils.LocaleItem item = org.colos.ejs.library.utils.LocaleItem.getLocaleItem(_localeStr);\n");
    code.append("    if (item!=null) pages.add(new org.colos.ejs.library.utils.HtmlPageInfo(item, _title, _link));\n");
    code.append("  }\n\n");
    
    code.append("  /**\n");
    code.append("   * Returns info about an html on the model\n");
    code.append("   */\n");
    code.append("  static public org.colos.ejs.library.utils.HtmlPageInfo _getHtmlPageClassInfo(String _pageName, org.colos.ejs.library.utils.LocaleItem _item) {\n");
    code.append("    java.util.Set<org.colos.ejs.library.utils.HtmlPageInfo> pages = __htmlPagesMap.get(_pageName);\n");
    code.append("    if (pages==null) return null;\n");
    code.append("    org.colos.ejs.library.utils.HtmlPageInfo defaultInfo=null;\n");
    code.append("    for (org.colos.ejs.library.utils.HtmlPageInfo info : pages) {\n");
    code.append("      if (info.getLocaleItem().isDefaultItem()) defaultInfo = info;\n");
    code.append("      if (info.getLocaleItem().equals(_item)) return info;\n");
    code.append("    }\n");
    code.append("    return defaultInfo;\n");
    code.append("  }\n\n");

    code.append("  public org.colos.ejs.library.utils.HtmlPageInfo _getHtmlPageInfo(String _pageName, org.colos.ejs.library.utils.LocaleItem _item) { return _getHtmlPageClassInfo(_pageName,_item); }\n\n");

    code.append("  // -------------------------- \n");
    code.append("  // static methods \n");
    code.append("  // -------------------------- \n\n");

    if (_ejs.getOptions().includeModel()) {
      if (_sourceXML!=null) {
        if (!_sourceXML.startsWith("/")) _sourceXML = "/"+_sourceXML; // Introduced because of Wolfgang's changes to ResourceLoader (Nov 2009)
//        if (_sourceXML.indexOf('/')<0) _sourceXML = "./"+_sourceXML; // This is now unnecesary
        code.append("  static public String _getEjsModel() { return \""+_sourceXML+"\"; }\n\n");
      }
      code.append("  static public String _getModelDirectory() { return \""+_parentPath+"\"; }\n\n");

      Dimension dim = _ejs.getViewEditor().getTree().getMainWindowDimension();
      if (dim!=null) {
        code.append("  static public java.awt.Dimension _getEjsAppletDimension() {\n");
        code.append("    return new java.awt.Dimension(" + dim.width + ","+ dim.height + ");\n");
        code.append("  }\n\n");
      }
      if (_resList!=null) {
        code.append("  static public java.util.Set<String> _getEjsResources() {\n");
        code.append("    java.util.Set<String> list = new java.util.HashSet<String>();\n");
        for (PathAndFile paf : _resList) {
          if (paf.getFile().isDirectory()) {
            String dirPath = paf.getPath();
            if (!dirPath.startsWith("/")) dirPath = "/"+dirPath; // Introduced because of Wolfgang's changes to ResourceLoader (Nov 2009)
            if (!dirPath.endsWith("/")) dirPath += "/";
            for (File file : JarTool.getContents(paf.getFile()))  {
              String relPath = dirPath + FileUtils.getRelativePath(file,paf.getFile(),false);
              code.append("    list.add(\"" + relPath + "\");\n");
            }
          }
          else {
            String path = paf.getPath();
            if (!path.startsWith("/")) path = "/"+path; // Introduced because of Wolfgang's changes to ResourceLoader (Nov 2009)
            code.append("    list.add(\"" + path + "\");\n");
          }
        }
        code.append("    return list;\n");
        code.append("  };\n\n");
      }
    }
    
    String libraryConfigPath = FileUtils.getAbsolutePath(OsejsCommon.CONFIG_DIR_PATH, _ejs.getBinDirectory());

    code.append("  static public boolean _common_initialization(String[] _args) {\n");
    code.append("    String lookAndFeel = null;\n"); //+_ejs.getLookAndFeel()+"\";\n");
    code.append("    boolean decorated = true;\n"); //+OSPRuntime.isDefaultLookAndFeelDecorated()+";\n");
    code.append("    if (_args!=null) for (int i=0; i<_args.length; i++) {\n");
    code.append("      if      (_args[i].equals(\"-_lookAndFeel\"))          lookAndFeel = _args[++i];\n");
    code.append("      else if (_args[i].equals(\"-_decorateWindows\"))      decorated = true;\n");
    code.append("      else if (_args[i].equals(\"-_doNotDecorateWindows\")) decorated = false;\n");
    code.append("    }\n");
    code.append("    if (lookAndFeel!=null) org.opensourcephysics.display.OSPRuntime.setLookAndFeel(decorated,lookAndFeel);\n");
    String searchPath = _parentPath.length()>0 ? _parentPath : ".";
    code.append("    org.opensourcephysics.tools.ResourceLoader.addSearchPath(\""+searchPath+"\"); // This is for relative resources\n");
//    code.append("    org.opensourcephysics.tools.ResourceLoader.addSearchPath(\""+_outputDirectoryPath+"\"); // This is for absolute resources in Launcher packages\n");
//    code.append("    org.opensourcephysics.tools.ResourceLoader.addSearchPath(\""+_outputDirectoryPath+"/"+searchPath+"\"); // This is for relative resources in Launcher packages\n");

    code.append("    boolean pathsSet = false, underEjs = false;\n");
    code.append("    try { // in case of security problems\n");
    //code.append("      System.setProperty(\"osp_defaultLookAndFeel\",\""+OSPRuntime.isDefaultLookAndFeelDecorated()+"\");\n");
    //code.append("      System.setProperty(\"osp_lookAndFeel\",\""+_ejs.getLookAndFeel()+"\");\n");
    code.append("      if (System.getProperty(\"osp_ejs\")!=null) { // Running under EJS\n"); // TODO Can I remove this check
    code.append("        underEjs = true;\n");
    code.append("        org.colos.ejs.library.Simulation.setPathToLibrary(\""+libraryConfigPath+"\"); // This is for classes (such as EjsMatlab) which needs to know where the library is\n");
    code.append("        pathsSet = true;\n");
    code.append("      }\n");
    code.append("    }\n");
    code.append("    catch (Exception _exception) { pathsSet = false; } // maybe an unsigned Web start?\n");
    code.append("    try { org.colos.ejs.library.control.EjsControl.setDefaultScreen(Integer.parseInt(System.getProperty(\"screen\"))); } // set default screen \n");
    code.append("    catch (Exception _exception) { } // Ignore any error here\n");
    code.append("    if (!pathsSet) {\n");
    code.append("      org.colos.ejs.library.Simulation.setPathToLibrary(\""+libraryConfigPath+"\"); // This is for classes (such as EjsMatlab) which needs to know where the library is\n");
    code.append("    }\n");
    addHtmlPagesMapCode (_ejs,_classname,code);
    code.append("    if (!underEjs) {\n");
    addCheckPasswordCode(_ejs, _classname, "false", code);
//      code.append("      if (org.colos.ejs.library.utils.PasswordDialog.checkPassword(\""+codedPassword+"\",null)==null) return false;\n");
    code.append("    }\n");

    code.append("    return true; // Everything went ok\n");
    code.append("  }\n\n");

    code.append("  static public void main (String[] _args) {\n");
    code.append("    if (!_common_initialization(_args)) {\n");
    code.append("      if (org.opensourcephysics.display.OSPRuntime.isLauncherMode()) return;\n");
    code.append("      System.exit(-1);\n");
    code.append("    }\n\n");
    code.append("    "+_classname+ " __theModel = new " + _classname + " (_args);\n");
    code.append("  }\n\n");
    
    code.append("  static public javax.swing.JComponent getModelPane(String[] _args, javax.swing.JFrame _parentFrame) {\n");
    code.append("    if (!_common_initialization(_args)) return null;\n");
    code.append("    "+_classname+ " __theModel = new " +_classname+" ("+_mainFrame+",_parentFrame,null,null,_args,true);\n");
    code.append("    return (javax.swing.JComponent) __theModel._getView().getComponent("+_mainFrame+");\n");
    code.append("  }\n\n");

    code.append("  public " + _classname + " () { this (null, null, null,null,null,false); } // slave application\n\n");
    code.append("  public " + _classname + " (String[] _args) { this (null, null, null,null,_args,true); }\n\n");
    code.append("  public " + _classname + " (String _replaceName, java.awt.Frame _replaceOwnerFrame, java.net.URL _codebase,"
                                    +  " org.colos.ejs.library.LauncherApplet _anApplet, String[] _args, boolean _allowAutoplay) {\n");
    code.append("    __theArguments = _args;\n");
    code.append("    __theApplet = _anApplet;\n");
    code.append("    java.text.NumberFormat _Ejs_format = java.text.NumberFormat.getInstance();\n");
    code.append("    if (_Ejs_format instanceof java.text.DecimalFormat) {\n");
    code.append("      ((java.text.DecimalFormat) _Ejs_format).getDecimalFormatSymbols().setDecimalSeparator('.');\n");
    code.append("    }\n");
    code.append("    _simulation = new " + _classname + "Simulation (this,_replaceName,_replaceOwnerFrame,_codebase,_allowAutoplay);\n");
    code.append("    _view = (" + _classname + "View) _simulation.getView();\n");
    code.append("    _simulation.processArguments(_args);\n");
    code.append("  }\n\n");

    code.append(" // -------------------------------------------\n");
    code.append(" // Abstract part of Model \n");
    code.append(" // -------------------------------------------\n\n");

    code.append("  public java.util.Set<String> _getClassEjsResources() { return _getEjsResources(); }\n\n");
    code.append("  public String _getClassEjsModel() { return _getEjsModel(); }\n\n");
    code.append("  public String _getClassModelDirectory() { return _getModelDirectory(); }\n\n");

    code.append("  public org.colos.ejs.library.View _getView() { return _view; }\n\n");
    code.append("  public org.colos.ejs.library.Simulation _getSimulation() { return _simulation; }\n\n");
    
    //CJB for collaborative
    if(_ejs.getSimInfoEditor().addAppletColSupport())
    	code.append("  public org.colos.ejs.library.collaborative.SimulationCollaborative getSimulationCollaborative() { return _simulation; }\n\n");
    //CJB for collaborative
    
    code.append("  public int _getPreferredStepsPerDisplay() { return "+_ejs.getModelEditor().getStepsPerDisplay()+"; }\n\n");

    code.append("  public void _resetModel () {\n");
    code.append(     initializationEditor.generateCode(Editor.GENERATE_RESET_ENABLED_CONDITION,""));
    code.append(     evolutionEditor.generateCode(Editor.GENERATE_RESET_ENABLED_CONDITION,""));
    code.append(     constraintsEditor.generateCode(Editor.GENERATE_RESET_ENABLED_CONDITION,""));

    code.append(    _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_CODE,""));
    code.append(    _ejs.getModelEditor().getElementsEditor().generateCode(Editor.GENERATE_CODE,""));
    code.append(     evolutionEditor.generateCode(Editor.GENERATE_SOURCECODE,""));
//    code.append("    System.gc(); // Free memory from unused old arrays\n"
    code.append("  }\n\n");
    if (evolutionEditor.hasODEpages()) {
      code.append("  public void _initializeSolvers () { for (org.opensourcephysics.numerics.EJSODE __pode : _privateOdesList.values()) __pode.initializeSolver(); }\n\n");
    }
    else code.append("  public void _initializeSolvers () { } // Do nothing \n\n");
    code.append("  public void _initializeModel () {\n");
//    code.append("    \n");
//    code.append(     _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_EXTERNAL_INITIALIZATION_DECLARATION,""));
    code.append(     initializationEditor.generateCode(Editor.GENERATE_DECLARATION,""));
    code.append("    _initializeSolvers();\n");
//    if (evolutionEditor.hasODEpages()) code.append("    for (org.opensourcephysics.numerics.EJSODE __pode : _privateOdesList.values()) __pode.initializeSolver();\n"); 
//    code.append("    _resetSolvers();\n"); // 051112
    code.append("  }\n\n");
    code.append("  public void _resetSolvers() { \n");
    code.append(     evolutionEditor.generateCode(Editor.GENERATE_RESET_SOLVER,""));
    code.append("    _external.resetIC();\n");
    code.append("  }\n");
    code.append("  public void _stepModel () {\n");
    code.append(     evolutionEditor.generateCode(Editor.GENERATE_DECLARATION,""));
    code.append("  }\n\n");
    code.append("  public void _updateModel () {\n");
    code.append(    constraintsEditor.generateCode(Editor.GENERATE_DECLARATION,""));
    if (_ejs.getOptions().experimentsEnabled() && _ejs.getExperimentEditor().getActivePageCount()>0) {
      code.append("    java.util.List<_ScheduledConditionClass> _toExecute = new java.util.ArrayList<_ScheduledConditionClass>();\n");
      code.append("    for (java.util.Iterator<?> it=_scheduledConditionsList.iterator(); it.hasNext();) {\n");
      code.append("      _ScheduledConditionClass _scheduledCondition = (_ScheduledConditionClass) it.next();\n");
      code.append("      if (_scheduledCondition.condition()) _toExecute.add(_scheduledCondition);\n");
      code.append("    }\n");
      code.append("    for (java.util.Iterator<_ScheduledConditionClass> it=_toExecute.iterator(); it.hasNext();) {\n");
      code.append("      _ScheduledConditionClass _scheduledCondition = it.next();\n");
      code.append("      _scheduledConditionsList.remove(_scheduledCondition);\n");
      code.append("      _scheduledCondition.action();\n");
      code.append("    }\n");
    }
    code.append("  }\n\n");

    code.append("  public void _freeMemory () {\n");
    code.append(     _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_DESTRUCTION,""));
    code.append(    _ejs.getModelEditor().getElementsEditor().generateCode(Editor.GENERATE_DESTRUCTION,""));
    code.append(     evolutionEditor.generateCode(Editor.GENERATE_DESTRUCTION,""));
    code.append("    System.gc(); // Free memory from unused old arrays\n");
    code.append("  }\n\n");


    if (evolutionEditor.hasODEpages()){
      code.append(" // -------------------------------------------\n");
      code.append(" // ODEs declaration \n");
      code.append(" // -------------------------------------------\n\n");
      code.append("  protected java.util.Hashtable<String,org.opensourcephysics.numerics.EJSODE> _privateOdesList = new java.util.Hashtable<String,org.opensourcephysics.numerics.EJSODE>();\n\n");
      code.append("  public org.opensourcephysics.numerics.EJSODE _getODE(String _odeName) {\n");
      code.append("    try { return _privateOdesList.get(_odeName); }\n");
      code.append("    catch (Exception __exc) { return null; }\n");
      code.append("  }\n\n");
      code.append("  public org.opensourcephysics.numerics.ODEInterpolatorEventSolver _getEventSolver(String _odeName) {\n");
      code.append("    try { return _privateOdesList.get(_odeName).getEventSolver(); }\n");
      code.append("    catch (Exception __exc) { return null; }\n");
      code.append("  }\n\n");
      code.append("  public void _setSolverClass (String _odeName, Class<?> _solverClass) { // Change the solver in run-time\n");
      code.append("    try { _privateOdesList.get(_odeName).setSolverClass(_solverClass); }\n");
      code.append("    catch (Exception __exc) { System.err.println (\"There is no ODE with this name \"+_odeName); }\n");
      code.append("  }\n\n");
    }

    code.append(" // -------------------------------------------\n");
    code.append(" // Implementation of ExternalClient \n");
    code.append(" // -------------------------------------------\n\n");
    
    code.append("  public String _externalInitCommand(String _applicationFile) { return null;}\n");
    code.append("  public void _externalSetValues(boolean _any, org.colos.ejs.library.external.ExternalApp _application) {} \n");
    code.append("  public void _externalGetValues(boolean _any, org.colos.ejs.library.external.ExternalApp _application) {} \n");
    
    /*
    code.append("  public String _externalInitCommand(String _applicationFile) { \n");
    code.append("    StringBuffer _external_initCommand=new StringBuffer();\n");
    code.append(     _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_EXTERNAL_INITIALIZATION,""));
    code.append("    return _external_initCommand.toString();\n");
    code.append("  }\n\n");
    
    code.append("  public void _externalSetValues(boolean _any, org.colos.ejs.library.external.ExternalApp _application) { \n");
    code.append(     _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_EXTERNAL_IN,"")); // Includes conversion
    code.append("  }\n\n");
    code.append("  public void _externalGetValues(boolean _any, org.colos.ejs.library.external.ExternalApp _application) { \n");
    code.append(    _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_EXTERNAL_OUT,""));
    code.append("  }\n\n");
    
    */
    
    code.append(" // -------------------------------------------\n");
    code.append(" // Variables defined by the user\n");
    code.append(" // -------------------------------------------\n\n");
    
    code.append("  private org.colos.ejs.library.external.ExternalAppsHandler _external"); //Gonzalo 090610  
    code.append(" = new org.colos.ejs.library.external.ExternalAppsHandler(this); // List of possible external applications\n\n");
    
    code.append(  _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_DECLARATION,""));
    code.append(  _ejs.getModelEditor().getElementsEditor().generateCode(Editor.GENERATE_DECLARATION,""));
    
    code.append("\n");

    code.append(" // -------------------------------------------\n");
    code.append(" // Enabled condition of pages \n");
    code.append(" // -------------------------------------------\n\n");
    
    code.append( initializationEditor.generateCode(Editor.GENERATE_ENABLED_CONDITION,""));
    code.append( evolutionEditor.generateCode(Editor.GENERATE_ENABLED_CONDITION,""));
    code.append( constraintsEditor.generateCode(Editor.GENERATE_ENABLED_CONDITION,""));
    code.append("\n");

    code.append("  public void _setPageEnabled(String _pageName, boolean _enabled) { // Sets the enabled state of a page\n");
    code.append("    boolean _pageFound = false;\n");
    code.append( initializationEditor.generateCode(Editor.GENERATE_CHANGE_ENABLED_CONDITION,""));
    code.append( evolutionEditor.generateCode(Editor.GENERATE_CHANGE_ENABLED_CONDITION,""));
    code.append( constraintsEditor.generateCode(Editor.GENERATE_CHANGE_ENABLED_CONDITION,""));
    code.append("    if (!_pageFound) System.out.println (\"_setPageEnabled() warning. Page not found: \"+_pageName);\n");
    code.append("  }\n\n");
    
    code.append(" // -------------------------------------------\n");
    code.append(" // Methods defined by the user \n");
    code.append(" // -------------------------------------------\n\n");

    code.append(" // --- Initialization\n\n");    
//    code.append( _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_EXTERNAL_INITIALIZATION,"")); //Gonzalo 090610
    
    code.append(  initializationEditor.generateCode(Editor.GENERATE_CODE,""));
    
    code.append(" // --- Evolution\n\n");
    code.append(  evolutionEditor.generateCode(Editor.GENERATE_CODE,""));
    code.append(" // --- Constraints\n\n");
    code.append(  constraintsEditor.generateCode(Editor.GENERATE_CODE,""));
    code.append(" // --- Custom\n\n");
    code.append(  _ejs.getModelEditor().getLibraryEditor().generateCode(Editor.GENERATE_CODE,""));
    code.append(" // --- Methods for view elements\n\n");
    code.append(  _ejs.getViewEditor().generateCode(Editor.GENERATE_VIEW_EXPRESSIONS,""));

    if (_ejs.getOptions().experimentsEnabled()) {
      code.append("\n  // -----------------------------\n");
      code.append("  //     Code for Experiments     \n");
      code.append("  // -----------------------------\n\n");
      code.append(_ejs.getExperimentEditor().generateCode(Editor.GENERATE_CODE,""));

      code.append("  public java.util.List<Experiment> _getExperiments () { // Creates a list of experiments\n");
      code.append("    java.util.ArrayList actions = new java.util.ArrayList();\n");
      code.append(_ejs.getExperimentEditor().generateCode(Editor.GENERATE_LIST_ACTIONS,_classname));
      code.append("    return actions;\n");
      code.append("  }\n\n");
      code.append("  public org.colos.ejs.library.Experiment _createExperiment (String _experimentName) { // gets an experiment by name\n");
      code.append(_ejs.getExperimentEditor().generateCode(Editor.GENERATE_LIST_VARIABLES,_classname));
      code.append("    return null;");
      code.append("  }\n\n");
      code.append("  // ------------------------------------\n");
      code.append("  //     End of Code for Experiments     \n");
      code.append("  // ------------------------------------\n\n");
    }

    code.append("} // End of class " + _classname + "Model\n\n");
    return code.toString();
  }

  /**
   * Adds the map of html pages for different locales
   */
  static private void addHtmlPagesMapCode(Osejs _ejs, String _classname, StringBuffer _buffer) {
    String prefix = "    "+_classname+"._addHtmlPageInfo";

    Set<LocaleItem> localesDesired = getLocalesDesired(_ejs);
    int counter = 0;
    for (java.util.Enumeration<Editor> e = _ejs.getDescriptionEditor().getPages().elements(); e.hasMoreElements(); ) {
      HtmlEditor htmlEditor = (HtmlEditor) e.nextElement();
      if (!htmlEditor.isActive()) continue;
      counter++;
      for (LocaleItem item : localesDesired) {
        OneHtmlPage htmlPage = htmlEditor.getHtmlPage(item);
        if (htmlPage==null || htmlPage.isEmpty()) continue;
        String link;
        if (htmlPage.isExternal()) {
          link = htmlPage.getLink();
          if (!link.startsWith("/")) link = "/"+link;
        }
        else  link = "./"+_classname+"_Intro "+counter+ (item.isDefaultItem() ? "" : "_"+item.getKeyword()) + ".html";
        java.awt.Dimension size = htmlEditor.getComponent().getSize();
        if (size.width<=0 || size.height<=0) size = _ejs.getOptions().getHtmlPanelSize(); // This happens in batch compilation
        _buffer.append(prefix+"(\""+htmlEditor.getName()+"\",\""+item.getKeyword()+"\",\""+htmlPage.getTitle()+"\",\""+link+"\");\n");
      } // end of for
    }
  }

  /**
   * Generates the Java code for the simulation part itself
   * @param _ejs Osejs The calling Ejs
   * @param _filename String The name of the simulation file
   * @return StringBuffer
   */
  static private String generateSimulation (Osejs _ejs, String _classname, String _packageName, String _xmlName, String _mainFrame) {
    StringBuffer code = new StringBuffer ();
    code.append(generateHeader(_ejs,_classname,_packageName,"Simulation"));

    //CJB for collaborative
    if(_ejs.getSimInfoEditor().addAppletColSupport())
    	code.append("class " + _classname + "Simulation extends org.colos.ejs.library.collaborative.SimulationCollaborative { \n\n");
    else
    	code.append("class " + _classname + "Simulation extends org.colos.ejs.library.Simulation { \n\n");
    //CJB for collaborative
    
    code.append("  public " + _classname + "Simulation (" + _classname + " _model, String _replaceName, java.awt.Frame _replaceOwnerFrame, java.net.URL _codebase, boolean _allowAutoplay) {\n");
    if (_ejs.getSimInfoEditor().addCaptureTools()) code.append("    videoUtil = new org.colos.ejs.library.utils.VideoUtilClass();\n");
    else code.append("    videoUtil = new org.colos.ejs.library.utils.VideoUtil();\n");

    code.append("    try { setUnderEjs(\"true\".equals(System.getProperty(\"osp_ejs\"))); }\n");      
    code.append("    catch (Exception exc) { setUnderEjs(false); } // in case of applet security\n");      
    code.append("    setCodebase (_codebase);\n");
    code.append("    setModel (_model);\n");
    code.append("    _model._simulation = this;\n");
    code.append("    setView (_model._view = new "+_classname+"View(this,_replaceName, _replaceOwnerFrame));\n");
    code.append("    if (_model._isApplet()) _model._getApplet().captureWindow (_model,"+_mainFrame+");\n");
    code.append(     _ejs.getModelEditor().generateCode(Editor.GENERATE_SIMULATION_STATE,""));
//    int htmlOption = _ejs.getOptions().generateHtml();
//    if (htmlOption==org.colos.ejs.osejs.EjsOptions.GENERATE_TOP_FRAME  || htmlOption==org.colos.ejs.osejs.EjsOptions.GENERATE_LEFT_FRAME) 
    {
      int counter = 0;
      java.awt.Dimension size = _ejs.getDescriptionEditor().getEditorSize();
      if (size.width<=0 || size.height<=0) size = _ejs.getOptions().getHtmlPanelSize(); // This happens in batch compilation
      for (Editor htmlEditor : _ejs.getDescriptionEditor().getPages()) {
        if (htmlEditor.isActive()) {
          counter++;
//          java.awt.Dimension size = ((HtmlEditor) htmlEditor).getTextComponent().getSize();
          code.append("    addDescriptionPage(\""+htmlEditor.getName()+"\","+size.width+","+size.height+");\n");
        }
      }
    }
    code.append("    setLocaleItem(getLocaleItem(),false); // false so that not to reset the model twice at start-up\n");
    //CJB for collaborative
    if(_ejs.getSimInfoEditor().addAppletColSupport())
    	code.append("    setParamDialog (\"http://\",\"50000\");\n");
    //CJB for collaborative
    code.append("  }\n\n");

    if (!_ejs.getModelEditor().checkSPD()) {
      code.append("  public void step() {\n");
      code.append( "    setStepsPerDisplay(model._getPreferredStepsPerDisplay());\n");
      code.append( "    super.step();\n");
      code.append("  }\n\n");
    }

    /*
    code.append("  public void rebuildView() {\n");
    code.append("    "+_classname+" theModel = ("+_classname+") getModel();\n");
    code.append("    setView (theModel._view = new "+_classname+"View(this,theModel._view.getReplaceOwnerName(), theModel._view.getReplaceOwnerFrame()));\n");
    code.append("  }\n\n");
    */
    
    code.append("  public java.util.List<String> getWindowsList() {\n");
    code.append( "    java.util.List<String> windowList = new java.util.ArrayList<String>();\n");
    code.append(     _ejs.getViewEditor().generateCode(Editor.GENERATE_WINDOW_LIST,""));
    code.append( "    return windowList;\n");
    code.append("  }\n\n");
    code.append("  public String getMainWindow() {\n");
    code.append( "    return "+_mainFrame+";\n");
    code.append("  }\n\n");
    code.append("  protected void setViewLocale() { // Overwrite its parent's dummy method with real actions \n");
    code.append(     _ejs.getViewEditor().generateCode(Editor.GENERATE_CHANGE_LOCALE,""));
    code.append("    super.setViewLocale();\n");
    code.append("  }\n\n");
    if (_ejs.getOptions().experimentsEnabled() && _ejs.getExperimentEditor().getActivePageCount()>0) {
      code.append("  public java.util.List<Experiment> getExperiments () { // Add the experiments to the menu\n");
      code.append("    return (("+_classname+") getModel())._getExperiments();\n");
      code.append("  }\n\n");
      code.append("  public org.colos.ejs.library.Experiment createExperiment (String _experimentName) { // creates an experiment\n");
      code.append("    return (("+_classname+") getModel())._createExperiment(_experimentName);\n");
      code.append("  }\n\n");
    }
    if (_ejs.getSimInfoEditor().addEmersionSupport()) {
      code.append("  public org.colos.ejs.library.LauncherApplet initEmersion () {\n");
      code.append("    org.colos.ejs.library.LauncherApplet applet = super.initEmersion();\n");
      code.append("    if (applet!=null && applet.getParameter(\"eMersionURL\")!=null) eMersion = new org.colos.ejs.library.EmersionConnection (applet,this);\n");
      code.append("    return applet;\n");
      code.append("  }\n\n");
    }
    
    code.append("} // End of class "+_classname+"Simulation\n\n");
    return code.toString();
  }

  /**
   * Generates the Java code for the view of the simulation
   * @param _ejs Osejs The calling Ejs
   * @param _filename String The name of the simulation file
   * @return StringBuffer
   */
  static private String generateView (Osejs _ejs, String _classname, String _packageName) {
    StringBuffer code = new StringBuffer();
    code.append(generateHeader(_ejs,_classname,_packageName, "View"));
    code.append("import javax.swing.event.*;\n");
    code.append("import javax.swing.*;\n");
    code.append("import java.awt.event.*;\n");
    code.append("import java.awt.*;\n");
    code.append("import java.net.*;\n");
    code.append("import java.util.*;\n");
    code.append("import java.io.*;\n");
    code.append("import java.lang.*;\n\n");

    code.append("class " + _classname + "View extends org.colos.ejs.library.control.EjsControl implements org.colos.ejs.library.View {\n");
    code.append("  private "+_classname+"Simulation _simulation=null;\n");
    code.append("  private "+_classname+" _model=null;\n\n");
    code.append("  // Public variables for wrapped view elements:\n");
    code.append(_ejs.getViewEditor().generateCode(Editor.GENERATE_DECLARATION,""));
    code.append("\n  // private variables to block changes in the view variables:\n");
    code.append(_ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_VIEW_UPDATE_BOOLEANS,""));
    code.append("\n");

    code.append("// ---------- Class constructor -------------------\n\n");
    code.append("  public "+_classname+"View ("+_classname+"Simulation _sim, String _replaceName, java.awt.Frame _replaceOwnerFrame) {\n");
    code.append("    super(_sim,_replaceName,_replaceOwnerFrame);\n");
    code.append("    _simulation = _sim;\n");
    code.append("    _model = ("+_classname+") _sim.getModel();\n");
    code.append("    _model._view = this;\n");
    code.append("    addTarget(\"_simulation\",_simulation);\n");
    code.append("    addTarget(\"_model\",_model);\n");
    code.append("    _model._resetModel();\n");
    code.append("    initialize();\n");
    code.append("    setUpdateSimulation(false);\n");
    code.append("    // The following is used by the JNLP file for the simulation to help find resources\n");
    code.append("    try { setUserCodebase(new java.net.URL(System.getProperty(\"jnlp.codebase\"))); }\n");
    code.append("    catch (Exception exc) { } // Do nothing and keep quiet if it fails\n");
    code.append("    update();\n");
    code.append("    if (javax.swing.SwingUtilities.isEventDispatchThread()) createControl();\n");
    code.append("    else try {\n");
    code.append("      javax.swing.SwingUtilities.invokeAndWait(new Runnable() {\n");
    code.append("        public void run () { \n");
    code.append("          createControl();\n");
    code.append("        }\n");
    code.append("      });\n");
    code.append("    } catch (java.lang.reflect.InvocationTargetException it_exc) { it_exc.printStackTrace(); \n");
    code.append("    } catch (InterruptedException i_exc) { i_exc.printStackTrace(); };\n");
//    code.append("    // The following is used by the JNLP file for the simulation to help find resources\n");
//    code.append("    try { setUserCodebase(new java.net.URL(System.getProperty(\"jnlp.codebase\"))); }\n");
//    code.append("    catch (Exception exc) { } // Do nothing and keep quiet if it fails\n");
    code.append("    addElementsMenuEntries();\n");
//    code.append("    _model._resetModel();\n");
//    code.append("    initialize();\n");
//    code.append("    _model._initializeSolvers();\n"); 
    code.append("    update();\n");
    code.append("    setUpdateSimulation(true);\n");
    code.append(     _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_VIEW_LISTENERS,""));
//    code.append("    setUpdateSimulation(true);\n");
    code.append("  }\n\n");
    code.append("// ---------- Implementation of View -------------------\n\n");
    code.append("  public void read() {\n");
    code.append("    // Ejs requires no read(). Actually, having it might cause problems!\n");
    code.append("  }\n\n");
    code.append("  @SuppressWarnings(\"unchecked\")\n");
    code.append("  public void read(String _variable) {\n");
    code.append(     _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_VIEW_READ_ONE,""));
    code.append("  }\n\n");
    code.append("  public void propagateValues () {\n"); // Do NOT synchronize!!!
    code.append("    setValue (\"_isPlaying\",_simulation.isPlaying());\n");
    code.append("    setValue (\"_isPaused\", _simulation.isPaused());\n");
    code.append(     _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_VIEW_UPDATE,""));
//    code.append("    super.update();\n");
    code.append("  }\n\n");
    code.append("  @SuppressWarnings(\"unchecked\")\n");
    code.append("  public void blockVariable(String _variable) {\n");
    code.append(     _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_VIEW_BLOCK_VARIABLES,""));
    code.append("  }\n\n");
    code.append("// ---------- Creation of the interface  -------------------\n\n");
    code.append("  private void createControl() {\n");
    code.append(     _ejs.getViewEditor().generateCode(Editor.GENERATE_CODE,""));
    code.append("  }\n\n");
    code.append("// ---------- Resetting the interface  -------------------\n\n");
    code.append("  public void reset() {\n");
    code.append(     _ejs.getViewEditor().generateCode(Editor.GENERATE_VIEW_RESET,""));
    code.append(     _ejs.getModelEditor().getVariablesEditor().generateCode(Editor.GENERATE_VIEW_RESET,""));
    code.append("    super.reset();\n");
    code.append("  }\n\n");
    code.append("} // End of class " + _classname + "View\n\n");
    return code.toString();
  }

} // end of class
