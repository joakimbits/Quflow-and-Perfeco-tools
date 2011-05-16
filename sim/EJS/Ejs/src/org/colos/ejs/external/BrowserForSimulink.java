package org.colos.ejs.external;

/**
 * Copyright (Dec 2004) by:
 *   Francisco Esquembre fem@um.es
 *   Gonzalo Farias gfarias@bec.uned.es
 */

import java.util.*;
import java.awt.*;

import javax.swing.*;
import org.colos.ejs.library.external.*;
import org.colos.ejs.osejs.utils.*;
import org.colos.ejs.osejs.edition.Editor;
import org.colos.ejs.unpackaged.EjsJMatLink;
import java.net.URL;

public class BrowserForSimulink extends BrowserForExternal implements java.awt.event.ActionListener {
  static private final String SEPARATOR="_%_";
  static private final String INTEGRATOR_BLOCK="-IB-";
  static private final String EJS_PREFIX="Ejs_";
  static private final ResourceUtil res = new ResourceUtil("Resources");
  static private final String MAIN_BLOCK=res.getString("SimulinkBrowser.MainBlock");
  static private final String MAIN_PARAMETERS="Output=time;Parameters=InitialStep,FixedStep,MaxStep;";

  static private EjsJMatLink matlab=null;
  static private boolean blockComboActive = false, currentBlockIsIntegrator=false;
  static private int id;   // The integer id for the Matlab engine opened
  static private double currentBlockHandle=Double.NaN;
  static private String userDir, returnValue=null, currentBlockName=null, originalBlockName=null;
  static private BrowserForSimulink currentBrowser=null;
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
  public String mdlFile, theMdlFile;
  static String theMdlFileStatic; //Gonzalo 060629
  private Vector<String> blocksInModel,blocksToDelete;
  private boolean fileExists=true;

  static boolean isSimulinkFile (String filename) {
    filename = filename.toLowerCase().trim();
    if (filename.endsWith(".mdl")) return true;
    return false;
  }

  public StringBuffer generateCode (int _type) {
    //if (_type==Editor.GENERATE_JARS_NEEDED) return new StringBuffer("ejsExternal.jar;");
    if (_type==Editor.GENERATE_DECLARATION) return new StringBuffer("org.colos.ejs.external.EjsSimulink.class");
    if (_type==Editor.GENERATE_RESOURCES_NEEDED) return new StringBuffer(mdlFile+";_library/EjsIcon.jpg;");
    return new StringBuffer();
  }

  public boolean fileExists () { return fileExists; }

  public BrowserForSimulink (String _mdlFile, URL _codebase) {
    _mdlFile = _mdlFile.trim().replace('\\','/');
    if (_mdlFile.toLowerCase().startsWith("<matlab")) {
      int index = _mdlFile.indexOf('>');
      if (index>0) _mdlFile = _mdlFile.substring(index+1);
      else _mdlFile = "";
    }
    mdlFile = _mdlFile.trim();
    if (!mdlFile.toLowerCase().endsWith(".mdl")) mdlFile += ".mdl";
    if (mdlFile.lastIndexOf('/')>=0) theMdlFile = mdlFile.substring (mdlFile.lastIndexOf("/")+1, mdlFile.length()-4);
    else theMdlFile = mdlFile.substring (0, mdlFile.length()-4);

    try {
      URL url = new URL(_codebase, mdlFile);
//      System.out.println ("Reading from "+url.toString());
      url.openStream(); // Check if the file exists
    }
    catch (Exception exc) { fileExists = false; }
    blocksToDelete = new Vector<String>(); // Prepare information about deleted blocks
    blocksInModel = null;
    theMdlFileStatic=theMdlFile; //Gonzalo 060629 para usarlo en un contexto estático
  }

  public synchronized void actionPerformed (java.awt.event.ActionEvent event) {
    if (!systemIsOpen()) {  // In case the user closes the system directly
      blockCB.setSelected(false);
      blockCombo.setEnabled(true);
      timer.stop();
      return;
    }
    matlab.engEvalString(id,"EjsCurrentBlock=gcbh"); // ; EjsCurrentBlockName=get_param(EjsCurrentBlock,'name');");
    double blockHandle = matlab.engGetScalar(id,"EjsCurrentBlock");

     //Gonzalo 060629
     //to avoidproblems with submodels and names

    if (currentBlockHandle==blockHandle) { // Check if the user has changed the name of the block
      String blockName = EjsMatlab.waitForString (matlab,id,"EjsCurrentBlockName",
                                                            "EjsCurrentBlockName=[get_param(EjsCurrentBlock,'parent'),'/',strrep(get_param(EjsCurrentBlock,'name'),'/','//')]"); // Gonzalo 060703
      matlab.engEvalString(id,"clear EjsCurrentBlock; clear EjsCurrentBlockName");
      if (blockName==null) return;

      String originalBlockNameR=theMdlFile.toLowerCase()+'/'+originalBlockName; //Gonzalo 060629
//      System.out.println("originalBlockNameR:"+originalBlockNameR);
//      System.out.println("blockName:"+blockName);

      //if (!blockName.equalsI(originalBlockNameR))
      if (!blockName.equalsIgnoreCase(originalBlockNameR)) //Gonzalo 060703
        JOptionPane.showMessageDialog(dialog, res.getString("SimulinkBrowser.ChangeNameNotAllowed"),
                              res.getString("Warning"), JOptionPane.YES_NO_OPTION);
      else{     //Gonzalo 060704   no changes were detected, so all is ok!
        matlab.engEvalString(id, "Ejs_Subsystem=find_system('dirty','on')");
        matlab.engEvalString(id, "for Ejs_index=1:length(Ejs_Subsystem), try, set_param(Ejs_Subsystem{Ejs_index},'dirty','off'), catch, end, end ");
      }
      return;
    }

    matlab.engEvalString(id,"clear EjsCurrentBlock;");
    // a new block has been selected
    currentBlockHandle = blockHandle;
    if (blockHandle!=-1) getBlockVariables ("block=gcb;\n"); // Query the block variables
  }

  public boolean isChanged () { return hasChanged; }

  public void setChanged (boolean _change) { hasChanged = _change; }

// ---------------------------------------
// Extending BrowserForExternal
// ---------------------------------------

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
    if (!loadMatlab()) return null;
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

    // Gonzalo 060629
    matlab.engEvalString(id, "Ejs_Subsystem=find_system('open','on')");
    matlab.engEvalString(id, "for Ejs_index=1:length(Ejs_Subsystem), try, set_param(Ejs_Subsystem{Ejs_index},'open','off'), catch, end, end ");
    //matlab.engEvalString(id, "set_param (gcs, 'Open','Off')");
    //matlab.engEvalString(id, "set_param (gcs, 'Lock','On')");
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
      lines.add("variables.path{end+1,1}='"+theMdlFile+"/"+blockName+"';");
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
        lines.add("variables.path{end+1,1}='"+theMdlFile+"/"+block+"';");
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
        lines.add("variables.path{end+1,1}='"+theMdlFile+"/"+block+"';");
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
        lines.add("variables.path{end+1,1}='"+theMdlFile+"';");
      }
      else {
        String block = oneConnection.substring(begin+1,end);
        if (block.endsWith(INTEGRATOR_BLOCK)) block = block.substring(0,block.length()-INTEGRATOR_BLOCK.length());
        if (blocksInModel!=null && !blocksInModel.contains(block)) continue;
        parameter = oneConnection.substring(0,begin);
        lines.add("variables.path{end+1,1}='"+theMdlFile+"/"+block+"';");
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
  private static void setCurrentValue (String _value) {
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
              getBlockVariables("block='" + currentBrowser.theMdlFile + "/" + blockSelected + "';\n");
            }
            currentBlockHandle=Double.NaN;
          }
        }
        else if (command.equals("showHide")) {
          if (blockCB.isSelected()) {
            if (!systemIsOpen()) openSystem();
            //matlab.engEvalString(id, "set_param (gcs, 'Lock','Off')");

            //Gonzalo 060629
            matlab.engEvalString(id, "set_param ('"+currentBrowser.theMdlFile+"' , 'Open','On')");

            //matlab.engEvalString(id, "set_param (gcs, 'Lock','On')");

            blockCombo.setEnabled(false);
            timer.start();
          }
          else {
            timer.stop();
            blockCombo.setEnabled(true);
//            matlab.engEvalString(id, "set_param (gcs, 'Lock','Off')");
            // Gonzalo 060629
             matlab.engEvalString(id, "Ejs_Subsystem=find_system('open','on')");
             matlab.engEvalString(id, "for Ejs_index=1:length(Ejs_Subsystem), try, set_param(Ejs_Subsystem{Ejs_index},'open','off'), catch, end, end ");
             //matlab.engEvalString(id, "set_param (gcs, 'Open','Off')");
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
          returnValue = finalValue();
          dialog.setVisible(false);
        }
        else if (aCmd.equals("cancel")) {
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
              //matlab.engEvalString(id, "set_param (gcs, 'Open','Off')");
              //Gonzalo 060629
              matlab.engEvalString(id, "Ejs_Subsystem=find_system('open','on')");
              matlab.engEvalString(id, "for Ejs_index=1:length(Ejs_Subsystem), try, set_param(Ejs_Subsystem{Ejs_index},'open','off'), catch, end, end ");
//            matlab.engEvalString(id, "set_param (gcs, 'Lock','On')");
          }
          returnValue = null;
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

  static private boolean loadMatlab () {
    if (matlab!=null) return true;
    userDir = System.getProperty("user.home");
    if (!userDir.endsWith(java.io.File.separator)) userDir += java.io.File.separator;
    userDir = userDir.replace('\\','/');

    JFrame frame = new JFrame(res.getString("SimulinkBrowser.LoadingMatlab"));
    java.awt.Image image = org.opensourcephysics.tools.ResourceLoader.getImage("data/icons/EjsIcon.gif");
    if (image!=null) frame.setIconImage(image);
    frame.getContentPane().setLayout (new java.awt.BorderLayout ());
    frame.getContentPane().add(new JLabel(res.getString("SimulinkBrowser.LoadingMatlab"),SwingConstants.CENTER), BorderLayout.CENTER);
    Dimension size = res.getDimension("Osejs.StartDialogSize");
    Rectangle bounds = org.colos.ejs.library.control.EjsControl.getDefaultScreenBounds();
    frame.setSize(Math.min(size.width, bounds.width),Math.min(size.height, bounds.height));
    frame.validate();
    frame.setLocation(bounds.x+(bounds.width-size.width)/2,bounds.y+(bounds.height-size.width)/2);
    frame.setVisible(true);
    String dir = System.getProperty("user.home").replace('\\','/');
    if (!dir.endsWith("/")) dir += "/";
    if (org.opensourcephysics.display.OSPRuntime.isWindows()) {
//      System.out.println ("Matlab expected at "+dir+"_library/external/JMatLink.dll");
      matlab = new EjsJMatLink(dir+"_library/external/JMatLink.dll");  // create a JMatLink in Windows
      id = matlab.engOpenSingleUse();
    }
    else {
      matlab = new EjsJMatLink(dir+"_library/external/libJMatLink.jnilib"); // create a JMatLink in Unix
      id = matlab.engOpen(System.getProperty("Ejs.MatlabCmd")); // engOpenSingleUse does not work on Unix
    }
    if (matlab==null) JOptionPane.showMessageDialog(null, res.getString("Osejs.File.Error"),res.getString("BrowserForSimulink.ConnectionError"), JOptionPane.INFORMATION_MESSAGE);
    else matlab.engEvalString (id,"cd ('" + userDir + "')");
    frame.dispose();
    return matlab!=null;
  }

  static public void exitMatlab () {
    if (matlab!=null) {
      matlab.engEvalString(id, "Ejs_Subsystem=find_system('type','block_diagram')"); //Gonzalo 060704 close systems without saving
      matlab.engEvalString(id, "for Ejs_index=1:length(Ejs_Subsystem), try, close_system(Ejs_Subsystem{Ejs_index},0), catch, end, end");
      matlab.engClose(id);
    }
    matlab = null;
  }

  static private void openSystem () {

    matlab.engEvalString(id, "open_system ('" + currentBrowser.mdlFile + "')");
    matlab.engEvalString(id, "set_param (gcs, 'SimulationCommand','Stop')");
    matlab.engEvalString(id, "set_param (gcs, 'StopTime','inf')");
    //Gonzalo 060629
    matlab.engEvalString(id, "Ejs_Subsystem=find_system('open','on')");
    matlab.engEvalString(id, "for Ejs_index=1:length(Ejs_Subsystem), try, set_param(Ejs_Subsystem{Ejs_index},'open','off'), catch, end, end ");
  //   matlab.engEvalString(id, "set_param (gcs, 'Open','Off')");
//    matlab.engEvalString(id, "set_param (gcs, 'Lock','On')");
    matlab.engEvalString(id, "variables=['']; blockList=[''];");
  }

  static private void queryModel () {
    if (!systemIsOpen()) openSystem();
//    matlab.engEvalString (id,blockListCommand);
    blockComboActive = false;
    blockCombo.removeAllItems();
    blockCombo.addItem(MAIN_BLOCK);

    EjsMatlab.waitForString (matlab,id,"handle","handle='"+theMdlFileStatic+"'");  // Gonzalo 060629
    String blockList = EjsMatlab.waitForString (matlab,id,"blockList",blockListCommand);
    currentBrowser.blocksInModel = new Vector<String>();
    if (blockList==null) { processList(null); return; }
    int pos = blockList.indexOf("_ejs_");
    while (pos>=0) {
      String piece = correctBlockName(blockList.substring(0,pos));
      blockCombo.addItem(piece);
      currentBrowser.blocksInModel.add(piece);
      blockList = blockList.substring(pos+5);
      pos = blockList.indexOf("_ejs_");
    }
    blockComboActive = true;
  }

  static private boolean systemIsOpen() {
    matlab.engEvalString(id,"clear currentSystem;");
//    matlab.engEvalString(id,"currentSystem=gcs;");
    //String cs = Paco
    EjsMatlab.waitForString (matlab,id,"currentSystem","currentSystem=gcs");
    // Gonzalo 060420
    matlab.engEvalString(id,"clear isOPEN;");
    String isOpen = EjsMatlab.waitForString (matlab,id,"isOPEN","try, isOPEN=get_param('"+currentBrowser.theMdlFile +"','open'),catch,isOPEN='off',end");
    if (isOpen.equalsIgnoreCase("on")) return true;
//  if (currentBrowser.theMdlFile.equalsIgnoreCase(cs)) return true;
    return false;
  }


  static private void getBlockVariables (String prefix) {
    if (!systemIsOpen()) {
      openSystem();
      if (!blockCB.isSelected()) {
//        matlab.engEvalString(id, "set_param (gcs, 'Lock','Off')");
        matlab.engEvalString(id, "set_param (gcs, 'Open','Off')");
  //      matlab.engEvalString(id, "set_param (gcs, 'Lock','On')");
      }
    }
//    matlab.engEvalString (id,prefix+updateCommand);
    processList(EjsMatlab.waitForString (matlab,id,"variables",prefix+updateCommand));
    currentBlockIsIntegrator = (matlab.engGetScalar (id,"isIntegrator")!=0);
  }

  static private String correctBlockName (String _name) {
    String correctName = "";
    StringTokenizer tkn = new StringTokenizer (_name.replace('\n',' '), "/",true);
    while (tkn.hasMoreTokens()) {
      String piece = tkn.nextToken();
//    if (piece.equals("/")) correctName += "//";
      if (piece.equals("/")) correctName += "/";  // Gonzalo 060420
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
    // "handle=gcs;\n" // Gonzalo 060629
    //    + "blocks=get_param(handle,'blocks');\n"
     "blocks=find_system(handle,'type','block');\n" // Gonzalo 060420
    + "for ik=1:length(blocks) aux=blocks{ik}; blocks{ik}=aux(length(handle)+2:end); end;\n" // Gonzalo 060420
    + "blockList=[''];\n"
    + "for i=1:size(blocks,1) blockList=[blockList,char(blocks(i,:)),'_ejs_']; end;\n";

  static private final String updateCommand =
    // "name=get_param(block,'name');\n"
    "name=block;\n" // Gonzalo 060420
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
    + "name=name(length(handle)+2:end);\n"// Gonzalo 060420 para quitar el nombre del modelo en la path
    + "variables=[name,'_ejs_',in_mess,';',out_mess,';',param_mess];"
    + "blocktype=get_param(block,'BlockType'); \n"
    + "isIntegrator = strcmp(deblank(blocktype),'Integrator');";

// --------------------------
// Private classes
// --------------------------

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


