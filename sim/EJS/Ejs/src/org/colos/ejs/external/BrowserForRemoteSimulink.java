package org.colos.ejs.external;

import java.util.*;
import java.awt.*;

import javax.swing.*;
import org.colos.ejs.osejs.utils.*;
import org.colos.ejs.osejs.edition.Editor;
import java.io.File;
import java.net.*;
import java.io.*;
import java.awt.event.*;
import javax.imageio.*;


public class BrowserForRemoteSimulink extends BrowserForExternal implements java.awt.event.ActionListener {
  static private final String SEPARATOR="_%_"; //$NON-NLS-1$
  static private final String INTEGRATOR_BLOCK="-IB-"; //$NON-NLS-1$
  static private final String EJS_PREFIX="Ejs_"; //$NON-NLS-1$
  static private final ResourceUtil res = new ResourceUtil("Resources"); //$NON-NLS-1$
  static private final String MAIN_BLOCK=res.getString("SimulinkBrowser.MainBlock"); //$NON-NLS-1$
  static private final String MAIN_PARAMETERS="Output=time;Parameters=InitialStep,FixedStep,MaxStep;"; //$NON-NLS-1$


  static private EjsJim Rmatlab; //##
  //private EjsJim Rmatlab; //##
  static private boolean blockComboActive = false, currentBlockIsIntegrator=false;
//  static private int id;   // The integer id for the Matlab engine opened
  static private double currentBlockHandle=Double.NaN;
  static private String returnValue=null, currentBlockName=null, originalBlockName=null;
  static private BrowserForRemoteSimulink currentBrowser=null;
  static private javax.swing.Timer timer;
  static private Color blockComboDefColor;

  static private JLabel blockLabel, variableLabel;
  static private JButton blockDeleteButton, blockUndeleteButton;
  static private JCheckBox blockCB;
  static private JComboBox blockCombo;
  static private JDialog dialog;
  static private DefaultListModel modelInput, modelOutput, modelParameters;
  static private JList listBlockInput,listBlockOutput,listBlockParameters;
  static private JList listVariableInput,listVariableOutput,listVariableParameters;


  private boolean hasChanged=false;
  // public String mdlFile, theMdlFile,mdlFileIPPort;
  static String mdlFile, theMdlFile,mdlFileIPPort;
  private Vector<String> blocksInModel,blocksToDelete;
  //private boolean fileExists=true;
  static boolean fileExists=true;
  static private modelwindow remoteSimulinkImage=null;

  static boolean isRemoteSimulinkFile (String filename) {
    filename = filename.toLowerCase();
    if (filename.endsWith(".mdl") && (filename.startsWith("<matlab:") || filename.startsWith("<matlabas:"))) return true; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    return false;
  }

  public boolean fileExists () { return fileExists; }

  public BrowserForRemoteSimulink (String _mdlFile) {
    long longitudArchivo=0;
    Rmatlab=null;
    fileExists=true;
    mdlFileIPPort=_mdlFile;
    mdlFile = _mdlFile.trim();
    mdlFile = mdlFile.substring(mdlFile.lastIndexOf('>')+1);

    if (!mdlFile.toLowerCase().endsWith(".mdl")) mdlFile += ".mdl"; //$NON-NLS-1$ //$NON-NLS-2$
    if (mdlFile.lastIndexOf('/')>=0) theMdlFile = mdlFile.substring (mdlFile.lastIndexOf("/")+1, mdlFile.length()-4); //$NON-NLS-1$
    else theMdlFile = mdlFile.substring (0, mdlFile.length()-4);

    String homeDir = System.getProperty("user.home"); //$NON-NLS-1$
    if (homeDir.endsWith(java.io.File.separator));
    else homeDir = homeDir + java.io.File.separator;
    homeDir = homeDir.replace('\\','/');

    File theFile = new File(homeDir+mdlFile);
    longitudArchivo=theFile.length();
    loadMatlab();
    if (Rmatlab.isConnected()){
      if (!theFile.exists()) {
        if (!Rmatlab.remoteFileExist(mdlFile)){
          System.out.println("Error Model "+ mdlFile +" doesn't exist in Local or Remote Place"); //$NON-NLS-1$ //$NON-NLS-2$
          fileExists=false;
          exitMatlab();
          return;
        }
      }else{
        byte abModel[];
        try{
          FileInputStream fstream = new FileInputStream(homeDir+mdlFile);
          DataInputStream in = new DataInputStream(fstream);
          abModel= new byte[(int)longitudArchivo];
          in.readFully(abModel);
          in.close();
        }catch (Exception e){System.err.println("Model reading error:"+e);  fileExists=false;return;}; //$NON-NLS-1$
        fileExists=true;
        if (mdlFile.lastIndexOf('/')>=0) theMdlFile = mdlFile.substring (mdlFile.lastIndexOf("/")+1, mdlFile.length()-4);
        else theMdlFile = mdlFile.substring (0, mdlFile.length()-4); //Gonzalo 060509
        theMdlFile=Rmatlab.importModel(theMdlFile+".mdl",abModel);  //Create model in Remote Matlab //$NON-NLS-1$
        theMdlFile=theMdlFile.substring (0, theMdlFile.length()-4);
      }
      blocksToDelete = new Vector<String>(); // Prepare information about deleted blocks
      blocksInModel = null;

    }else{
      System.out.println("Warning there is not a remote Matlab connection available"); //$NON-NLS-1$
      fileExists=false;
      return;
    }
  }

  public synchronized void actionPerformed (java.awt.event.ActionEvent event) {
    if (!systemIsOpen()) {  // In case the user closes the system directly
      blockCB.setSelected(false);
      blockCombo.setEnabled(true);
      timer.stop();
      return;
    }
    Rmatlab.eval("EjsCurrentBlock=gcbh"); // ; EjsCurrentBlockName=get_param(EjsCurrentBlock,'name');"); //$NON-NLS-1$
    double blockHandle = Rmatlab.getDouble("EjsCurrentBlock"); //$NON-NLS-1$
    if (currentBlockHandle==blockHandle) { // Check if the user has changed the name of the block
      String blockName = Rmatlab.waitForString ("EjsCurrentBlockName",                                                       "EjsCurrentBlockName=get_param(EjsCurrentBlock,'name')");
      Rmatlab.eval("clear EjsCurrentBlock; clear EjsCurrentBlockName"); //$NON-NLS-1$
      if (blockName==null) return;
     // System.out.println("nombre bloque original:"+originalBlockName);
     // System.out.println("nombre bloque ahora:"+blockName);
     //## if (!blockName.equals(originalBlockName))
       //## JOptionPane.showMessageDialog(dialog, res.getString("SimulinkBrowser.ChangeNameNotAllowed"),
        //##                      res.getString("Warning"), JOptionPane.YES_NO_OPTION);
      return;
    }
    Rmatlab.eval("clear EjsCurrentBlock;");
    // a new block has been selected
    currentBlockHandle = blockHandle;
    if (blockHandle!=-1) getBlockVariables ("block=gcb;\n"); // Query the block variables
  }

  public boolean isChanged () { return hasChanged; }

  public void setChanged (boolean _change) { hasChanged = _change; }

// ---------------------------------------
// Extending BrowserForExternal
// ---------------------------------------

  public StringBuffer generateCode (int _type) {
    exitMatlab();
    //if (_type==Editor.GENERATE_JARS_NEEDED) return new StringBuffer("ejsExternal.jar;");
    if (_type==Editor.GENERATE_DECLARATION) return new StringBuffer("org.colos.ejs.external.EjsRemoteSimulink.class");
    if (_type==Editor.GENERATE_RESOURCES_NEEDED) return new StringBuffer(mdlFile+";");

    return new StringBuffer();
  }

  public String saveString () {
    String code = "";
    for (Enumeration<String> e = blocksToDelete.elements(); e.hasMoreElements();) {
      String blockName = e.nextElement();
      if (blocksInModel==null) code += blockName + SEPARATOR;
      else if (blocksInModel.contains(blockName)) code += blockName + SEPARATOR;
    }
    return code;
  }

  public void readString (String _code) {
    int pos = _code.indexOf(SEPARATOR);
    while (pos>=0) {
      String block = _code.substring(0,pos);
      _code = _code.substring(pos+SEPARATOR.length());
      blocksToDelete.add(block);
      pos = _code.indexOf(SEPARATOR);
    }
  }

  public String edit (String _property, String _name, JComponent _target, String _value) {
    if (!fileExists) return null;

    loadMatlab();

    timer = new javax.swing.Timer (20,this);
    createTheInterface();
    currentBrowser = this;
    queryModel();
    processList(null);
    currentBlockHandle=Double.NaN;
    setCurrentValue (_value);
    dialog.setLocationRelativeTo (_target);
    dialog.setTitle (res.getString("EditorForVariables.Title"+_property));
    variableLabel.setText(res.getString("SimulinkBrowser.VariableLabel")+" "+_name);
    dialog.setVisible (true);
    timer.stop();

    if (systemIsOpen()) {
//      matlab.engEvalString(id, "set_param (gcs, 'Lock','Off')");
      Rmatlab.eval("set_param (gcs, 'Open','Off')");
//      matlab.engEvalString(id, "set_param (gcs, 'Lock','On')");
    }
    // timer.start();
    blockCB.setSelected(false);
    blockCombo.setEnabled(true);
    return returnValue;
  }

  public Vector<String> prepareInitCode () {
    createTheInterface(); // actually it is addToInitCode which needs it
    Vector<String> lines = new Vector<String>();
    // Add the block deletion commands for this model
    for (Enumeration<String> e = blocksToDelete.elements(); e.hasMoreElements();) {
      String blockName = e.nextElement();
      if (blocksInModel!=null && !blocksInModel.contains(blockName)) continue;
      //##lines.add("variables.path{end+1,1}='"+blockName+"';");
      //##lines.add("variables.path{end+1,1}='"+theMdlFile+"/"+blockName+"';"); Gonzalo 060518
      lines.add("variables.path{end+1,1}=[Ejs_random_model_name,'/"+blockName+"'];");
      lines.add("variables.name{end+1,1}='';");
      lines.add("variables.fromto{end+1,1}='Delete';");
      lines.add("variables.port{end+1,1}='';");
    }
    return lines;
  }

  public Vector<String> addToInitCode (String _name, String _connected, double _value) {
    setCurrentValue(_connected);
    Vector<String> lines = new Vector<String>();
    if (isInputVariable(_connected)) { // Just to declare them
      if (Double.isNaN(_value)) lines.add(EJS_PREFIX+_name + " = 0;");
      else lines.add(EJS_PREFIX+_name + " = "+ _value + ";");
    }
    if (isOutputVariable(_connected) && _connected.indexOf(INTEGRATOR_BLOCK)>=0) {
      if (Double.isNaN(_value)) lines.add(EJS_PREFIX+_name + "_IC = 0;");
      else lines.add(EJS_PREFIX+_name + "_IC = "+ _value + ";");
      // lines.add(EJS_PREFIX + _name + "_RS = 0;");
    }
    for (int i=0,n=modelInput.getSize(); i<n; i++) {
      String oneConnection = ((String) modelInput.get(i)).trim();
      int begin = oneConnection.indexOf('('), end = oneConnection.lastIndexOf(')');
      if (begin<0) ; // It is an only output var from the model (actually there is no such variable)
      else {
        String block = oneConnection.substring(begin+1,end), position = oneConnection.substring(5,begin); // 5 = "input"
        if (block.endsWith(INTEGRATOR_BLOCK)) block = block.substring(0,block.length()-INTEGRATOR_BLOCK.length());
        if (blocksInModel!=null && !blocksInModel.contains(block)) continue;
        // ##lines.add("variables.path{end+1,1}='"+block+"';");
        //  lines.add("variables.path{end+1,1}='"+theMdlFile+"/"+block+"';"); Gonzalo 060518
        lines.add("variables.path{end+1,1}=[Ejs_random_model_name,'/"+block+"'];");
        lines.add("variables.name{end+1,1}='"+EJS_PREFIX+_name+"';");
        lines.add("variables.fromto{end+1,1}='In';");
        lines.add("variables.port{end+1,1}='"+position+"';");
      }
    }
    for (int i=0,n=modelOutput.getSize(); i<n; i++) {
      String oneConnection = ((String) modelOutput.get(i)).trim();
      int begin = oneConnection.indexOf('('), end = oneConnection.lastIndexOf(')');
      if (begin<0) ; // It is an only output var from the model, such as 'time'
      else {
        String block = oneConnection.substring(begin+1,end), position = oneConnection.substring(6,begin); // 6 = "output"
        if (block.endsWith(INTEGRATOR_BLOCK)) block = block.substring(0,block.length()-INTEGRATOR_BLOCK.length());
        if (blocksInModel!=null && !blocksInModel.contains(block)) continue;
        //## lines.add("variables.path{end+1,1}='"+block+"';");
 //       lines.add("variables.path{end+1,1}='"+theMdlFile+"/"+block+"';"); Gonzalo 060518
        lines.add("variables.path{end+1,1}=[Ejs_random_model_name,'/"+block+"'];");
        lines.add("variables.name{end+1,1}='"+EJS_PREFIX+_name+"';");
        lines.add("variables.fromto{end+1,1}='Out';");
        lines.add("variables.port{end+1,1}='"+position+"';");
      }
    }
    for (int i=0,n=modelParameters.getSize(); i<n; i++) {
      String oneConnection = ((String) modelParameters.get(i)).trim();
      int begin = oneConnection.indexOf('('), end = oneConnection.lastIndexOf(')');
      String parameter;
      if (begin<0) {
        parameter = oneConnection;
       //## lines.add("variables.path{end+1,1}='';");
//         lines.add("variables.path{end+1,1}='"+theMdlFile+"';"); Gonzalo 060518
           lines.add("variables.path{end+1,1}=[Ejs_random_model_name];");
      }
      else {
        String block = oneConnection.substring(begin+1,end);
        if (block.endsWith(INTEGRATOR_BLOCK)) block = block.substring(0,block.length()-INTEGRATOR_BLOCK.length());
        if (blocksInModel!=null && !blocksInModel.contains(block)) continue;
        parameter = oneConnection.substring(0,begin);
        //## lines.add("variables.path{end+1,1}='"+block+"';");
//        lines.add("variables.path{end+1,1}='"+theMdlFile+"/"+block+"';"); Gonzalo 060518
        lines.add("variables.path{end+1,1}=[Ejs_random_model_name,'/"+block+"'];");
      }
      lines.add("variables.name{end+1,1}='"+EJS_PREFIX+_name+"';");
      lines.add("variables.fromto{end+1,1}='Param';");
      lines.add("variables.port{end+1,1}='"+parameter+"';");
    }
    return lines;
  }

  public String addToInputCode (String _name, String _type, int _dim, String _connectedTo, String _app, String _info) {
    if (_connectedTo.length()<=0) return "";
    String line = "";
    if ( (_type.equals("String") && _dim<1) || (_type.equals("double") && _dim<3) )  {
      if (isInputVariable(_connectedTo)) line += "      " + _app + ".setValue(\"" + EJS_PREFIX+_name + "\"," + _name + "); // " + _info;
      if (isOutputVariable(_connectedTo) && _connectedTo.indexOf(INTEGRATOR_BLOCK)>=0) {
        line += "      " + _app + ".setValue(\"" + EJS_PREFIX+_name + "_IC\"," + _name + "); // " + _info + "\n";
        //line += "      " + _app + ".eval(\"" + EJS_PREFIX+_name + "_RS = 1 - "+EJS_PREFIX+_name+"_RS\"); // " + _info;
      }
    }
    return line;
  }

  public String addToOutputCode (String _name, String _type, int _dim, String _connectedTo, String _app, String _info) {
    if (_connectedTo.length()<=0) return "";
    if (!isOutputVariable(_connectedTo)) return "";
    if (_connectedTo.equals("time")) return "      "+_name+" = "+ _app + ".getDouble(\""+EJS_PREFIX+"time\"); // "+_info;
    String line="";
    if (_type.equals("String") && _dim<1) {
      line = "      " + _name + " = "+ _app + ".getString(\""+EJS_PREFIX+_name+"\"); // "+_info;
    }
    else if (_type.equals("double") && _dim<3) {
      line = "      "+_name+" = "+ _app + ".";
      switch (_dim) {
        case 0 : line += "getDouble"; break;
        case 1 : line += "getDoubleArray"; break;
        case 2 : line += "getDoubleArray2D"; break;
        }
      line += "(\""+EJS_PREFIX+_name+"\"); // "+_info;
    }
    return line;
  }

  protected boolean isInputVariable (String _connected) {
    int pos;
    do {
      pos = _connected.indexOf(SEPARATOR);
      String variable;
      if (pos<0) variable = _connected;
      else { variable = _connected.substring(0,pos); _connected = _connected.substring(pos+SEPARATOR.length()); }
      if (variable.startsWith("input")) return true;
      if (variable.startsWith("output")) continue;
      if (variable.equals("time")) continue;
      return true; // a parameter
    } while (pos>=0);
    return false;
  }

  protected boolean isOutputVariable (String _connected) {
    int pos;
    do {
      pos = _connected.indexOf(SEPARATOR);
      String variable;
      if (pos<0) variable = _connected;
      else { variable = _connected.substring(0,pos); _connected = _connected.substring(pos+SEPARATOR.length()); }
      if (variable.startsWith("output")) return true;
      if (variable.equals("time")) return true;
      if (variable.startsWith("input")) continue;
      // continue; // a parameter
    } while (pos>=0);
    return false;
  }

// End of extending BrowserForExternal

// -----------------------------------
//  The BIG static part
// -----------------------------------

  /**
   * Extracts the connections for the variable from the
   * String and fills in the corresponding lists
   * @param _value String
   */
  private  static void setCurrentValue (String _value) {
    modelInput.removeAllElements();
    modelOutput.removeAllElements();
    modelParameters.removeAllElements();
    _value = _value.trim();
    if (_value.length()<=0) return;
    int pos;
    do {
      pos = _value.indexOf(SEPARATOR);
      String variable;
      if (pos<0) variable = _value;
      else { variable = _value.substring(0,pos); _value = _value.substring(pos+SEPARATOR.length()); }
      if (variable.startsWith("input")) modelInput.addElement(variable);
      else if (variable.startsWith("output")) modelOutput.addElement(variable);
      // 'time' or a parameter
      else if (variable.equals("time")) modelOutput.addElement(variable);
      else modelParameters.addElement(variable);
    } while (pos>=0);
  }

  /**
   * Constructs a single string with the info of the connections established
   * @return String
   */
  private static String finalValue () {
     String txt = "";
     int n;
     if ((n=modelInput.getSize())>0)      for (int i=0; i<n; i++) txt += (String) modelInput.get(i) + SEPARATOR;
     if ((n=modelOutput.getSize())>0)     for (int i=0; i<n; i++) txt += (String) modelOutput.get(i) + SEPARATOR;
     if ((n=modelParameters.getSize())>0) for (int i=0; i<n; i++) txt += (String) modelParameters.get(i) + SEPARATOR;
     if (txt.endsWith(SEPARATOR)) txt = txt.substring(0,txt.length()-SEPARATOR.length());
     return txt;
   }

  static private void createTheInterface () {
    if (dialog!=null) return;
    java.awt.event.ActionListener comboListener = new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {

        String command = evt.getActionCommand();
        if (command.equals("blockCombo")) {
          String blockSelected = (String) blockCombo.getSelectedItem();
          if (blockComboActive) {
            if (blockSelected.equals(MAIN_BLOCK)) processList(null);
            else {
              //getBlockVariables("block='"+theMdlFile+"/"+ blockSelected + "';\n");
              //getBlockVariables("block='" + currentBrowser.theMdlFile + "/" + blockSelected + "';\n"); Gonzalo 060518
              getBlockVariables("block='" + theMdlFile + "/" + blockSelected + "';\n");
            }
            currentBlockHandle=Double.NaN;
          }
        }
        else if (command.equals("showHide")) {
          if (blockCB.isSelected()) {

            if (!systemIsOpen()) openSystem();
          //            matlab.engEvalString(id, "set_param (gcs, 'Lock','Off')");
          Rmatlab.eval("set_param (gcs, 'Open','On')");

          if  (remoteSimulinkImage==null) {

            //System.out.println("Starting Primer...");
            remoteSimulinkImage=new modelwindow();
            //mainFrame.setSize(400, 400);
            remoteSimulinkImage.setTitle("Remote Model");
            remoteSimulinkImage.setVisible(true);
            remoteSimulinkImage.repaint();
          }else{
            remoteSimulinkImage.setVisible(true);
            remoteSimulinkImage.repaint();
          }

//            matlab.engEvalString(id, "set_param (gcs, 'Lock','On')");
            blockCombo.setEnabled(false);
            timer.start();
          }
          else {

            //Close Window Remote Model  Gonzalo 060518
            if (remoteSimulinkImage!=null){
              remoteSimulinkImage.dispose();
              remoteSimulinkImage=null;
            }

            timer.stop();
            blockCombo.setEnabled(true);
//            matlab.engEvalString(id, "set_param (gcs, 'Lock','Off')");
            Rmatlab.eval("set_param (gcs, 'Open','Off')");
//            matlab.engEvalString(id, "set_param (gcs, 'Lock','On')");
          }
        }
      }
    };

    java.awt.event.MouseAdapter mouseListener = new java.awt.event.MouseAdapter() {
      public void mousePressed(java.awt.event.MouseEvent evt) {
        if (evt.getSource() instanceof JList) {
          if (evt.getClickCount() > 1) {
            JList list = (JList) evt.getSource();
            String selection = (String) list.getSelectedValue();
            if (selection==null) return;
            String block;
            if (MAIN_BLOCK.equals(currentBlockName)) block = "";
            else {
              block = currentBlockName;
              if (currentBlockIsIntegrator) block += INTEGRATOR_BLOCK;
              block = "("+block+")";
            }
            // Depends on which list was chosen
            if (list == listBlockInput && !modelInput.contains(selection)) modelInput.addElement(selection+block);
            else if (list == listBlockOutput) { modelOutput.removeAllElements(); modelOutput.addElement(selection+block); }
            else if (list == listBlockParameters && !modelParameters.contains(selection)) modelParameters.addElement(selection+block);
            else if (list == listVariableInput && modelInput.contains(selection)) modelInput.removeElement(selection);
            else if (list == listVariableOutput) modelOutput.removeAllElements();
            else if (list == listVariableParameters && modelParameters.contains(selection)) modelParameters.removeElement(selection);
            currentBrowser.hasChanged = true;
          }
          return;
        }
        AbstractButton button = (AbstractButton) (evt.getSource());
        String aCmd = button.getActionCommand();
        if (aCmd.equals("ok")) {
            if (remoteSimulinkImage!=null){
              remoteSimulinkImage.dispose();
              remoteSimulinkImage=null;
            }
           returnValue = finalValue();
           dialog.setVisible(false);
        }
        else if (aCmd.equals("cancel")) {

          if (remoteSimulinkImage!=null){
            remoteSimulinkImage.dispose();
            remoteSimulinkImage=null;
          }
          returnValue = null;
          dialog.setVisible(false);

        }
        else if (aCmd.equals("deleteBlock")) {
          if (!MAIN_BLOCK.equals(currentBlockName)) {
            if (!currentBrowser.blocksToDelete.contains(currentBlockName)) currentBrowser.blocksToDelete.add(currentBlockName);
            blockDeleteButton.setEnabled(false);
            blockUndeleteButton.setEnabled(true);
            blockCombo.setForeground(Color.gray);
            listBlockInput.setEnabled(false); listBlockOutput.setEnabled(false); listBlockParameters.setEnabled(false);
            currentBrowser.hasChanged = true;
          }
        }
        else if (aCmd.equals("undeleteBlock")) {
          if (!MAIN_BLOCK.equals(currentBlockName)) {
            if (currentBrowser.blocksToDelete.contains(currentBlockName)) currentBrowser.blocksToDelete.remove(currentBlockName);
            blockDeleteButton.setEnabled(true);
            blockUndeleteButton.setEnabled(false);
            blockCombo.setForeground(Color.black);
            listBlockInput.setEnabled(true); listBlockOutput.setEnabled(true); listBlockParameters.setEnabled(true);
            currentBrowser.hasChanged = true;
          }
        }
      }
    };

    JButton okButton = new JButton(res.getString("EditorFor.Ok"));
    okButton.setActionCommand("ok");
    okButton.addMouseListener(mouseListener);

    JButton cancelButton = new JButton(res.getString("EditorFor.Cancel"));
    cancelButton.setActionCommand("cancel");
    cancelButton.addMouseListener(mouseListener);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);

    blockCB = new JCheckBox(res.getString("SimulinkBrowser.BlockLabel"));
    blockCB.setActionCommand("showHide");
    blockCB.addActionListener(comboListener);

    blockLabel = new JLabel(res.getString("SimulinkBrowser.BlockLabel"));
    blockCombo = new JComboBox();
    blockCombo.setActionCommand("blockCombo");
    blockCombo.addActionListener(comboListener);
    blockComboDefColor = blockCombo.getForeground();
    blockCombo.setRenderer(new BrowserForSimulinkCR());
    blockDeleteButton = new JButton(res.getString("SimulinkBrowser.BlockDelete"));
    blockDeleteButton.setActionCommand("deleteBlock");
    blockDeleteButton.addMouseListener(mouseListener);
    blockUndeleteButton = new JButton(res.getString("SimulinkBrowser.BlockUndelete"));
    blockUndeleteButton.setActionCommand("undeleteBlock");
    blockUndeleteButton.addMouseListener(mouseListener);

    JPanel blocksLabelPanel = new JPanel(new java.awt.BorderLayout());
    blocksLabelPanel.add(blockCB, java.awt.BorderLayout.WEST);
    blocksLabelPanel.add(blockLabel, java.awt.BorderLayout.CENTER);

    JPanel blocksDeletePanel = new JPanel(new java.awt.GridLayout(1,2));
    blocksDeletePanel.add(blockDeleteButton);
    blocksDeletePanel.add(blockUndeleteButton);

    JPanel blockPanelUp = new JPanel(new java.awt.BorderLayout());
    blockPanelUp.add(blockCB, java.awt.BorderLayout.WEST);
    blockPanelUp.add(blockCombo, java.awt.BorderLayout.CENTER);
    blockPanelUp.add(blocksDeletePanel, java.awt.BorderLayout.EAST);

    listBlockInput = new JList();
    listBlockInput.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    listBlockInput.addMouseListener(mouseListener);
    listBlockInput.setFont(InterfaceUtils.font(null,res.getString("Editor.DefaultFont")));
    JPanel panelBlockInput = new JPanel(new java.awt.BorderLayout());
    panelBlockInput.add(new JLabel (res.getString("SimulinkBrowser.Input"),SwingConstants.CENTER),java.awt.BorderLayout.NORTH);
    panelBlockInput.add(new JScrollPane(listBlockInput),java.awt.BorderLayout.CENTER);

    listBlockOutput = new JList();
    listBlockOutput.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    listBlockOutput.addMouseListener(mouseListener);
    listBlockOutput.setFont(InterfaceUtils.font(null,res.getString("Editor.DefaultFont")));
    JPanel panelBlockOutput = new JPanel(new java.awt.BorderLayout());
    panelBlockOutput.add(new JLabel (res.getString("SimulinkBrowser.Output"),SwingConstants.CENTER),java.awt.BorderLayout.NORTH);
    panelBlockOutput.add(new JScrollPane(listBlockOutput),java.awt.BorderLayout.CENTER);

    listBlockParameters = new JList();
    listBlockParameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    listBlockParameters.addMouseListener(mouseListener);
    listBlockParameters.setFont(InterfaceUtils.font(null,res.getString("Editor.DefaultFont")));
    JPanel panelBlockParameters = new JPanel(new java.awt.BorderLayout());
    panelBlockParameters.add(new JLabel (res.getString("SimulinkBrowser.Parameters"),SwingConstants.CENTER),java.awt.BorderLayout.NORTH);
    panelBlockParameters.add(new JScrollPane(listBlockParameters),java.awt.BorderLayout.CENTER);

    JPanel blockPanelCenter = new JPanel(new java.awt.GridLayout(1,3));
    blockPanelCenter.add(panelBlockInput);
    blockPanelCenter.add(panelBlockOutput);
    blockPanelCenter.add(panelBlockParameters);

    JPanel blockPanel = new JPanel (new java.awt.BorderLayout());
    blockPanel.add (blockPanelUp,java.awt.BorderLayout.NORTH);
    blockPanel.add (blockPanelCenter,java.awt.BorderLayout.CENTER);

    listVariableInput = new JList();
    listVariableInput.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    listVariableInput.addMouseListener(mouseListener);
    listVariableInput.setFont(InterfaceUtils.font(null,res.getString("Editor.DefaultFont")));
    listVariableInput.setModel(modelInput = new DefaultListModel());
    JPanel panelVariableInput = new JPanel(new java.awt.BorderLayout());
    panelVariableInput.add(new JLabel (res.getString("SimulinkBrowser.Input"),SwingConstants.CENTER),java.awt.BorderLayout.NORTH);
    panelVariableInput.add(new JScrollPane(listVariableInput),java.awt.BorderLayout.CENTER);

    listVariableOutput = new JList();
    listVariableOutput.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    listVariableOutput.addMouseListener(mouseListener);
    listVariableOutput.setFont(InterfaceUtils.font(null,res.getString("Editor.DefaultFont")));
    listVariableOutput.setModel(modelOutput = new DefaultListModel());
    JPanel panelVariableOutput = new JPanel(new java.awt.BorderLayout());
    panelVariableOutput.add(new JLabel (res.getString("SimulinkBrowser.Output"),SwingConstants.CENTER),java.awt.BorderLayout.NORTH);
    panelVariableOutput.add(new JScrollPane(listVariableOutput),java.awt.BorderLayout.CENTER);

    listVariableParameters = new JList();
    listVariableParameters.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    listVariableParameters.addMouseListener(mouseListener);
    listVariableParameters.setFont(InterfaceUtils.font(null,res.getString("Editor.DefaultFont")));
    listVariableParameters.setModel(modelParameters = new DefaultListModel());
    JPanel panelVariableParameters = new JPanel(new java.awt.BorderLayout());
    panelVariableParameters.add(new JLabel (res.getString("SimulinkBrowser.Parameters"),SwingConstants.CENTER),java.awt.BorderLayout.NORTH);
    panelVariableParameters.add(new JScrollPane(listVariableParameters),java.awt.BorderLayout.CENTER);

    variableLabel = new JLabel(res.getString("SimulinkBrowser.VariableLabel"));

    JPanel variablePanelCenter = new JPanel(new java.awt.GridLayout(1,3));
    variablePanelCenter.add(panelVariableInput);
    variablePanelCenter.add(panelVariableOutput);
    variablePanelCenter.add(panelVariableParameters);

    JPanel variablePanel = new JPanel (new java.awt.BorderLayout());
    variablePanel.add (variableLabel,java.awt.BorderLayout.NORTH);
    variablePanel.add (variablePanelCenter,java.awt.BorderLayout.CENTER);

    JPanel centerPanel = new JPanel (new java.awt.GridLayout(2,1));
    centerPanel.add (blockPanel);
    centerPanel.add (variablePanel);

    JSeparator sep1 = new JSeparator (SwingConstants.HORIZONTAL);

    JPanel bottomPanel = new JPanel (new java.awt.BorderLayout());
    bottomPanel.add (sep1,java.awt.BorderLayout.NORTH);
    bottomPanel.add (buttonPanel,java.awt.BorderLayout.SOUTH);

    dialog = new JDialog();
    dialog.getContentPane().setLayout (new java.awt.BorderLayout(0,0));
    dialog.getContentPane().add (centerPanel,java.awt.BorderLayout.CENTER);
    dialog.getContentPane().add (bottomPanel,java.awt.BorderLayout.SOUTH);

    dialog.addWindowListener (
      new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent event) {

          timer.stop();
          if (systemIsOpen()) {
//            matlab.engEvalString(id, "set_param (gcs, 'Lock','Off')");
            Rmatlab.eval("set_param (gcs, 'Open','Off')");

            Rmatlab.eval("close_system"); //## para evitar problemas con mostrar el modelo remoto

//            matlab.engEvalString(id, "set_param (gcs, 'Lock','On')");
          }
          returnValue = null;
          if (remoteSimulinkImage!=null){
            remoteSimulinkImage.dispose();
            remoteSimulinkImage=null;
          }
        }
      }
    );

    dialog.setSize (res.getDimension("SimulinkBrowser.Size"));
    dialog.validate();
    dialog.setModal(true);
  }

   /**
    * Processes a String obtained from Matlab with the variables of a given block
    * @param _vars String
    */
    static private void processList (String _vars) {
    Vector<String> inputList = new Vector<String>(), outputList = new Vector<String>(), parameterList = new Vector<String>();
    if (_vars==null || _vars.trim().length()<=0) {
      _vars = MAIN_BLOCK + "_ejs_" + MAIN_PARAMETERS;
      currentBlockHandle = Double.NaN;
    }
    originalBlockName = _vars.substring(0,_vars.indexOf("_ejs_"));
    String blockName = correctBlockName(originalBlockName);
    _vars = _vars.substring(blockName.length()+5);
    currentBlockName = blockName;
    blockComboActive=false;

    blockCombo.setSelectedItem(blockName);
    blockComboActive=true;
    if (blockName.equals(MAIN_BLOCK)) {
      blockDeleteButton.setEnabled(false);
      blockUndeleteButton.setEnabled(false);
      blockCombo.setForeground(Color.black);
      listBlockInput.setEnabled(true); listBlockOutput.setEnabled(true); listBlockParameters.setEnabled(true);
    }
    else {
      boolean deleted = currentBrowser.blocksToDelete.contains(blockName);
      blockDeleteButton.setEnabled(!deleted);
      blockUndeleteButton.setEnabled(deleted);
      blockCombo.setForeground(deleted ? Color.gray : Color.black);
      listBlockInput.setEnabled(!deleted); listBlockOutput.setEnabled(!deleted); listBlockParameters.setEnabled(!deleted);
    }
    StringTokenizer tkn = new StringTokenizer (_vars,";");
    int type = 0; // Input
    while (tkn.hasMoreTokens()) {
      String listStr = tkn.nextToken();
      if (listStr.startsWith("Output")) type = 1;
      else if (listStr.startsWith("Parameters")) type = 2;
      else type = 0;
      listStr = listStr.substring(listStr.indexOf('=')+1);
      StringTokenizer tkn2 = new StringTokenizer (listStr,",");
      while (tkn2.hasMoreTokens()) {
        switch (type) {
          case 1 : outputList.add(tkn2.nextToken()); break;
          case 2 : parameterList.add(tkn2.nextToken()); break;
          default :
          case 0 : inputList.add(tkn2.nextToken()); break;
        }
      }
    }
    listBlockInput.setListData(inputList);
    listBlockOutput.setListData(outputList);
    listBlockParameters.setListData(parameterList);
  }

// -----------------------------------
//  static Matlab utilities
// -----------------------------------

   static private void loadMatlab () {

    if (Rmatlab!=null) return;

    JFrame frame = new JFrame(res.getString("SimulinkBrowser.LoadingMatlab"));
    java.awt.Image image = org.opensourcephysics.tools.ResourceLoader.getImage("data/icons/EjsIcon.gif");
    if (image!=null) frame.setIconImage(image);
    frame.getContentPane().setLayout (new java.awt.BorderLayout ());
    frame.getContentPane().add(new JLabel(res.getString("SimulinkBrowser.LoadingMatlab"),SwingConstants.CENTER)
                               , BorderLayout.CENTER);
    Dimension size = res.getDimension("Osejs.StartDialogSize");
    Rectangle bounds = org.colos.ejs.library.control.EjsControl.getDefaultScreenBounds();
    frame.setSize(Math.min(size.width, bounds.width),Math.min(size.height, bounds.height));
    frame.setLocation(bounds.x+(bounds.width-size.width)/2,bounds.y+(bounds.height-size.width)/2);
    frame.setVisible(true);
    if (org.opensourcephysics.display.OSPRuntime.isWindows()) {
      Rmatlab = new EjsJim(mdlFileIPPort);
    }
    else {
      Rmatlab = new EjsJim(mdlFileIPPort); // create a JMatLink in Unix
      //  id = matlab.engOpen(System.getProperty("Ejs.MatlabCmd")); // engOpenSingleUse does not work on Unix
    }
    frame.setVisible(false);
  }

   static public void exitMatlab () {
    if (Rmatlab!=null) Rmatlab.quit();
    Rmatlab = null;
  }

  static private void openSystem () {
    if (Rmatlab!=null){
      Rmatlab.eval("open_system ('" + theMdlFile + "')"); //Gonzalo 060518
      Rmatlab.eval("set_param (gcs, 'SimulationCommand','Stop')");
      Rmatlab.eval("set_param (gcs, 'StopTime','inf')");
      Rmatlab.eval("set_param (gcs, 'Open','Off')");
      //    matlab.engEvalString(id, "set_param (gcs, 'Lock','On')");
      Rmatlab.eval("variables=['']; blockList=[''];");
    }
  }

  static private void queryModel () {
    if (!systemIsOpen()) openSystem();
    if (Rmatlab!=null) {
      //    matlab.engEvalString (id,blockListCommand);
      blockComboActive = false;
      blockCombo.removeAllItems();
      blockCombo.addItem(MAIN_BLOCK);
      String blockList = Rmatlab.waitForString("blockList", blockListCommand);
      currentBrowser.blocksInModel = new Vector<String>();
      if (blockList == null) {
        processList(null);
        return;
      }
      int pos = blockList.indexOf("_ejs_");
      while (pos >= 0) {
        String piece = correctBlockName(blockList.substring(0, pos));
        blockCombo.addItem(piece);
        currentBrowser.blocksInModel.add(piece);
        blockList = blockList.substring(pos + 5);
        pos = blockList.indexOf("_ejs_");
      }
      blockComboActive = true;
    }
  }

  static private boolean systemIsOpen() {
    if (Rmatlab==null) return false;
    Rmatlab.eval("clear currentSystem;");
    //    matlab.engEvalString(id,"currentSystem=gcs;");
    //String cs = Paco
    Rmatlab.waitForString ("currentSystem","currentSystem=gcs");
    Rmatlab.eval("clear isOPEN;");
    //matlab.engEvalString(id,"try, EjsGetisOPEN=get_param('"+currentBrowser.theMdlFile +"','open'),catch,EjsGetisOPEN='off',end");
   // String isOpen = Rmatlab.waitForString ("isOPEN","try, isOPEN=get_param('"+currentBrowser.theMdlFile +"','open'),catch,isOPEN='off',end"); Gonzalo 060518
   String isOpen = Rmatlab.waitForString ("isOPEN","try, isOPEN=get_param('"+theMdlFile +"','open'),catch,isOPEN='off',end");
    if (isOpen.equalsIgnoreCase("on")) return true;
    //##if (currentBrowser.theMdlFile.equalsIgnoreCase(cs)) return true;
    return false;
  }

  static private void getBlockVariables (String prefix) {
    if (!systemIsOpen()) {
      openSystem();
      if (Rmatlab != null) {
        if (!blockCB.isSelected()) {
          //        matlab.engEvalString(id, "set_param (gcs, 'Lock','Off')");
          Rmatlab.eval("set_param (gcs, 'Open','Off')");
          //      matlab.engEvalString(id, "set_param (gcs, 'Lock','On')");
        }
      }
      //    matlab.engEvalString (id,prefix+updateCommand);
      processList(Rmatlab.waitForString("variables", prefix + updateCommand));
      currentBlockIsIntegrator = (Rmatlab.getDouble("isIntegrator") != 0);
    }
  }

  static private String correctBlockName (String _name) {
    String correctName = "";
    StringTokenizer tkn = new StringTokenizer (_name.replace('\n',' '), "/",true);
    while (tkn.hasMoreTokens()) {
      String piece = tkn.nextToken();
      //####         if (piece.equals("/")) correctName += "//";
      if (piece.equals("/")) correctName += "/";
      else correctName += piece;
    }
    return correctName;
  }

// ---------------------------------------------------
// Stuff needed to prepare the conversion of the model
// ---------------------------------------------------

// --------------------------
// Elaborate Matlab commands
// --------------------------

  static private final String blockListCommand =
        "handle=gcs;\n"
      //"handle='"+currentBrowser.theMdlFile +"';\n" //da null
      //    + "blocks=get_param(handle,'blocks');\n"
      + "blocks=find_system(handle,'type','block');\n"
      + "for ik=1:length(blocks) aux=blocks{ik}; blocks{ik}=aux(length(handle)+2:end); end;\n" //nueva
      + "blockList=[''];\n"
      + "for i=1:size(blocks,1) blockList=[blockList,char(blocks(i,:)),'_ejs_']; end;\n";

  static private final String updateCommand =
      "name=block;\n"//name="get_param(block,'name');\n"
      + "inportnumbers=size(get_param(block,'InputPorts'),1);\n"
      + "in_mess='Input=';\n"
      + "for i=1:inportnumbers in_mess=[in_mess,'input',num2str(i),',']; end;\n"
      + "if strcmp(in_mess(end),',') in_mess(end)=''; end;\n"
      + "outportnumbers=size(get_param(block,'OutputPorts'),1);\n"
      + "out_mess='Output=';\n"
      + "for i=1:outportnumbers out_mess=[out_mess,'output',num2str(i),',']; end;\n"
      + "if strcmp(out_mess(end),',') out_mess(end)=''; end;\n"
      + "parameters=get_param(block,'DialogParameters');\n"
      + "nameparams=char(fieldnames(parameters));\n"
      + "param_mess=['Parameters='];\n"
      + "for i=1:size(nameparams,1) param_mess=[param_mess,deblank(nameparams(i,:)),',']; end;\n"
      + "if strcmp(param_mess(end),',') param_mess(end)=''; end;\n"
      + "name=name(length(handle)+2:end);\n"//##nueva para quitar el nombre del modelo en la path
      + "variables=[name,'_ejs_',in_mess,';',out_mess,';',param_mess];"
      + "blocktype=get_param(block,'BlockType'); \n"
      + "isIntegrator = strcmp(deblank(blocktype),'Integrator');";

// --------------------------
// Private classes
// --------------------------

static private class EjsJim
{
    private Socket jimTCP;
    private DataInputStream bufferInputTCP;
    private DataOutputStream bufferOutputTCP;
    private int SERVICE_PORT;
    private String SERVICE_IP;
    private int EjsId;
    private boolean connected=false;

    public EjsJim(String _mdlFile){

      EjsId = 2005;
      connected=false;

      String sPort = _mdlFile.substring(_mdlFile.lastIndexOf(':') + 1,_mdlFile.lastIndexOf('>'));
      String sAdress = _mdlFile.substring(10, _mdlFile.lastIndexOf(':'));

      if (_mdlFile.startsWith("<matlab:"))
      {
        sPort = _mdlFile.substring(_mdlFile.lastIndexOf(':') + 1,_mdlFile.lastIndexOf('>'));
        sAdress = _mdlFile.substring(8, _mdlFile.lastIndexOf(':'));
      }

      SERVICE_IP = sAdress;
      try {
        SERVICE_PORT = Integer.parseInt(sPort);
      }
      catch (NumberFormatException nfe) {
        System.out.println("Error in Port number:" + nfe); //Gonzalo 060523
      }

      try {
        jimTCP = new java.net.Socket(SERVICE_IP, SERVICE_PORT);
        bufferInputTCP = new DataInputStream(new BufferedInputStream(jimTCP.
            getInputStream()));
        bufferOutputTCP = new DataOutputStream(new BufferedOutputStream(jimTCP.
            getOutputStream()));
        jimTCP.setSoTimeout(10000);
        jimTCP.setTcpNoDelay(true);
        System.out.println("Successful Connection");
        connected=true;
      }
      catch (IOException ioe) {
        System.err.println("Error " + ioe);

      }
      catch (Exception e2) {
        System.err.println("Error " + e2);
      }

      //model = _mdlFile.trim();
      //model = model.substring(model.lastIndexOf('>') + 1);
      //theModel = model.substring(0, model.length() - 4);

      //    openModel();
    }

    public synchronized boolean isConnected(){
      return  connected;
    }

    public synchronized byte[] getModelImage(int Ejs_rmdl_size) {
      byte[] imageData=new byte[Ejs_rmdl_size];
      try {
        bufferOutputTCP.writeInt(EjsId);
        bufferOutputTCP.writeUTF("getModelImage");
        bufferOutputTCP.writeUTF(theMdlFile); // Gonzalo 060519
        //       bufferOutputTCP.writeUTF(archivo);
        bufferOutputTCP.writeInt(Ejs_rmdl_size);
        bufferOutputTCP.flush();
        bufferInputTCP.readFully(imageData);
      }
      catch (Exception e) {
        System.out.println(" getModelImage Exception:" + e);
      }
      return imageData;
    }

    public  synchronized boolean remoteFileExist(String modelPath){
      boolean result=false;
      try {
        bufferOutputTCP.writeInt(EjsId);
        bufferOutputTCP.writeUTF("remoteFileExist");
        bufferOutputTCP.writeUTF(modelPath);
        bufferOutputTCP.flush();
        result=bufferInputTCP.readBoolean();
      }
      catch (Exception e) {
        System.out.println(" fileExist Remote Exception:" + e);
      }
      return result;
    }

   public  synchronized String importModel(String _modelFile,byte[] miNuevoArray){
     String remoteModel=null;
     try {
       bufferOutputTCP.writeInt(EjsId);
       bufferOutputTCP.writeUTF("importModel");
       bufferOutputTCP.writeUTF(_modelFile);
       bufferOutputTCP.writeInt(miNuevoArray.length);
       bufferOutputTCP.write(miNuevoArray);
       bufferOutputTCP.flush();
       remoteModel=bufferInputTCP.readUTF();
     }
     catch (Exception e) {
       System.out.println("importModel Remote Exception:" + e);
     }
     return remoteModel;
   }


     public synchronized void eval(String _command) {
      try {
        bufferOutputTCP.writeInt(EjsId);
        bufferOutputTCP.writeUTF("eval");
        bufferOutputTCP.writeUTF(_command);
        bufferOutputTCP.flush();
      }
      catch (Exception e) {
        System.out.println(" eval Remote Exception:" + e);
      }
    }

    public synchronized double getDouble(String _variable) {
      double valueDouble = 0.0;
      try {
        bufferOutputTCP.writeInt(EjsId);
        bufferOutputTCP.writeUTF("getDouble");
        bufferOutputTCP.writeUTF(_variable);
        bufferOutputTCP.flush();
        valueDouble = bufferInputTCP.readDouble();
      }catch(Exception e){
        System.out.println("Error closing Remote Matlab "+e);
      }

      return (valueDouble);
    }

    public synchronized void quit()  {

      eval("bdclose ('all')");

      try {
        bufferOutputTCP.writeInt(EjsId);
        bufferOutputTCP.writeUTF("exit");
        bufferOutputTCP.flush();
        bufferOutputTCP.close();
        bufferOutputTCP.close();
        jimTCP.close();
      }catch(Exception e){
        System.out.println("Error closing Remote Matlab "+e);
      }

    }





    public synchronized double[][] getDoubleArray2D (String _variable) {
      int _dim1,_dim2;
      double[][] arrayDouble=null;
      try {
        bufferOutputTCP.writeInt(EjsId);
        bufferOutputTCP.writeUTF("getDoubleArray2D");
        bufferOutputTCP.writeUTF(_variable);
        bufferOutputTCP.flush();
        _dim1 = bufferInputTCP.readInt();
        _dim2 = bufferInputTCP.readInt();
        arrayDouble = new double[_dim1][_dim2];
        for (int i = 0; i < _dim1; i++) {
          for (int j = 0; j < _dim2; j++) {
            arrayDouble[i][j] = bufferInputTCP.readDouble();
          }
        }
      }catch(Exception e){
        System.out.println("Error getDoubleArray2D Remote Matlab "+e);
      }
      return(arrayDouble);
    }

    public synchronized String waitForString (String _variable, String _command) {
      String request = "EjsGet"+_variable;
      double[][] arrayD=null;
      if (_command!=null) eval(_command); // 051123 Lausanne. This was before inside the loop
      eval(request+"=double(" + _variable +")" ); // 051123 Lausanne. This was before inside the loop
      int max=10;

      do {
        arrayD = getDoubleArray2D(request);
        max--;
        //      System.out.println("Counter = "+counter);
      } while (arrayD==null && max>0); // A maximum of 10 times
      eval("clear "+request);
      String value[] = double2String(arrayD);
      if (value.length<=0) return null;
      return value[0];
    }


    static public String[] double2String(double[][] d) // Borrowed from JMatLink
  {
      String encodeS[]=new String[d.length];  // String vector

      // for all rows
      for (int n=0; n<d.length; n++){
          byte b[] = new byte[d[n].length];
          // convert row from double to byte
          for (int i=0; i<d[n].length ;i++) b[i]=(byte)d[n][i];

          // convert byte to String
          try { encodeS[n] = new String(b, "UTF8");}
          catch (UnsupportedEncodingException e) { e.printStackTrace(); }
      }
      return encodeS;
  } // end double2String

} //End Class


static private class modelwindow extends Frame {
  private static final long serialVersionUID = 1L;
  Image imagenFuente;
  byte[] imageData;
  int iniAncho;
  int iniAlto;
  // Imagen modificada
//  Image imagenNueva;
  // Valores del borde para el objeto contenedor
  int insetArriba;
  int insetIzqda;

  public modelwindow() {

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        remoteSimulinkImage=null;
        //Rmatlab.eval("set_param (gcs, 'Open','off')");
        dispose();
      }
    });

    //System.out.println("print -s"+mdlFile.substring(0,mdlFile.lastIndexOf('.'))+" -dbitmap 'Ejs_rmdl.bmp'");
    //Rmatlab.eval("print -s"+mdlFile.substring(0,mdlFile.lastIndexOf('.'))+" -dbitmap 'Ejs_rmdl.bmp'"); Gonzalo 060519
    Rmatlab.eval("print -s"+theMdlFile+" -dbitmap '"+theMdlFile+".bmp'");
    Rmatlab.eval("Ejs_rmdl = IMREAD('"+theMdlFile+".bmp','bmp');");
    Rmatlab.eval("IMWRITE(Ejs_rmdl,'"+theMdlFile+".jpg','jpg');");
    Rmatlab.eval("Ejs_rmdl_size=size(Ejs_rmdl,1)*size(Ejs_rmdl,2)");
    int Ejs_rmdl_size=(int)Rmatlab.getDouble("Ejs_rmdl_size");
    imageData=Rmatlab.getModelImage(Ejs_rmdl_size);
    ByteArrayInputStream imStream = new ByteArrayInputStream(imageData);
    try {
       imagenFuente = ImageIO.read(imStream);
    }catch(Exception e){}

    MediaTracker tracker = new MediaTracker( this );
    tracker.addImage( imagenFuente,1 );

    try {
      if( !tracker.waitForID( 1,10000 ) ) {
        System.out.println( "Error loading remote model image" );
        System.exit( 1 );
      }
    } catch( InterruptedException e ) {
      System.out.println( e );
    }

    iniAncho = imagenFuente.getWidth( this );
    iniAlto = imagenFuente.getHeight( this );

    // Se hace visible el Frame
    this.setVisible( true );

    insetArriba = this.getInsets().top;
    insetIzqda = this.getInsets().left;

    this.setSize( insetIzqda+iniAncho,insetArriba+iniAlto) ;
    this.setIconImage(imagenFuente);

  }

  public void paint( Graphics g ) {
    if( imagenFuente != null ) {
      g.drawImage(imagenFuente ,insetIzqda,insetArriba,this );
    }
  }
}

static private class BrowserForSimulinkCR extends JLabel implements ListCellRenderer {
  private static final long serialVersionUID = 1L;
  public BrowserForSimulinkCR() { setOpaque(true); }
  public Component getListCellRendererComponent(JList list,Object value,int index,
                                                boolean isSelected,boolean cellHasFocus) {
    setText(value.toString());
    setForeground(currentBrowser.blocksToDelete.contains(value) ? Color.gray : blockComboDefColor);
    if (isSelected) setBackground(list.getSelectionBackground());
    else setBackground(list.getBackground());
    return this;
  }
}

} // End of class


