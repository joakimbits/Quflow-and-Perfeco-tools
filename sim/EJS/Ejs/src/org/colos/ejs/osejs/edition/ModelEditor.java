/**
 * The edition package contains generic tools to edit parts
 * of a simulation
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition;

import org.colos.ejs.osejs.edition.variables.ElementsEditor;
import org.colos.ejs.osejs.edition.variables.VariablesEditor;
import org.colos.ejs.osejs.utils.*;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.colos.ejs.library.Animation;
import org.colos.ejs.library.control.value.IntegerValue;
import org.colos.ejs.library.control.value.Value;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

//--------------------

public class ModelEditor implements Editor {
  static private ResourceUtil res = new ResourceUtil ("Resources");
  static private String[] keywords = {"Variables", "Initialization", "Evolution", "Constraints", "Library", "Elements"};
  static private final int DEFAULT_FPS = 20;
  static private String name="";
  static private Insets nullInsets = new Insets(0,0,0,0);

  private Editor[] editors;
  private final TabbedEditor initializationEditor, constraintsEditor;
  private final TabbedEvolutionEditor evolutionEditor;
  private final ElementsEditor elementsEditor;
  private final VariablesEditor variablesEditor;
  private final TabbedLibraryEditor libraryEditor;
  private CardLayout cardLayout = new CardLayout ();
  private JPanel panel = new JPanel (cardLayout);
  private JPanel mainPanel = new JPanel (new BorderLayout());
  private JRadioButton buttons[];
  private Osejs ejs = null;

  private JCheckBox startCheckBox;
  private JSlider fpsSlider;
  private JTextField fpsField,spdField;
  private boolean changed = false , isMerging = false, visible=true;

  public ModelEditor (org.colos.ejs.osejs.Osejs _ejs) {
    ejs = _ejs;
    Color color = InterfaceUtils.color(res.getString("Model.Color"));
    Font font   = InterfaceUtils.font(null,res.getString("Editor.TitleFont"));

    // ----------------------------------
    // The execution control panel for the 'Evolution' panel

    JLabel fpsLabel = new JLabel (res.getString ("Model.Evolution.FpsLabel"),SwingConstants.LEFT);
    fpsLabel.setForeground (color);
    fpsLabel.setFont(font);
    fpsLabel.setToolTipText(res.getString ("Model.Evolution.FpsTooltip"));
    fpsLabel.setHorizontalAlignment (SwingConstants.CENTER);
    fpsLabel.setBorder(new EmptyBorder(0,0,0,2));

    fpsField = new JTextField (4);
    fpsField.setFont(fpsField.getFont().deriveFont(Font.BOLD));
    fpsField.setEditable (true);
    fpsField.setHorizontalAlignment (SwingConstants.RIGHT);
    fpsField.setMargin (nullInsets);
    fpsField.setText(""+DEFAULT_FPS);
    fpsField.addActionListener (new ActionListener() {
      public void actionPerformed(ActionEvent _e) {
        changed = true;
        try {
          int value = Integer.parseInt(fpsField.getText());
          if (value >= Animation.MAXIMUM_FPS) {
            fpsField.setText(res.getString("Model.Evolution.MaximumFPS"));
            fpsSlider.setValue(Animation.MAXIMUM_FPS);
          }
          else if (value <= Animation.MINIMUM_FPS) {
            fpsField.setText(res.getString("Model.Evolution.MinimumFPS"));
            fpsSlider.setValue(Animation.MINIMUM_FPS);
          }
          else fpsSlider.setValue(value);
        }
        catch (Exception exc) {
          fpsField.setText(""+DEFAULT_FPS);
          fpsSlider.setValue(DEFAULT_FPS);
        }
      }
     });

    fpsSlider = new JSlider(SwingConstants.VERTICAL,Animation.MINIMUM_FPS,Animation.MAXIMUM_FPS,DEFAULT_FPS);
    fpsSlider.setInverted (false);
    fpsSlider.setMinorTickSpacing (1);
    fpsSlider.setSnapToTicks(true);
    fpsSlider.setPaintTicks (true);
    fpsSlider.setPaintLabels (true);
    fpsSlider.setBorder(new EmptyBorder(2,5,5,0));
    fpsSlider.setFont(font);
    java.util.Hashtable<Integer,JComponent> table = new java.util.Hashtable<Integer,JComponent> ();
    table.put (new Integer(Animation.MINIMUM_FPS),new JLabel(res.getString("Model.Evolution.MinimumFPS")));
    for (int i=5; i<(Animation.MAXIMUM_FPS-1); i+=5) table.put (new Integer(i),new JLabel(""+i));
    table.put (new Integer(Animation.MAXIMUM_FPS),new JLabel(res.getString("Model.Evolution.MaximumFPS")));
    fpsSlider.setLabelTable (table);

    fpsSlider.addChangeListener (new ChangeListener() {
      public void stateChanged(ChangeEvent _e) {
        changed = true;
        int value = fpsSlider.getValue();
        if      (value==Animation.MAXIMUM_FPS) fpsField.setText(res.getString("Model.Evolution.MaximumFPS"));
        else if (value==Animation.MINIMUM_FPS) fpsField.setText(res.getString("Model.Evolution.MinimumFPS"));
        else fpsField.setText (""+value);
      }
     });

     JLabel spdLabel = new JLabel (res.getString ("Model.Evolution.SpdLabel"),SwingConstants.LEFT);
     spdLabel.setForeground (color);
     spdLabel.setFont(font);
     spdLabel.setToolTipText(res.getString ("Model.Evolution.SpdTooltip"));
     spdLabel.setBorder(new EmptyBorder(0,0,0,2));
     spdLabel.setHorizontalAlignment (SwingConstants.CENTER);

     spdField = new JTextField (4);
     spdField.setFont(spdField.getFont().deriveFont(Font.BOLD));
     spdField.setEditable (true);
     spdField.setHorizontalAlignment (SwingConstants.RIGHT);
     spdField.setMargin (nullInsets);
     spdField.setText("1");
     spdField.addActionListener (new ActionListener() {
       public void actionPerformed(ActionEvent _e) {
         changed = true;
         checkSPD();
       }
     });

     JPanel southLeftPanel = new JPanel (new GridLayout(0,1));
     southLeftPanel.add (fpsLabel);
     southLeftPanel.add (spdLabel);

     JPanel southRightPanel = new JPanel (new GridLayout(0,1));
     southRightPanel.add (fpsField);
     southRightPanel.add (spdField);

     JPanel southTopPanel = new JPanel (new BorderLayout());
     southTopPanel.add(southLeftPanel,BorderLayout.WEST);
     southTopPanel.add(southRightPanel,BorderLayout.CENTER);

    JLabel label = new JLabel (res.getString ("Model.Evolution.FpsLabel1"),SwingConstants.CENTER);
    label.setForeground (color);
    label.setFont(font);

    JPanel labelPanel = new JPanel(new GridLayout(0,1,0,0));
    labelPanel.add(label);

    if (res.getString ("Model.Evolution.FpsLabel2").trim().length()>0) {
      label = new JLabel (res.getString ("Model.Evolution.FpsLabel2"),SwingConstants.CENTER);
      label.setForeground (color);
      label.setFont(font);
      labelPanel.add(label);
    }
    if (res.getString ("Model.Evolution.FpsLabel3").trim().length()>0) {
      label = new JLabel (res.getString ("Model.Evolution.FpsLabel3"),SwingConstants.CENTER);
      label.setForeground (color);
      label.setFont(font);
      labelPanel.add(label);
    }

    startCheckBox = new JCheckBox (res.getString ("Model.Evolution.Autostart"),true);
    startCheckBox.setForeground (color);
    startCheckBox.setFont(font);
    startCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
    startCheckBox.setMargin(nullInsets);
    startCheckBox.setToolTipText(res.getString ("Model.Evolution.AutostartTooltip"));
    startCheckBox.addItemListener (new ItemListener() {
      public void itemStateChanged(ItemEvent _e) { changed = true; }
     });

    JPanel southPanel = new JPanel(new BorderLayout());
    southPanel.add(southTopPanel,BorderLayout.NORTH);
    southPanel.add(startCheckBox,BorderLayout.SOUTH);

    JPanel leftPanel = new JPanel (new BorderLayout());
    leftPanel.add (fpsSlider,BorderLayout.CENTER);
    leftPanel.add (labelPanel,BorderLayout.NORTH);
    leftPanel.add (southPanel,BorderLayout.SOUTH);
    leftPanel.setBorder (new EmptyBorder(3,3,5,3));

    JPanel evolutionPanel = new JPanel (new BorderLayout());
    evolutionPanel.add(leftPanel,BorderLayout.WEST);

    // --- Now, create the editors
    editors = new Editor[6];
    editors[0] = variablesEditor = new VariablesEditor (_ejs);
    editors[1] = initializationEditor = new TabbedEditor (_ejs, Editor.CODE_EDITOR,"Model.Initialization");
    editors[2] = evolutionEditor = new TabbedEvolutionEditor (_ejs);
    editors[3] = constraintsEditor = new TabbedEditor (_ejs, Editor.CODE_EDITOR,"Model.Constraints");
    editors[4] = libraryEditor = new TabbedLibraryEditor (_ejs);
    editors[5] = elementsEditor = new ElementsEditor (_ejs);

    ActionListener al = new ActionListener() {
      public void actionPerformed (java.awt.event.ActionEvent evt) {
        cardLayout.show (panel,evt.getActionCommand());
      }
    };
    Border buttonsBorder = BorderFactory.createEmptyBorder(0,6,0,6);
    JPanel toolbar = new JPanel (new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 0.5;
    gbc.gridy = 0;
    Insets inset = new java.awt.Insets(0,3,0,3);
    
    font = InterfaceUtils.font(null,res.getString("Model.TitleFont"));
    buttons = MenuUtils.createRadioGroup (keywords,"Model.",al,_ejs.getOptions().useShortHeaders());
    for (int i=0; i<buttons.length; i++) {
      buttons[i].setBorder(buttonsBorder);
      gbc.gridx = i;
      buttons[i].setFont(font);
      buttons[i].setMargin(inset);
      toolbar.add (buttons[i],gbc);
    }

    JPanel toolbarPanel = new JPanel(new BorderLayout());
    toolbarPanel.add(toolbar,BorderLayout.WEST);

    panel.add (variablesEditor.getComponent(),keywords[0]);
    panel.add (initializationEditor.getComponent(),keywords[1]);
    evolutionPanel.add (evolutionEditor.getComponent(),BorderLayout.CENTER);
    panel.add (evolutionPanel,keywords[2]);
    panel.add (constraintsEditor.getComponent(),keywords[3]);
    JPanel libraryPanel = new JPanel (new BorderLayout());
    libraryPanel.add(libraryEditor.getComponent(),BorderLayout.CENTER);
    //libraryPanel.add(jarsPanel,BorderLayout.SOUTH);
    panel.add (libraryPanel,keywords[4]);
    panel.add (elementsEditor.getComponent(),keywords[5]);

    cardLayout.show (panel,keywords[0]);
    buttons[0].setSelected(true);

    mainPanel.add (toolbarPanel,BorderLayout.NORTH);
    mainPanel.add (panel  ,BorderLayout.CENTER);
    setName("Model");
  }

  public ElementsEditor getElementsEditor() { return this.elementsEditor; }
  public VariablesEditor getVariablesEditor() { return this.variablesEditor; }
  public TabbedEditor getInitializationEditor() { return this.initializationEditor; }
  public TabbedEvolutionEditor getEvolutionEditor() { return this.evolutionEditor; }
  public TabbedEditor getConstraintsEditor() { return this.constraintsEditor; }
  public TabbedLibraryEditor getLibraryEditor() { return this.libraryEditor; }

  public void checkSyntax() {
    checkSPD();
  }
  
  /**
   * Checks for a valid value of SPD
   * @return true if the value is a constant
   */
  public boolean checkSPD() {
    String variable = spdField.getText().trim();
    try {
      int value = Integer.parseInt(variable);
      if (value <= 1) spdField.setText("1");
      spdField.setBackground(Color.WHITE);
      return true;
    }
    catch (Exception exc) { }
    if (variablesEditor.isVariableDefined(variable,"int")) spdField.setBackground(Color.WHITE);
    else {
      Value value = variablesEditor.checkExpression(variable,"int");
      if (value instanceof IntegerValue) spdField.setBackground(Color.WHITE);
      else spdField.setBackground(TabbedEditor.ERROR_COLOR);
    }
    return false;
  }

  public void refreshHeaders(boolean _useShortForm) {
    for (int i=0; i<buttons.length; i++) {
      String key=buttons[i].getActionCommand();
      String text=null;
      if (_useShortForm) text = res.getOptionalString("Model."+key+".Short");
      if (text==null) text = res.getString("Model."+key);
      if (text==null) text = key;
      buttons[i].setText(text);
    }
  }
  
  
  // Start of implementation of Editor

  public java.util.List<SearchResult> search (String _info, String _searchString, int _mode) {
    java.util.List<SearchResult> list = new ArrayList<SearchResult>();
    for (int i=0; i<editors.length; i++) list.addAll(editors[i].search(_info,_searchString,_mode));
    return list;
  }

  public void setName (String _name) {
    name = _name;
    for (int i=0; i<editors.length; i++) editors[i].setName(_name+"."+keywords[i]);
  }

  public String getName() { return name; }

  public void clear () {
    for (int i=0; i<editors.length; i++) editors[i].clear();
    fpsSlider.setValue(DEFAULT_FPS);
    fpsField.setText(""+DEFAULT_FPS);
    spdField.setText("1");
    spdField.setBackground(Color.WHITE);
    startCheckBox.setSelected(true);
    setChanged(false);
    isMerging = false;
  }

  public Component getComponent () { return mainPanel; }

  public void setColor (Color _color) {
    for (int i=0; i<editors.length; i++) editors[i].setColor(_color);
    for (int i=0; i<buttons.length; i++) buttons[i].setForeground(_color);
  }

  public void setFont (Font _font) {
    for (int i=0; i<editors.length; i++) editors[i].setFont(_font);
  }

  public void setVisible (boolean _visible) { visible = _visible; }

  public boolean isVisible () { return visible; }

  public boolean isChanged () {
    if (changed) return true;
    for (int i=0; i<editors.length; i++) if (editors[i].isChanged()) return true;
    return false;
  }

  public void setChanged (boolean _ch) {
    changed = _ch;
    for (int i=0; i<editors.length; i++) editors[i].setChanged(_ch);
  }

  public boolean isActive () {
    for (int i=0; i<editors.length; i++) if (!editors[i].isActive()) return false;
    return true;
  }

  public void setActive (boolean _active) {
    for (int i=0; i<editors.length; i++) editors[i].setActive(_active);
  }

  public String getStepsPerDisplay() { return spdField.getText(); }
  
  public StringBuffer generateCode (int _type, String _info) {
    StringBuffer code = new StringBuffer();
    if (_type==Editor.GENERATE_SIMULATION_STATE) {
      boolean autoplay = startCheckBox.isSelected() && evolutionEditor.getActivePageCount()>0;
      code.append("    setFPS("+fpsSlider.getValue()+");\n");
      code.append("    setStepsPerDisplay(_model._getPreferredStepsPerDisplay()); \n");
      code.append("    if (_allowAutoplay) { setAutoplay("+autoplay+"); reset(); }\n");
      code.append("    else { reset(); setAutoplay("+autoplay+"); }\n");
    }
    else if (_type==Editor.GENERATE_RESOURCES_NEEDED) {
      code.append(ejs.getSimInfoEditor().getJarsStringBuffer());
      for (int i=0; i<editors.length; i++) code.append(editors[i].generateCode(_type,""));
    }

    else for (int i=0; i<editors.length; i++) code.append(editors[i].generateCode(_type,""));
    return code;
  }

  public StringBuffer saveStringBuffer () {
    StringBuffer save = new StringBuffer (
      "<"+name+".FramesPerSecond>"+fpsSlider.getValue()+"</"+name+".FramesPerSecond>\n" +
      "<"+name+".StepsPerDisplay>"+spdField.getText()+"</"+name+".StepsPerDisplay>\n" +
      "<"+name+".Autostart>"+startCheckBox.isSelected()+"</"+name+".Autostart>\n");
    for (int i=0; i<editors.length; i++)
      save.append("<"+name+"."+keywords[i]+">\n"+editors[i].saveStringBuffer()+"</"+name+"."+keywords[i]+">\n");
    return save;
  }

  public void setMerging (boolean _value) { isMerging = _value; }

  public void readString (String _input) {
    int fps = 0;
    if (!isMerging) {
      try {
        fps = Integer.parseInt(OsejsCommon.getPiece(_input,
         "<"+name+".FramesPerSecond>","</"+name+".FramesPerSecond>",false));
      }
      catch (NumberFormatException _e) { fps = DEFAULT_FPS; }
      fpsSlider.setValue(fps);
      if (fps>=Animation.MAXIMUM_FPS) fpsField.setText(res.getString("Model.Evolution.MaximumFPS"));
      else if (fps <= Animation.MINIMUM_FPS) fpsField.setText(res.getString("Model.Evolution.MinimumFPS"));
      else fpsField.setText(""+fps);

      String spd = OsejsCommon.getPiece(_input,"<"+name+".StepsPerDisplay>","</"+name+".StepsPerDisplay>",false);
      if (spd!=null) spdField.setText(spd);
      else spdField.setText("1");

      String start = OsejsCommon.getPiece(_input,"<"+name+".Autostart>","</"+name+".Autostart>",false);
      if ("false".equals(start)) startCheckBox.setSelected(false);
      else startCheckBox.setSelected(true);
    }
    
    // --- Backwards compatibility. Libraries and imports will be saved on SimInfoEditor
    // Extract the additional libraries, if any
    String libs = OsejsCommon.getPiece(_input,"<"+name+".AdditionalLibraries>","</"+name+".AdditionalLibraries>",false);
    if (libs!=null) {
      Set<String> libsToAdd = new HashSet<String>();
      int begin = libs.indexOf("<Library>");
      while (begin>=0) {
        int end = libs.indexOf("</Library>\n");
        String oneLib = libs.substring(begin+9,end);
        libsToAdd.add(oneLib);
        libs = libs.substring(end+11);
        begin = libs.indexOf("<Library>");
      }
      if (!libsToAdd.isEmpty()) OsejsCommon.warnAboutFiles(ejs.getMainPanel(),libsToAdd,"SimInfoEditor.RequiredFileNotFound");
    }
    // Extract the import statements, if any
    String imports = OsejsCommon.getPiece(_input,"<"+name+".ImportStatements>","</"+name+".ImportStatements>",false);
    if (imports!=null) {
      int begin = imports.indexOf("<Import>");
      while (begin>=0) {
        int end = imports.indexOf("</Import>\n");
        String oneImport = imports.substring(begin+8,end);
        ejs.getSimInfoEditor().addToImportsCombo(oneImport);
        imports = imports.substring(end+10);
        begin = imports.indexOf("<Import>");
      }
      ejs.getSimInfoEditor().updateImportStatements();
    }
    // End of backwards compatibility
    
    // Pass over the rest of the work to the editors
    for (int i=0; i<editors.length; i++) {
      int begin = _input.indexOf("<"+name+"."+keywords[i]+">\n");
      if (begin<0) continue;
      int end = _input.indexOf("</"+name+"."+keywords[i]+">\n");
      editors[i].readString(_input.substring(begin+keywords[i].length()+name.length()+4,end));
    }
    setChanged(false);
  }

// ------ End of implementation of Editor

// ------ Other public methods

  /**
   * Make sure a given panel is shown
   */
  public void showPanel (String subpanelStr) {
   for (int i=0; i<keywords.length; i++) {
      if (keywords[i].equals(subpanelStr)) {
        cardLayout.show(panel, keywords[i]);
        buttons[i].setSelected(true);
        return;
      }
    }
  }

  /**
   * Returns the list of all variables
   */
  public java.util.List<String> getAllVariables () {
    ArrayList<String> list = new ArrayList<String>();
    String listStr = variablesEditor.generateCode(Editor.GENERATE_LIST_VARIABLES,"").toString();
    StringTokenizer lineTkn = new StringTokenizer (listStr,"\n");
    while (lineTkn.hasMoreTokens()) {
      String line = lineTkn.nextToken();
      StringTokenizer tkn = new StringTokenizer (line,":");
      String type = tkn.nextToken().trim();
      list.add(tkn.nextToken().trim()+":"+type);
    }
    return list;
  }

  /**
   * Returns the list of variables of one of the allowed types
   */
  private java.util.List<String> getVariables (String _types) {
    java.util.List<String> possibleTypes = new ArrayList<String>();
    if (_types!=null) {
      StringTokenizer typeTkn = new StringTokenizer(_types,"|");
      while (typeTkn.hasMoreTokens()) possibleTypes.add(typeTkn.nextToken());
    }
    java.util.List<String> list = new ArrayList<String>();
    String listStr = variablesEditor.generateCode(Editor.GENERATE_LIST_VARIABLES,"").toString();
    StringTokenizer lineTkn = new StringTokenizer (listStr,"\n");
    while (lineTkn.hasMoreTokens()) {
      String line = lineTkn.nextToken();
      StringTokenizer tkn = new StringTokenizer (line,":");
      String type = tkn.nextToken().trim();
      for (String aType : possibleTypes) if (aType.equals(type)) { list.add(tkn.nextToken().trim()); break; }
    }
    return list;
  }

  public boolean isVariableDefinedOfType (String _variable, String _types) {
    for (String aVariable : getVariables(_types)) {
      if (_variable.equals(aVariable)) return true;
    }
    return false;
  }

  public java.util.List<String> getCustomMethods (String _type) {
    ArrayList<String> list = new ArrayList<String>();
    String txt = libraryEditor.generateCode(Editor.GENERATE_LIST_ACTIONS,_type).toString();
    StringTokenizer tkn = new StringTokenizer (txt,"\n");
    while (tkn.hasMoreTokens()) list.add(tkn.nextToken());
    return list;
  }

  /*
  public Vector getActions () {
    Vector actions= new Vector();
    for (Enumeration e = libraryEditor.getPageEnumeration(); e.hasMoreElements();)
      actions.addAll(((LibraryEditor) e.nextElement()).getActions());
    return actions;
  }
*/


} // end of class
