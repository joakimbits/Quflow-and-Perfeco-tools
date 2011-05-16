package org.colos.ejs;

import org.colos.ejs.osejs.utils.FileUtils;
import java.io.*;
import java.util.*;
import java.util.zip.*;

import javax.swing.filechooser.FileSystemView;

/**
 * This class packages Ejs
 * @author Francisco Esquembre
 *
 */
public class CreateDistributionEjs {

  static private final String EJS_VERSION_NAME = "EJS_"+org.colos.ejs.library._EjsConstants.VERSION;
  
  public static void main(String[] args) {
    Calendar calendar = Calendar.getInstance();
    int year = calendar.get(Calendar.YEAR) - 2000;
    int month = calendar.get(Calendar.MONTH)+1;
    int day = calendar.get(Calendar.DAY_OF_MONTH);
    String targetName = EJS_VERSION_NAME+"_";
    if (year<10) targetName += "0"+year;
    else targetName += year;
    if (month<10) targetName += "0"+month;
    else targetName += month;
    if (day<10) targetName += "0"+day;
    else targetName += day;
    File distributionDir = new File ("distribution");
    File versionFile = new File(distributionDir,"bin/releaseNumber.txt");
    try {  FileUtils.saveToFile(versionFile, null, targetName); }
    catch (Exception exc) { exc.printStackTrace(); }
    targetName += ".zip";
    File targetFile = new File ("../../"+targetName);
    List<File> sources = new ArrayList<File>();
    sources.add(distributionDir);
    sources.add(new File ("../../EJS_doc"));
    sources.add(new File ("../../EJS_examples"));
    
    System.out.println ("Creating ZIP file of Ejs : "+FileUtils.getPath(targetFile)+" ...");
    compress(sources,targetFile, EJS_VERSION_NAME);
    System.out.println ("Created ZIP file of Ejs : "+FileUtils.getPath(targetFile));
    System.exit(0);
  }

  
  static private boolean compress (List<File> sources, File target, String prefix) {
//    String dirSuffix = System.getProperty("file.separator")+".";
    try {
      if (target.exists()) target.delete(); // Remove the previous JAR file
      ZipOutputStream output = new ZipOutputStream(new FileOutputStream(target));
      byte[] buffer = new byte[1024]; // Allocate a buffer for reading entry data.

      for (File sourceFile : sources) {
        if ( ! (sourceFile.exists() && sourceFile.isDirectory()) ) {
          System.out.println ("Skipping file "+sourceFile.getCanonicalPath());
          continue;
        }
        System.out.println ("Processing file "+sourceFile.getCanonicalPath());
        java.util.Collection<File> list = getContents(sourceFile);
        String baseDir = sourceFile.getCanonicalPath().replace('\\','/');
        if (!baseDir.endsWith("/")) baseDir = baseDir + "/";
//        System.out.println ("Base dir is "+baseDir);
        int baseDirLength = baseDir.length();
        // Compress this one
        int  bytesRead;
        for (File file : list) {
          // Read the entry and make it relative
          String filename = file.getCanonicalPath().replace('\\','/');
//          System.out.println ("filename is "+filename);
          if (filename.startsWith (baseDir)) filename = filename.substring(baseDirLength);
          filename = prefix+"/"+filename;
          // Write the entry to the compressed file.
          output.putNextEntry(new ZipEntry(filename));
          FileInputStream f_in = new FileInputStream (file);
          while ((bytesRead = f_in.read(buffer)) != -1) output.write(buffer, 0, bytesRead);
          f_in.close();
          output.closeEntry();
        }
      }
      output.close();
    } catch (Exception exc) { exc.printStackTrace(); return false; }
     return true;
   }
  
  static public java.util.Collection<File> getContents(File directory) {
    if(directory.exists()&&directory.isDirectory()) {
      return recursiveGetDirectory(directory, FileSystemView.getFileSystemView());
    }
    return new HashSet<File>();
  }
  
  static private java.util.Collection<File> recursiveGetDirectory(File directory, FileSystemView fsView) {
    java.util.Collection<File> list = new ArrayList<File>();
    File files[] = fsView.getFiles(directory, false);
//    boolean filesAdded = false;
    for(int i = 0; i<files.length; i++) {
      if (files[i].isDirectory()) {
        if (files[i].getName().equals(".svn")) {
          System.out.println ("Skipping "+files[i].getAbsolutePath());
        }
        else {
          list.addAll(recursiveGetDirectory(files[i], fsView));
//          filesAdded = true;
        }
      } 
      else {
        if (files[i].getName().equals(".DS_Store")) {
          System.out.println ("Skipping (and deleting) "+files[i].getAbsolutePath());
          files[i].delete();
        }
        else {
          list.add(files[i]);
//          filesAdded = true;
        }
      }
    }
//    if (!filesAdded) {
//      System.out.println ("Adding empty dir "+directory.getAbsolutePath());
//      list.add(directory);
//    }

    return list;
  }



}
