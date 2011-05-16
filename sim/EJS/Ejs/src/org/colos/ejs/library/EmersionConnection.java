/**
 * The package contains definitions for the different parts of a simulation
 * Copyright (c) July 2005 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library;

import ch.epfl.cockpit.communication.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.*;
import javax.swing.Timer;
import java.awt.event.*;
import org.colos.ejs.library.utils.SwingWorker;;

/**
 * All that is needed to communicate with eMersion
 */
public class EmersionConnection implements EmersionLink {

  private URL eMersionURL=null;
  private String eMersionSessionID = null;
  private java.awt.Component parentComponent = null;
  private Simulation simulation=null; // In case eMersion belongs to a simulation
  private SavePanel savePanel = new SavePanel();
  private ReadPanel readPanel = new ReadPanel();
  private ProgressDialog progressMonitor;
  private LongTask task;
  private Timer timer;
  private int previous;

  public EmersionConnection (javax.swing.JApplet _applet, Simulation _simulation) {
    this.simulation = _simulation;
    String urlStr = _applet.getParameter("eMersionURL");
    try {
      eMersionURL = new URL(urlStr);
      eMersionSessionID = _applet.getParameter("eMersionSessionID");
    }
    catch (MalformedURLException mue) {
      if (urlStr!=null) System.out.println ("Malformed URL exception for URL = <"+urlStr+">");
      eMersionURL = null;
      eMersionSessionID = null;
    }
  }

// ----------------------------------------
// Utilities
// ----------------------------------------

  /**
   * Whether there is a valid connection to a server.
   * @return boolean
   */
  public boolean isConnected () { return (eMersionURL!=null) && (eMersionSessionID!=null); }

  /**
   * Sets the parent component of any subsequent message window such as
   * a JOptionPane.
   * @param _component java.awt.Component
   */
  public void setParentComponent (java.awt.Component _component) { parentComponent = _component; }

  /**
   * Sets the name label for any message window
   * @param _label String
   */
  public void setNameLabel (String _label) { savePanel.nameLabel.setText (_label); }

  /**
   * Sets the annotation label for any message window
   * @param _label String
   */
  public void setAnnotationLabel (String _label) { savePanel.annotationLabel.setText (_label); }

// ----------------------------------------
// Saving
// ----------------------------------------

  private void delay(int mseconds) {
    try {
        //java.lang.Thread.sleep(mseconds);
        Thread.sleep(mseconds);    // Inserted delay
    }
    catch (InterruptedException e) {
        //do nothing!
        System.out.println("Delay interrupted!");
    }
  }

  /**
   * Saves a binary data to file.
   * It offers the user to change the provided name.
   * @param _filename String
   * @param _annotation String
   * @param _data byte[]
   * @return String The name of the file finally saved (null if aborted or errors).
   */
  public String saveBinary (String _filename, String _annotation, byte[] _data) {
    if (!isConnected()) return null;
    if (!savePanel.showSaveOptions(_filename,_annotation)) return null;
    _filename = savePanel.nameField.getText().trim();
    if (_filename.length()<=0) return null;
    _annotation = savePanel.annotationField.getText();

    task = new LongTask(_filename, _annotation, _data);
    timer = new Timer(100, new TimerListener());
    if (progressMonitor == null){
      progressMonitor = new ProgressDialog();
      progressMonitor.showProgressDialog(true);
    }
    task.go();
    timer.start();

    return _filename;
  }

  /**
   * Saves an image to file.
   * It offers the user to change the provided name.
   * @param _filename String
   * @param _annotation String
   * @param _image Image
   * @return String The name of the file finally saved (null if aborted or errors).
   */
  public String saveImage (String _filename, String _annotation, java.awt.Image _image) {
    if (!isConnected()) return null;
    // For the moment, only GIF files are supported
    if (!_filename.toLowerCase().endsWith(".gif")) _filename += ".gif";
    if (!savePanel.showSaveOptions(_filename,_annotation)) return null;
    _filename = savePanel.nameField.getText().trim();
    if (_filename.length()<=0) return null;
    _annotation = savePanel.annotationField.getText();
    // GraphicsFile always appends ".gif" to the saved fragment (!?)
    if (_filename.toLowerCase().endsWith(".gif")) _filename = _filename.substring(0,_filename.length()-4);

    task = new LongTask(_filename, _annotation, _image);
    timer = new Timer(100, new TimerListener());
    if (progressMonitor == null){
      progressMonitor = new ProgressDialog();
      progressMonitor.showProgressDialog(true);
    }
    task.go();
    timer.start();

    return _filename;
  }

  /**
   * Saves a text to file.
   * It offers the user to change the provided name.
   * @param _filename String
   * @param _annotation String
   * @param _text String
   * @return String The name of the file finally saved (null if aborted or errors).
   */
  public String saveText (String _filename, String _annotation, String _text) {
    if (!isConnected()) return null;
    if (!savePanel.showSaveOptions(_filename,_annotation)) return null;
    _filename = savePanel.nameField.getText().trim();
    if (_filename.length()<=0) return null;
    _annotation = savePanel.annotationField.getText();

    task = new LongTask(_filename, _annotation, _text, true);
    timer = new Timer(100, new TimerListener());
    if (progressMonitor == null){
      progressMonitor = new ProgressDialog();
      progressMonitor.showProgressDialog(true);
    }
    task.go();
    timer.start();

    return _filename;
  }

  /**
   * Saves a XML text to file.
   * It offers the user to change the provided name.
   * @param _filename String
   * @param _annotation String
   * @param _text String
   * @param _xml String
   * @return String The name of the file finally saved (null if aborted or errors).
   */
  public String saveXML (String _filename, String _annotation, String _xml) {
    if (!isConnected()) return null;
    if (!savePanel.showSaveOptions(_filename,_annotation)) return null;
    _filename = savePanel.nameField.getText().trim();
    if (_filename.length()<=0) return null;
    _annotation = savePanel.annotationField.getText();

    task = new LongTask(_filename, _annotation, _xml, false);
    timer = new Timer(100, new TimerListener());
    if (progressMonitor == null){
      progressMonitor = new ProgressDialog();
      progressMonitor.showProgressDialog(true);
    }
    task.go();
    timer.start();

    return _filename;
  }

// ----------------------------------------
// Reading
// ----------------------------------------

  public byte[] readBinary (String _ext) {
    if (!isConnected()) return null;
    String fragID = readPanel.chooseFragment (Request.REQUEST_TYPE_BINARY_SET);
    if (fragID==null) return null;
    ServletMessenger messenger = new ServletMessenger(eMersionURL);
    Request request= new Request(eMersionSessionID,Request.REQUEST_FRAGMENT,Request.REQUEST_TYPE_BINARY_SET);
    request.setFragID(fragID);
    try {
      messenger.send(request);
      Message feedback= (Message) messenger.getFeedback();
      System.out.println("Response:"+ feedback.toString());
      System.out.println("Response feedback:"+feedback.getStatus());
      DatasFile file = (DatasFile) feedback;
      return file.getContent();
    }
    catch (Exception e) { showException (e); }
    return null;
  }

  public String readText (String _filename) { // for the moment, _filename is ignored
    if (!isConnected()) return null;
    String fragID = readPanel.chooseFragment (Request.REQUEST_TYPE_TEXT_SET);
    if (fragID==null) return null;
    ServletMessenger messenger = new ServletMessenger(eMersionURL);
    Request request= new Request(eMersionSessionID,Request.REQUEST_FRAGMENT,Request.REQUEST_TYPE_TEXT_SET);
    request.setFragID(fragID);
    try {
      messenger.send(request);
      Message feedback= (Message) messenger.getFeedback();
      System.out.println("Response:"+ feedback.toString());
      System.out.println("Response feedback:"+feedback.getStatus());
      TextFile file = (TextFile) feedback;
      String text = file.getText();
      return text;
    }
    catch (Exception e) { showException (e); }
    return null;
  }

  public String readXML (String _filename) { // for the moment, _filename is ignored
    if (!isConnected()) return null;
    String fragID = readPanel.chooseFragment (Request.REQUEST_TYPE_PARAMS_SET);
    if (fragID==null) return null;
    ServletMessenger messenger = new ServletMessenger(eMersionURL);
    Request request= new Request(eMersionSessionID,Request.REQUEST_FRAGMENT,Request.REQUEST_TYPE_PARAMS_SET);
    request.setFragID(fragID);
    try {
      messenger.send(request);
      Message feedback= (Message) messenger.getFeedback();
      System.out.println("Response:"+ feedback.toString());
      System.out.println("Response feedback:"+feedback.getStatus());
      ParamsFileXML xmlFile = (ParamsFileXML) feedback; String text = xmlFile.getParamsXML();
      return text;
    }
    catch (Exception e) { showException (e); }
    return null;
  }

  public java.awt.Image readImage (String _ext) {
    if (!isConnected()) return null;
    String fragId = readPanel.chooseFragment (Request.REQUEST_TYPE_BINARY_SET);
    System.out.println ("File for reading image from eMersion is "+fragId);
    return null;
  }

// ---------------------------------
// Private classes
// ---------------------------------

  private java.awt.Component getParentComponent() {
    if (simulation != null) return simulation.getParentComponent();
    return parentComponent;
  }

  private void showException (Exception _exc) {
    _exc.printStackTrace();
    System.out.println ("A dialog should appear now");
    JOptionPane.showMessageDialog(getParentComponent(),_exc.getLocalizedMessage());
  }

//  private String chooseFragment (String _type) {
//    ServletMessenger messenger = new ServletMessenger(eMersionURL);
//    Request request= new Request(eMersionSessionID,Request.REQUEST_LIST, _type, true); // true = append name
//    try {
//      messenger.send(request);
//      Message feedback= (Message) messenger.getFeedback();
//      HashTableMessage hashtable = (HashTableMessage) feedback;
//      Hashtable<?, ?> fragList = hashtable.getContent();
//      Enumeration<?> en = fragList.keys();
//      if (fragList.size()==0) {
//        JOptionPane.showMessageDialog(getParentComponent(),"No fragments available of type:"+ _type,
//                                      "Emersion", JOptionPane.ERROR_MESSAGE);
//        return null; // No fragments to read
//      }
//      Object[] files = new Object[fragList.size()];
//      int i = 0;
//      while (en.hasMoreElements()) { files[i] = en.nextElement().toString(); i++; }
//      Object selected = javax.swing.JOptionPane.showInputDialog(getParentComponent(), "Choose one", "Input",
//        javax.swing.JOptionPane.INFORMATION_MESSAGE, null,files, files[0]);
//      if (selected!=null) return (String) fragList.get(selected);
//    }
//    catch (Exception exc) { exc.printStackTrace(); }
//    return null;
//  }

// ---------------------------------
// Private classes
// ---------------------------------

  private class SavePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    JLabel nameLabel, annotationLabel;
    JTextField nameField, annotationField;

    SavePanel () {
      nameLabel = new JLabel ("Name");
      nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
      nameLabel.setBorder(BorderFactory.createEmptyBorder(0,0,0,2));
      nameField = new JTextField();
      nameField.setColumns(10);

      annotationLabel = new JLabel ("Annotation");
      annotationLabel.setHorizontalAlignment(SwingConstants.CENTER);
      annotationLabel.setBorder(BorderFactory.createEmptyBorder(0,0,0,2));
      annotationField = new JTextField();
      annotationField.setColumns(10);

      JPanel leftPanel = new JPanel(new java.awt.GridLayout(0,1));
      leftPanel.add(nameLabel);
      leftPanel.add(annotationLabel);

      JPanel rightPanel = new JPanel(new java.awt.GridLayout(0,1));
      rightPanel.add(nameField);
      rightPanel.add(annotationField);

      setLayout(new BorderLayout());
      add(leftPanel,BorderLayout.WEST);
      add(rightPanel,BorderLayout.CENTER);
    }

    boolean showSaveOptions (String _name, String _ann) {
      if (_name!=null) nameField.setText(_name);
      if (_ann!=null) annotationField.setText(_ann);
      int option = JOptionPane.showConfirmDialog(getParentComponent(), this, "eMersion", JOptionPane.OK_CANCEL_OPTION);
      return (option==JOptionPane.OK_OPTION);
    }

  } // End of private class

  private class ReadPanel extends JPanel {
    private static final long serialVersionUID = 1L;
//    JLabel nameLabel, annotationLabel;
//    JTextField nameField, annotationField;
    DefaultListModel listModel;
    JList list;

    ReadPanel () {
      listModel = new DefaultListModel();
      list = new JList(listModel);
      list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      JScrollPane scrollPanel = new JScrollPane(list);
      setLayout(new BorderLayout());
      add(scrollPanel,BorderLayout.CENTER);
    }

    String chooseFragment (String _type) {
      ServletMessenger messenger = new ServletMessenger(eMersionURL);
      Request request= new Request(eMersionSessionID,Request.REQUEST_LIST, _type, true); // true = append name
      try {
        messenger.send(request);
        Message feedback= (Message) messenger.getFeedback();
        HashTableMessage hashtable = (HashTableMessage) feedback;
        Hashtable<?, ?> fragList = hashtable.getContent();
        listModel.removeAllElements();
        for (Enumeration<?> en = fragList.keys(); en.hasMoreElements(); ) {
          listModel.addElement(en.nextElement());
        }
        if (fragList.size()>0) list.setSelectedIndex(0);
        int option = JOptionPane.showConfirmDialog(getParentComponent(), this, "eMersion", JOptionPane.OK_CANCEL_OPTION);
        if (option==JOptionPane.OK_OPTION) return (String) fragList.get(list.getSelectedValue());
      }
      catch (Exception exc) { showException (exc); }
      return null;
    }

  } // End of private class

  private class LongTask {
    private int lengthOfTask;
    private int current = 0;
    private String statMessage;
    private String filename;
    private String annotation;
    private java.awt.Image image;
    private String txt;
//    private String xml;
    private byte[] data;
    private boolean isText;
    private String kindFragment;
    private boolean isOk = false;

    LongTask (String _filename, String _annotation, java.awt.Image _image) {
        //compute length of task ...
        //in a real program, this would figure out
        //the number of bytes to read or whatever
        this.kindFragment = "image";
        this.filename = _filename;
        this.annotation = _annotation;
        this.image = _image;
        lengthOfTask = 100;
    }

    LongTask (String _filename, String _annotation, String _txt, boolean _isText) {
        //compute length of task ...
        //in a real program, this would figure out
        //the number of bytes to read or whatever
        this.kindFragment = "text";
        this.filename = _filename;
        this.annotation = _annotation;
        this.txt = _txt;
        this.isText = _isText;
        lengthOfTask = 100;
    }

    LongTask (String _filename, String _annotation, byte[] _data) {
        //compute length of task ...
        //in a real program, this would figure out
        //the number of bytes to read or whatever
        this.kindFragment = "data";
        this.filename = _filename;
        this.annotation = _annotation;
        this.data = _data;
        lengthOfTask = 100;
    }
    //called from ProgressBarDemo to start the task
    void go() {
        current = 0;
        new SwingWorker() {
            public Object construct() {
              if (kindFragment.equals("image")){
                return new ActualTask(filename, annotation, image);
              }else if(kindFragment.equals("text")){
                return new ActualTask(filename, annotation, txt, isText);
              }else{
                return new ActualTask(filename, annotation, data);
              }
            }
        };
    }

    //called from ProgressBarDemo to find out how much work needs to be done
//    int getLengthOfTask() {
//        return lengthOfTask;
//    }

    //called from ProgressBarDemo to find out how much has been done
    int getCurrent() {
        return current;
    }

    void stop() {
        current = lengthOfTask;
    }

    boolean getStatus(){
      return isOk;
    }

    //called from ProgressBarDemo to find out if the task has completed
    boolean done() {
        if (current >= lengthOfTask) return true;
        return false;
    }

    String getMessage() {
        return statMessage;
    }

    //the actual long running task, this runs in a SwingWorker thread
    class ActualTask {
        ActualTask (String _filename, String _annotation, java.awt.Image _image) {
            //fake a long task,
            //make a random amount of progress every second

            try {
                for (int i=0;i<50;i++){
                  current = i;
                  delay(20);
                  statMessage = "Completed " + current + "% out of " + 100 + "%.";
                }
                ServletMessenger messenger = new ServletMessenger(eMersionURL);
                GraphicsFile im = new GraphicsFile(eMersionSessionID,_filename,_image);
                im.setAnnotation(_annotation);
                messenger.send(im);
                for (int i=50;i<101;i++){
                  current = i;
                  delay(50);
                  statMessage = "Completed " + current + "% out of " + lengthOfTask + "%.";
                  isOk = true;
                }
            }catch (Exception e) {
                showException(e);
                for (int i=50;i<101;i++){
                  current = i;
                  delay(10);
                  statMessage = "Completed " + current + "% out of " + lengthOfTask + "%.";
                  isOk = false;
                }
            }

        }
        ActualTask (String _filename, String _annotation, String _txt, boolean _isText) {
            //fake a long task,
            //make a random amount of progress every second

            try {
                for (int i=0;i<50;i++){
                  current = i;
                  delay(20);
                  statMessage = "Completed " + current + "% out of " + lengthOfTask + "%.";
                }
                if (isText){
                  ServletMessenger messenger = new ServletMessenger(eMersionURL);
                  TextFile file = new TextFile(eMersionSessionID, _filename, _annotation, _txt);
                  messenger.send(file);
                }else{
                  ServletMessenger messenger = new ServletMessenger(eMersionURL);
                  ParamsFileXML file = new ParamsFileXML(eMersionSessionID,_filename,_annotation,_txt);
                  messenger.send(file);
                }
                for (int i=50;i<101;i++){
                  current = i;
                  delay(50);
                  statMessage = "Completed " + current + "% out of " + lengthOfTask + "%.";
                  isOk = true;
                }
            }catch (Exception e) {
                showException(e);
                for (int i=50;i<101;i++){
                  current = i;
                  delay(10);
                  statMessage = "Completed " + current + "% out of " + lengthOfTask + "%.";
                  isOk = false;
                }
            }

        }
        ActualTask (String _filename, String _annotation, byte[] _data) {
            //fake a long task,
            //make a random amount of progress every second

            try {
                for (int i=0;i<50;i++){
                  current = i;
                  delay(20);
                  statMessage = "Completed " + current + "% out of " + lengthOfTask + "%.";
                }
                ServletMessenger messenger = new ServletMessenger(eMersionURL);
                DatasFile df = new DatasFile(eMersionSessionID,_filename,_annotation,_data);
                messenger.send(df);
                for (int i=50;i<101;i++){
                  current = i;
                  delay(50);
                  statMessage = "Completed " + current + "% out of " + lengthOfTask + "%.";
                  isOk = true;
                }
            }catch (Exception e) {
                showException(e);
                for (int i=50;i<101;i++){
                  current = i;
                  delay(10);
                  statMessage = "Completed " + current + "% out of " + lengthOfTask + "%.";
                  isOk = false;
                }
            }

        }
     }
  }

  class TimerListener implements ActionListener {
    public void actionPerformed(ActionEvent evt) {
        if (task.done()) {
            if (task.getStatus()){
              progressMonitor.setProgress(100);
              progressMonitor.setNote("Completed " + "100" + "% out of 100%.");
              progressMonitor.setBarString("Successful Delivery... Close this window and refresh eJournal");
            }else{
              progressMonitor.setBarString("Sending with problems. Try it again!!!");
              progressMonitor.setNote(task.getMessage());
            }
            task.stop();
            Toolkit.getDefaultToolkit().beep();
            timer.stop();
        } else {
            int actual = task.getCurrent();
            if ( actual == previous){
              progressMonitor.setBarIndeterminate(true);
              progressMonitor.setBarString("Sending Fragment... Please wait");
            }else{
              progressMonitor.setBarIndeterminate(false);
              progressMonitor.setBarString("Progress");
            }
            previous = actual;
            progressMonitor.setNote(task.getMessage());
            progressMonitor.setProgress(actual);
        }
    }
  }

  class ProgressDialog extends JDialog{

    private JProgressBar progressBar;
    private JLabel msg;
    private JLabel note;

    public ProgressDialog(){
      super(JOptionPane.getFrameForComponent(getParentComponent()),"Progress");
      progressBar = new JProgressBar(0, 100);
      progressBar.setValue(0);
      progressBar.setStringPainted(true);

      msg = new JLabel("Monitoring the Delivery");
      note = new JLabel("Initializing progress...");

      JPanel panel = new JPanel(new GridLayout(3,0));
      panel.add(msg);
      panel.add(note);
      panel.add(progressBar);
      panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
      setContentPane(panel);
      setSize(380,120);
      setLocation(300,300);
      addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent we) {
          dispose();
          progressMonitor.dispose();
          progressMonitor = null;
        }
      });

    }

    public void setProgress(int value){
      progressBar.setValue(value);
    }

    public void setNote(String _note){
      note.setText(_note);
    }

    public void showProgressDialog(boolean _value){
      setVisible(_value);
    }

    public void setBarIndeterminate(boolean _value){
      progressBar.setIndeterminate(_value);
    }

    public void setBarString(String _value){
      progressBar.setString(_value);
    }

  }


} // End of class
