/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * Revised Feb 2006 F. Esquembre
 */

package org.colos.ejs.osejs;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;

import org.colos.ejs.library.control.ControlElement;
import org.colos.ejs.library.control.swing.ControlWindow;
import org.colos.ejs.library.control.EjsControl;
import org.colos.ejs.osejs.utils.*;
import org.colos.ejs.control.editors.*;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.ResourceLoader;

public class EjsOptions {
  static public final int GENERATE_LEFT_FRAME   = 0;
  static public final int GENERATE_ONE_PAGE     = 1;
  static public final int GENERATE_NONE         = 2;
  static public final int GENERATE_TOP_FRAME    = 3;

  static private final int CENTER  = 0;
  static private final int TOPLEFT = 1;
  static private final int CUSTOM  = 2;

  static private final String[] targetVMOptions = { "1.5", "1.6" };
    
  static private ResourceUtil res = new ResourceUtil("Resources");
  static private ResourceUtil sysRes = new ResourceUtil("SystemResources");

  static private int def_targetVM=0;
  static private final Font def_font=InterfaceUtils.font(null,res.getString("Osejs.MonospacedFont"));

  private Osejs ejs;

  private JDialog dialog;
  private JTabbedPane tabbedPanel;
  private JPanel authorInfoTopPanel;
  private JRadioButton generateHtmlRB, generateHtmlTB, generateOneHtmlRB, generateNoHtmlRB;
  private JCheckBox removeJavaFileCB, generateJNLPCB,//eMersionCB,
                    experimentsCB,runAsAppletCB,includeModelCB,saveWhenRunningCB, showPropertyErrorsCB,
                    dlCompadreCB, dlMasterCB, deprecatedElementsCB, checkOnExitCB, shortHeadersCB;
  private JRadioButton centerRB, topLeftRB, customRB, defaultSizeRB, currentSizeRB;
  private JTextField loadFileTF,jnlpURLTF, vmOptionsTF, borderTF; // eMersionURLTF, 
  private DefaultListModel dlListModel;
  private javax.swing.text.JTextComponent authorTF, affiliationTF, contactTF;
  private JComboBox targetVMCB,lookandfeelCombo;
  private int position=CENTER;
  private Point customLocation=new Point (0,0);
  private Dimension customSize=null;
  private String currentFont="<default>";
  private JComponent borderColorLabel, borderTitleColorLabel;

  private JTextField htmlBodyTF;///FKH
  private String lastXMLFilePath=null;
  private Dimension htmlPanelSize=new Dimension(600,400);

// ---- Methods for the public options

  //public String loadFilename()            { return loadFileTF.getText().trim(); }
  public boolean experimentsEnabled() { return experimentsCB.isSelected(); }
  public boolean runAsApplet() { return false; } // return runAsAppletCB.isSelected() && !generateNoHtmlRB.isSelected(); }
  public int generateHtml()  {
    if (generateHtmlRB.isSelected())    return GENERATE_LEFT_FRAME;
    if (generateHtmlTB.isSelected())    return GENERATE_TOP_FRAME;
    if (generateOneHtmlRB.isSelected()) return GENERATE_ONE_PAGE;
    return GENERATE_NONE;
  }
  public String getHtmlBody()      { return htmlBodyTF.getText().trim(); }
  public boolean generateJNLP()    { return false; } // generateJNLPCB.isSelected(); }
  public String jnlpURL()          { return jnlpURLTF.getText().trim(); }
//  public boolean eMersionEnabled() { return eMersionCB.isSelected(); }
//  public String eMersionGetURL ()  { return eMersionURLTF.getText().trim(); }
  public String getAuthor ()  { return authorTF.getText().trim(); }
  public String getAffiliation ()  { return affiliationTF.getText().trim(); }
  public String getContact ()  { return contactTF.getText().trim(); }

  public boolean saveWhenRunning()   { return saveWhenRunningCB.isSelected(); }
  public boolean showPropertyErrors()   { return showPropertyErrorsCB.isSelected(); }
  public void setShowPropertyErrors(boolean show)   { showPropertyErrorsCB.setSelected(show); }

  public boolean removeJavaFile()  { return removeJavaFileCB.isSelected(); }
  public String targetVM()         { return targetVMCB.getSelectedItem()==null ? targetVMOptions[0] : targetVMCB.getSelectedItem().toString(); }
  public String vmOptions()        { return vmOptionsTF.getText().trim(); }
  public boolean includeModel() { return includeModelCB.isSelected(); }
  public String lastXMLFilePath () { return lastXMLFilePath; }
  public Dimension getHtmlPanelSize() { return htmlPanelSize; }
  public NamedLookAndFeel getRunningLookAndFeel()  { return (NamedLookAndFeel) lookandfeelCombo.getSelectedItem(); }
  
  public boolean useCompadreDL() { return dlCompadreCB.isSelected(); }
  
  public boolean useShortHeaders()   { return shortHeadersCB.isSelected(); }
  
  public boolean showDeprecatedElements() { return deprecatedElementsCB.isSelected(); }

  public boolean checkOnExit() { return checkOnExitCB.isSelected(); }
  
  public java.util.List<String> getDigitalLibraries() {
    java.util.List<String> list = new ArrayList<String>();
    if (dlMasterCB.isSelected()) list.addAll(getDLMasterList());
    for (int i=0, n= dlListModel.getSize(); i<n; i++) {
      String dlib = dlListModel.getElementAt(i).toString(); 
      if (!list.contains(dlib)) list.add(dlib);
    }
    return list;
  }
  
  private java.util.List<String> getDLMasterList() {
    java.util.List<String> list = new ArrayList<String>();
    try {
      String dlListURL = "http://"+OsejsCommon.EJS_SERVER+"/Site/EjsDLs?action=source";
      java.net.URL url = new java.net.URL(dlListURL);
      Reader reader = new InputStreamReader(url.openStream());
      LineNumberReader l = new LineNumberReader(reader);
      String sl = l.readLine();
      while (sl != null) {
        String dlib = sl.trim();
        if (dlib.length()>0) list.add(dlib);
        sl = l.readLine(); 
      }
      reader.close();
    } 
    catch (Exception exc) {
      JOptionPane.showMessageDialog(ejs.getMainPanel(),res.getString("DigitalLibrary.ServerNotAvailable"),
          res.getString("Osejs.File.Error"),JOptionPane.ERROR_MESSAGE);
    }
    return list;
  }
  
  
  /**
   * Whether the generated simulation will offer DataTool and FourierTool
   *
  public boolean addToolsForData() { return dataToolCB.isSelected(); }
  /**
   * Whether the generated simulation will offer a TranslatorTool
   *
  public boolean addTranslatorTool() { return translatorToolCB.isSelected(); }
  /**
   * Whether the generated simulation will offer capture tools
   *
  public boolean addCaptureTools() { return captureToolCB.isSelected(); }
  
  */
  
  private void setBorderWidth(Color _color, Color _textColor) {
    int border;
    try { border = Integer.parseInt(borderTF.getText()); }
    catch (Exception exc) { border = 2; }
    EjsControl.setBorderWidthAroundWindows(border);
    EjsControl.setBorderTitleAroundWindows(res.getString("EjsOptions.Preview"));
    EjsControl.setBorderColorAroundWindows(_color);
    EjsControl.setBorderTitleColorAroundWindows(_textColor);
    try {
      EjsControl control = ejs.getViewEditor().getTree().getControl();
      for (ControlElement element : control.getElements()) {
        if (element instanceof ControlWindow) ((ControlWindow) element).adjustBorder();
      }
    } catch (Exception exc) {}
    
  }

  private void placeEjsFrame (int _pos) { position = _pos; placeFrame(ejs.getMainFrame()); }

  public void sizeFrame(JFrame frame) {
    if (frame==null) return;
    if (customSize!=null) {
      frame.setSize(customSize);
      frame.validate();
    }
  }

  /**
   * Places Ejs' main frame in the position selected by the options
   * @param frame JFrame
   */
  public void placeFrame(JFrame frame) {
    if (frame==null) return;
    Rectangle bounds = OsejsCommon.getScreenBounds(frame);
    switch (position) {
      case TOPLEFT : frame.setLocation(bounds.x+0,bounds.y+0); break;
      case CUSTOM :
        int x = customLocation.x, y = customLocation.y;
        Dimension w = frame.getSize();
//        Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
//        if (x+w.width>d.width)   x = d.width-w.width;
//        if (y+w.height>d.height) y = d.height-w.height;
        if (x+w.width>bounds.width) x = bounds.width-w.width;
        if (y+w.height>bounds.height) y = bounds.height-w.height;
        frame.setLocation(bounds.x+x,bounds.y+y);
        break;
      default :
      case CENTER :
//        d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
//        frame.setLocation((d.width  - frame.getSize().width)/2,(d.height - frame.getSize().height)/2);
        frame.setLocation(bounds.x + (bounds.width - frame.getSize().width)/2, bounds.y + (bounds.height - frame.getSize().height)/2);
        break;
    }
  }

  /**
   * Constructor
   * @param _ejs Osejs
   */
  public EjsOptions (Osejs _ejs, JFrame _frame) {
    ejs = _ejs;

    EmptyBorder border0002 = new EmptyBorder(0,0,0,2);

    // ------------------ Listeners for all buttons and controls
    java.awt.event.MouseAdapter mouseListener =  new java.awt.event.MouseAdapter () {
      public void mousePressed (java.awt.event.MouseEvent evt) {
        AbstractButton button = (AbstractButton) (evt.getSource());
        String aCmd = button.getActionCommand();
        if (aCmd.equals("ok")) {
          dialog.setVisible (false);
        }
        else if (aCmd.equals("default"))  {
          // Location
          centerRB.setSelected(true);
          placeEjsFrame(CENTER);
          // Size
          defaultSizeRB.setSelected(true);
          // Font
          currentFont = "<default>";
          ejs.setFont(def_font);
          // Base file
          loadFileTF.setText("");
          // Show experiments panel
          experimentsCB.setSelected(false);
          // Create HTML pages
          generateHtmlRB.setSelected(true);
          // HTML body
          htmlBodyTF.setText("");
          // Create JNLP
          generateJNLPCB.setSelected(false);
          jnlpURLTF.setText("http://localhost/jaws");
          // Create HTML for eMersion
//          eMersionCB.setSelected(false);
//          eMersionURLTF.setText("http://localhost:8080");
          // Save XML file previous to running
          saveWhenRunningCB.setSelected(true);
          // Save XML file previous to running
          showPropertyErrorsCB.setSelected(false);
          // Include Model
          includeModelCB.setSelected(true);
          // Run as applet
          runAsAppletCB.setSelected(false);
          // Delete Java files
          removeJavaFileCB.setSelected(false);
          // Select VM
          targetVMCB.setSelectedIndex(def_targetVM);
          // Select LaF
          lookandfeelCombo.setSelectedItem(NamedLookAndFeel.getLookAndFeel(OSPRuntime.DEFAULT_LF));
          // Virtual Machine options
          vmOptionsTF.setText("");
          // Border width around windows
          borderTF.setText("2");
          borderColorLabel.setBackground(Color.RED);
          borderColorLabel.setForeground(Color.RED);
          borderTitleColorLabel.setBackground(Color.BLACK);
          borderTitleColorLabel.setForeground(Color.BLACK);
          setBorderWidth(Color.RED,Color.BLACK);
          // digital libraries
          dlCompadreCB.setSelected(true);
          dlMasterCB.setSelected(true);
          // show deprecated elements
          shortHeadersCB.setSelected(false);
          deprecatedElementsCB.setSelected(false);
          checkOnExitCB.setSelected(true);
        }
        // Location of Ejs frame
        else if (aCmd.equals("center"))  placeEjsFrame(CENTER);
        else if (aCmd.equals("topLeft")) placeEjsFrame(TOPLEFT);
        else if (aCmd.equals("custom")) position = CUSTOM;
        else if (aCmd.equals("defaultSize")) ejs.getMainFrame().pack();
        else if (aCmd.equals("customSize")) sizeFrame(ejs.getMainFrame());
        // Font for Ejs' panels
        else if (aCmd.equals("font")) {
          EditorForFont.edit(dialog);
          Font font = EditorForFont.getFont();
          if (font!=null) {
            currentFont = EditorForFont.getFontName();
            ejs.setFont (font);
          }
        }
        else if (aCmd.equals("defaultFont")) {
          currentFont = "<default>";
          ejs.setFont (def_font);
        }
        // Base file
        else if (aCmd.equals("SelectLoadFile")) {
          String file = EditorForFile.edit (ejs,loadFileTF,"Ejsfile"); 
          if (file!=null) loadFileTF.setText(file);
        }
      }
    };

    java.awt.event.ItemListener itemListener = new java.awt.event.ItemListener() {
      public void itemStateChanged (java.awt.event.ItemEvent _e) {
        if (_e.getSource()==experimentsCB) {
          ejs.forceCompilation();  // Re-generate when running
          ejs.showExperimentsPanel(experimentsCB.isSelected());
        }
        else ejs.forceCompilation(); // Re-generate when running
      }
    };

    // --------------------- Main Buttons

    JButton okButton = new JButton (res.getString("EditorFor.Ok"));
    okButton.setActionCommand ("ok");
    okButton.addMouseListener (mouseListener);

    JButton defaultButton = new JButton (res.getString("EditorFor.Default"));
    defaultButton.setActionCommand ("default");
    defaultButton.addMouseListener (mouseListener);

    JPanel buttonPanel = new JPanel (new FlowLayout(FlowLayout.CENTER));
    buttonPanel.add (okButton);
    buttonPanel.add (defaultButton);

    // ------------- Options ---------------

    // ---------------------------
    // Position of EJS at start-up
    // ---------------------------

    JLabel positionLabel = new JLabel(res.getString("EjsOptions.Position"));

    centerRB  = new JRadioButton(res.getString("EjsOptions.center"),true);
    centerRB.setRequestFocusEnabled(false);
    centerRB.setActionCommand ("center");
    centerRB.addMouseListener (mouseListener);

    topLeftRB = new JRadioButton(res.getString("EjsOptions.topLeft"),false);
    topLeftRB.setRequestFocusEnabled(false);
    topLeftRB.setActionCommand ("topLeft");
    topLeftRB.addMouseListener (mouseListener);

    customRB = new JRadioButton(res.getString("EjsOptions.custom"),false);
    customRB.setRequestFocusEnabled(false);
    customRB.setActionCommand ("custom");
    customRB.addMouseListener (mouseListener);

    ButtonGroup group2 = new ButtonGroup();
    group2.add(centerRB);
    group2.add(topLeftRB);
    group2.add(customRB);

    FlowLayout flow1 = new FlowLayout(FlowLayout.CENTER);
    flow1.setVgap(0);
    JPanel positionButtonPanel = new JPanel (flow1); //new FlowLayout(FlowLayout.CENTER));
//    positionButtonPanel.setBorder(new EmptyBorder(0,0,0,0));
    positionButtonPanel.add(centerRB);
    positionButtonPanel.add(topLeftRB);
    positionButtonPanel.add(customRB);

    defaultSizeRB = new JRadioButton(res.getString("EjsOptions.defaultSize"),false);
    defaultSizeRB.setRequestFocusEnabled(true);
    defaultSizeRB.setSelected(true);
    defaultSizeRB.setActionCommand ("defaultSize");
    defaultSizeRB.addMouseListener (mouseListener);

    currentSizeRB = new JRadioButton(res.getString("EjsOptions.currentSize"),false);
    currentSizeRB.setRequestFocusEnabled(true);
    currentSizeRB.setActionCommand ("customSize");
    currentSizeRB.addMouseListener (mouseListener);

    ButtonGroup group3 = new ButtonGroup();
    group3.add(defaultSizeRB);
    group3.add(currentSizeRB);

    FlowLayout flow2 = new FlowLayout(FlowLayout.CENTER);
    flow2.setVgap(0);
    JPanel sizeButtonPanel = new JPanel (flow2); //new FlowLayout(FlowLayout.CENTER));
//    sizeButtonPanel.setBorder(new EmptyBorder(0,0,0,0));
    sizeButtonPanel.add(defaultSizeRB);
    sizeButtonPanel.add(currentSizeRB);

    // ---------------------------
    // Default Font
    // ---------------------------

    JLabel fontLabel = new JLabel(res.getString("Osejs.Icon.Font"));
    fontLabel.setBorder(new EmptyBorder(0,0,0,5));
    fontLabel.setHorizontalAlignment(SwingConstants.LEFT);

    ImageIcon icon = org.opensourcephysics.tools.ResourceLoader.getIcon(sysRes.getString("Osejs.Icon.Font"));
    JButton fontCB = new JButton(icon);
    //fontCB.setRequestFocusEnabled(false);
    fontCB.setMargin(new Insets (0,0,0,0));
    fontCB.setActionCommand("font");
    fontCB.addMouseListener (mouseListener);

    JButton defaultFontButton = new JButton (res.getString("EjsOptions.ChooseFont"));
    defaultFontButton.setActionCommand ("defaultFont");
    defaultFontButton.addMouseListener (mouseListener);

    JPanel fontPanel = new JPanel (new FlowLayout(FlowLayout.LEFT));
    fontPanel.add (fontCB);
    fontPanel.add (fontLabel);
    fontPanel.add (defaultFontButton);

    // ---------------------------
    // Check on exit
    // ---------------------------
    
    checkOnExitCB = new JCheckBox(res.getString("EjsOptions.CheckOnExit"));
    checkOnExitCB.setSelected(true);

    JPanel checkOnExitPanel=new JPanel (new BorderLayout());
    checkOnExitPanel.add(checkOnExitCB,BorderLayout.WEST);

    // ---------------------------
    // Deprecated elements
    // ---------------------------
    
    shortHeadersCB = new JCheckBox(res.getString("EjsOptions.UseShortLabels"));
    shortHeadersCB.setSelected(false);
    shortHeadersCB.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent _evt) {
        ejs.getModelEditor().refreshHeaders(shortHeadersCB.isSelected());
      }
    });

    JPanel shortHeadersPanel=new JPanel (new BorderLayout());
    shortHeadersPanel.add(shortHeadersCB,BorderLayout.WEST);
    
    // ---------------------------
    // Deprecated elements
    // ---------------------------
    
    deprecatedElementsCB = new JCheckBox(res.getString("EjsOptions.ShowDeprecatedElements"));
    deprecatedElementsCB.setSelected(false);
    deprecatedElementsCB.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent _evt) {
        ejs.getViewEditor().getCreationPanel().showDeprecatedElements(deprecatedElementsCB.isSelected());
      }
    });

    JPanel deprecatedElementsPanel=new JPanel (new BorderLayout());
    deprecatedElementsPanel.add(deprecatedElementsCB,BorderLayout.WEST);
    
    
    // ---------------------------
    // Preview border
    // ---------------------------
    
    JLabel borderLabel = new JLabel(res.getString ("EjsOptions.BorderWidth"));
    borderLabel.setBorder(border0002);

    borderColorLabel = new JLabel(" ... ");
//    borderColorLabel.setBorder(new LineBorder (Color.BLACK));
    borderColorLabel.setOpaque(true);
    borderColorLabel.setBackground(Color.RED);
    borderColorLabel.setForeground(Color.RED);
    borderColorLabel.setToolTipText(res.getString("EjsOptions.BorderColor"));
//    borderColorLabel.setPreferredSize(new Dimension(20,20));
    borderColorLabel.addMouseListener(new MouseAdapter(){
      public void mouseClicked(MouseEvent arg0) {
        Color color = EditorForColor.editColor (borderColorLabel);
        if (color!=null) {
          borderColorLabel.setBackground(color);
          borderColorLabel.setForeground(color);
          setBorderWidth(color,borderTitleColorLabel.getBackground());
        }
      }
    });

    borderTitleColorLabel = new JLabel(" ... ");
//    borderTitleColorLabel.setBorder(new LineBorder (Color.WHITE));
    borderTitleColorLabel.setOpaque(true);
    borderTitleColorLabel.setBackground(Color.BLACK);
    borderTitleColorLabel.setForeground(Color.BLACK);
    borderTitleColorLabel.setToolTipText(res.getString("EjsOptions.BorderTitleColor"));
    borderTitleColorLabel.addMouseListener(new MouseAdapter(){
      public void mouseClicked(MouseEvent arg0) {
        Color color = EditorForColor.editColor (borderTitleColorLabel);
        if (color!=null) {
          borderTitleColorLabel.setBackground(color);
          borderTitleColorLabel.setForeground(color);
          setBorderWidth(borderColorLabel.getBackground(),color);
        }
      }
    });

    borderTF=new JTextField("2");
    borderTF.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) { setBorderWidth(borderColorLabel.getBackground(),borderTitleColorLabel.getBackground()); }
    });
    borderTF.addFocusListener(new FocusAdapter() {
      public void focusLost(FocusEvent arg0) { setBorderWidth(borderColorLabel.getBackground(),borderTitleColorLabel.getBackground()); }
    });
    setBorderWidth(borderColorLabel.getBackground(),borderTitleColorLabel.getBackground());

    JPanel borderGridPanel = new JPanel (new GridLayout(1,0,2,2));
    borderGridPanel.add(borderColorLabel);
    borderGridPanel.add(borderTitleColorLabel);
    
    JPanel borderPanel=new JPanel (new BorderLayout());
    borderPanel.add(borderLabel,BorderLayout.WEST);
    borderPanel.add(borderTF,BorderLayout.CENTER);
    borderPanel.add(borderGridPanel,BorderLayout.EAST);

    // ---------------------------
    // Default File
    // ---------------------------

    JLabel loadFileLabel = new JLabel(res.getString ("EjsOptions.loadFile"));
    loadFileLabel.setBorder(border0002);
    JButton loadFileB = new JButton(" ... ");
    loadFileB.setMargin(new Insets (2,5,2,5));
    loadFileB.setActionCommand ("SelectLoadFile");
    loadFileB.addMouseListener (mouseListener);

    loadFileTF=new JTextField("");

    JPanel loadFilePanel=new JPanel (new BorderLayout());
    loadFilePanel.add(loadFileLabel,BorderLayout.WEST);
    loadFilePanel.add(loadFileTF,BorderLayout.CENTER);
    loadFilePanel.add(loadFileB,BorderLayout.EAST);

    // ---------------------------
    // Show/hide the experiments panel
    // ---------------------------

    experimentsCB  = new JCheckBox(res.getString("EjsOptions.enableExperiments"), false);
    experimentsCB.setRequestFocusEnabled(false);
    experimentsCB.addItemListener(itemListener);

    // ---------------------------
    // What HTMLs to generate
    // ---------------------------

    JLabel htmlLabel = new JLabel(res.getString("EjsOptions.generateHtml"));
    htmlLabel.setHorizontalAlignment(SwingConstants.LEFT);

    generateHtmlRB    = new JRadioButton(res.getString("EjsOptions.generateLeftFrameHtml"), true);
    generateHtmlRB.setRequestFocusEnabled(false);
    generateHtmlRB.addItemListener(itemListener);
    generateHtmlRB.setSelected(true);

    generateHtmlTB    = new JRadioButton(res.getString("EjsOptions.generateTopFrameHtml"), true);
    generateHtmlTB.setRequestFocusEnabled(false);
    generateHtmlTB.addItemListener(itemListener);

    generateOneHtmlRB    = new JRadioButton(res.getString("EjsOptions.generateOneHtml"), false);
    generateOneHtmlRB.setRequestFocusEnabled(false);
    generateOneHtmlRB.addItemListener(itemListener);

    generateNoHtmlRB    = new JRadioButton(res.getString("EjsOptions.generateNoHtml"), false);
    generateNoHtmlRB.setRequestFocusEnabled(false);
    generateNoHtmlRB.addItemListener(itemListener);

    ButtonGroup group1 = new ButtonGroup();
    group1.add (generateHtmlRB);
    group1.add (generateHtmlTB);
    group1.add (generateOneHtmlRB);
    group1.add (generateNoHtmlRB);

    JPanel htmlOptionsPanel = new JPanel (new GridLayout(2,2)); // FlowLayout(FlowLayout.CENTER)); // GridLayout(1,0));
    htmlOptionsPanel.setBorder(new EmptyBorder(0,50,0,5));
    htmlOptionsPanel.add(generateHtmlRB);
    htmlOptionsPanel.add(generateOneHtmlRB);
    htmlOptionsPanel.add(generateHtmlTB);
    htmlOptionsPanel.add(generateNoHtmlRB);

    // ---------------------------
    // Body tag
    // ---------------------------

    htmlBodyTF=new JTextField("");
    htmlBodyTF.addActionListener(new ActionListener () {
      public void actionPerformed(ActionEvent _evt) { ejs.forceCompilation(); } // Re-generate when running
    });
    JPanel htmlBodyPanel=new JPanel (new BorderLayout(0,0));
    htmlBodyPanel.add(new JLabel(res.getString("EjsOptions.htmlBody")+" "),BorderLayout.WEST);
    htmlBodyPanel.add(htmlBodyTF,BorderLayout.CENTER);
    htmlBodyPanel.add(new JLabel(">"),BorderLayout.EAST);

    // ---------------------------
    // Generate JNLP file
    // ---------------------------

    generateJNLPCB  = new JCheckBox(res.getString("EjsOptions.generateJNLPFile"), false);
    generateJNLPCB.setRequestFocusEnabled(false);
    generateJNLPCB.addItemListener(itemListener);

    JLabel jnlpURLLabel=new JLabel(" "+res.getString("EjsOptions.jnlpURL"));
    jnlpURLTF=new JTextField("http://localhost/jaws");
    jnlpURLTF.addActionListener(new ActionListener () {
      public void actionPerformed(ActionEvent _evt) { ejs.forceCompilation(); } // Re-generate when running
    });

    JPanel jnlpBottomPanel = new JPanel (new BorderLayout());
    jnlpBottomPanel.add(jnlpURLLabel,BorderLayout.WEST);
    jnlpBottomPanel.add(jnlpURLTF,BorderLayout.CENTER);

    JPanel jnlpPanel = new JPanel (new BorderLayout());
    jnlpPanel.add(generateJNLPCB,BorderLayout.NORTH);
    jnlpPanel.add(jnlpBottomPanel,BorderLayout.SOUTH);


    // ---------------------------
    // Generate HTML for eMersion
    // ---------------------------

//    eMersionCB = new JCheckBox(res.getString("EjsOptions.eMersionEnabled"), false);
//    eMersionCB.setRequestFocusEnabled(false);
//    eMersionCB.addItemListener(itemListener);
//
//    JLabel eMersionURLLabel=new JLabel(" "+res.getString("EjsOptions.eMersionURL"));
//    eMersionURLTF=new JTextField("http://localhost:8080");
//    eMersionURLTF.addActionListener(new ActionListener () {
//      public void actionPerformed(ActionEvent _evt) { ejs.forceCompilation(); } // Re-generate when running
//    });
//
//    JPanel eMersionBottomPanel = new JPanel (new java.awt.BorderLayout());
//    eMersionBottomPanel.add(eMersionURLLabel,BorderLayout.WEST);
//    eMersionBottomPanel.add(eMersionURLTF,BorderLayout.CENTER);
//
//    JPanel eMersionPanel = new JPanel (new java.awt.BorderLayout());
//    eMersionPanel.add(eMersionCB,BorderLayout.NORTH);
//    eMersionPanel.add(eMersionBottomPanel,BorderLayout.SOUTH);

    // ---------------------------
    // Save when running
    // ---------------------------

    saveWhenRunningCB  = new JCheckBox(res.getString("EjsOptions.SaveWhenRunning"), true);
    saveWhenRunningCB.setRequestFocusEnabled(false);

    // ---------------------------
    // Show property errors
    // ---------------------------

    showPropertyErrorsCB  = new JCheckBox(res.getString("EjsOptions.ShowPropertyErrors"), false);
    showPropertyErrorsCB.setRequestFocusEnabled(false);

    // ---------------------------
    // Include model
    // ---------------------------

    includeModelCB  = new JCheckBox(res.getString("EjsOptions.includeModelCB"), true);
    includeModelCB.setRequestFocusEnabled(false);
    includeModelCB.addItemListener(itemListener);

    // ---------------------------
    // Run as applet
    // ---------------------------

    runAsAppletCB  = new JCheckBox(res.getString("EjsOptions.runAsApplet"), false);
    runAsAppletCB.setRequestFocusEnabled(false);

    // ---------------------------
    // Delete Java files
    // ---------------------------

    removeJavaFileCB  = new JCheckBox(res.getString("EjsOptions.removeJavaFile"), false);
    removeJavaFileCB.setRequestFocusEnabled(false);
    removeJavaFileCB.addItemListener(itemListener);

    // ---------------------------
    // Java VM to use
    // ---------------------------

    JLabel targetVMLabel = new JLabel(res.getString ("EjsOptions.targetVM"));
    targetVMLabel.setBorder(border0002);
    targetVMCB = new JComboBox(targetVMOptions);
    targetVMCB.setEditable(false);
    targetVMCB.setSelectedIndex(def_targetVM);
    targetVMCB.addItemListener(itemListener);

    JPanel targetVMPanel = new JPanel (new BorderLayout());
    targetVMPanel.add(targetVMLabel,BorderLayout.WEST);
    targetVMPanel.add(targetVMCB,BorderLayout.CENTER);

    // ---------------------------
    // Look and Feel
    // ---------------------------
    JLabel lookandfeelLabel = new JLabel(res.getString("EjsOptions.RunningLookAndFeel"));
    lookandfeelLabel.setBorder(border0002);

    lookandfeelCombo = new JComboBox();
    for (NamedLookAndFeel laf : NamedLookAndFeel.getInstalledLookAndFeels()) lookandfeelCombo.addItem(laf);
    lookandfeelCombo.setEditable(false);
    lookandfeelCombo.setSelectedIndex(lookandfeelCombo.getComponentCount()-1); // DEFAULT is the default

    JPanel lookandfeelPanel = new JPanel (new BorderLayout());
    lookandfeelPanel.add(lookandfeelLabel,BorderLayout.WEST);
    lookandfeelPanel.add(lookandfeelCombo,BorderLayout.CENTER);

    // ---------------------------
    // Java VM options
    // ---------------------------

    JLabel vmOptionsLabel = new JLabel(res.getString ("EjsOptions.VMOptions"));
    vmOptionsLabel.setBorder(border0002);

    vmOptionsTF=new JTextField("");

    JPanel vmOptionsPanel=new JPanel (new BorderLayout());
    vmOptionsPanel.add(vmOptionsLabel,BorderLayout.WEST);
    vmOptionsPanel.add(vmOptionsTF,BorderLayout.CENTER);

    // ---------------------------
    // Author
    // ---------------------------

    JLabel authorLabel = new JLabel(res.getString ("VariableEditor.Name"));
    authorLabel.setBorder(border0002);

    authorTF=new JTextField("");

    JPanel authorPanel=new JPanel (new BorderLayout());
    authorPanel.add(authorLabel,BorderLayout.WEST);
    authorPanel.add(authorTF,BorderLayout.CENTER);

    // ---------------------------
    // filiation
    // ---------------------------

    JLabel affiliationLabel = new JLabel(res.getString ("EjsOptions.Affiliation"));
    affiliationLabel.setBorder(border0002);

    affiliationTF=new JTextField("");

    JPanel affiliationPanel=new JPanel (new BorderLayout());
    affiliationPanel.add(affiliationLabel,BorderLayout.WEST);
    affiliationPanel.add(affiliationTF,BorderLayout.CENTER);

    // ---------------------------
    // contact
    // ---------------------------

    JLabel contactLabel = new JLabel(res.getString ("EjsOptions.Contact"));
    contactLabel.setVerticalAlignment(SwingConstants.TOP);
    contactLabel.setBorder(border0002);

    contactTF=new JTextArea("");
    JScrollPane contactSP = new JScrollPane(contactTF);

    JPanel contactPanel=new JPanel (new BorderLayout());
    contactPanel.setBorder(new EmptyBorder(0,2,0,0));
    contactPanel.add(contactLabel,BorderLayout.WEST);
    contactPanel.add(contactSP,BorderLayout.CENTER);

    // ---------------------------
    // Create author info panel
    // ---------------------------
    
    // Make all label the same dimension
    Set<JLabel> labelSet = new HashSet<JLabel>();
    labelSet.add(authorLabel);
    labelSet.add(affiliationLabel);
    labelSet.add(contactLabel);

    int maxWidth = 0, maxHeight=0;
    for (JLabel label : labelSet) {
      maxWidth  = Math.max(maxWidth,  label.getPreferredSize().width);
      if (label!=contactLabel) maxHeight = Math.max(maxHeight, label.getPreferredSize().height);
    }
    Dimension dim = new Dimension (maxWidth,maxHeight);
    for (JLabel label : labelSet) label.setPreferredSize(dim);

    // Put them together
    JPanel authorInfoPanel=new JPanel (new java.awt.GridLayout(0,1));
    authorInfoPanel.setBorder(new EmptyBorder(2,2,5,0));
    authorInfoPanel.add(authorPanel);
    authorInfoPanel.add(affiliationPanel);
    //authorInfoPanel.add(contactPanel);

    authorInfoTopPanel = new JPanel(new BorderLayout());
    authorInfoTopPanel.add(authorInfoPanel,BorderLayout.NORTH);
    authorInfoTopPanel.add(contactPanel,BorderLayout.CENTER);

    
    // ---------------------------
    // Digital libraries
    // ---------------------------
    
    ItemListener dlIL = new ItemListener() {
      public void itemStateChanged(ItemEvent _evt) {
        checkDLchanges();
      }
    };
    
    dlCompadreCB = new JCheckBox(res.getString("EjsOptions.UseCompadreDL"));
    dlCompadreCB.setSelected(true);
    dlCompadreCB.addItemListener(dlIL);
    
    dlMasterCB = new JCheckBox(res.getString("EjsOptions.UseMasterDLs"));
    dlMasterCB.setSelected(true);
    dlMasterCB.addItemListener(dlIL);
    
    dlListModel = new DefaultListModel();
    initDLlist();

    final JList dlList = new JList(dlListModel);
    JScrollPane dlScrollPane = new JScrollPane(dlList);

    JLabel dlLabel = new JLabel (res.getString("EjsOptions.OtherDL"));

    JButton dlAddButton = new JButton (ResourceLoader.getIcon(sysRes.getString("EjsOptions.Add.Icon")));
    dlAddButton.setToolTipText(res.getString("Add"));
    dlAddButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) {
        String newDL = JOptionPane.showInputDialog(tabbedPanel,res.getString("Add"));
        if (newDL!=null) {
          if (!newDL.toLowerCase().startsWith("http://")) newDL = "http://" + newDL;
          dlListModel.addElement(newDL);
          checkDLchanges();
        }
      }
    });
    
    JButton dlRemoveButton = new JButton (ResourceLoader.getIcon(sysRes.getString("EjsOptions.Remove.Icon")));
    dlRemoveButton.setToolTipText(res.getString("Remove"));
    dlRemoveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) {
        int selected = JOptionPane.showConfirmDialog(tabbedPanel,res.getString("Confirm"),
            res.getString("Warning"), JOptionPane.YES_NO_OPTION);
        if (selected != JOptionPane.YES_OPTION) return;
        Object[] toRemove = dlList.getSelectedValues();
        for (int i=0; i<toRemove.length; i++) dlListModel.removeElement(toRemove[i]);
        checkDLchanges();
      }
    });

    JButton dlDefaultButton = new JButton (ResourceLoader.getIcon(sysRes.getString("SimInfoEditor.Refresh.Icon")));
    dlDefaultButton.setToolTipText(res.getString("Default"));
    dlDefaultButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) {
        int selected = JOptionPane.showConfirmDialog(tabbedPanel,res.getString("Confirm"),
            res.getString("Warning"), JOptionPane.YES_NO_OPTION);
        if (selected != JOptionPane.YES_OPTION) return;
        initDLlist();
        checkDLchanges();
      }
    });

    JPanel dlButtonsPanel = new JPanel (new GridLayout(1,0,0,0));
    dlButtonsPanel.add(dlAddButton);
    dlButtonsPanel.add(dlRemoveButton);
    dlButtonsPanel.add(dlDefaultButton);
    
    JPanel dlControlPanel = new JPanel (new BorderLayout());
    dlControlPanel.add(dlLabel,BorderLayout.WEST);
    dlControlPanel.add(dlButtonsPanel,BorderLayout.EAST);
    
    JPanel dlOptionsPanel = new JPanel (new GridLayout(0,1));
    dlOptionsPanel.add(dlCompadreCB);
    dlOptionsPanel.add(dlMasterCB);
    
    JPanel dlTopPanel = new JPanel (new BorderLayout());
    dlTopPanel.add(dlOptionsPanel,BorderLayout.WEST);
    dlTopPanel.add(dlControlPanel,BorderLayout.SOUTH);

    JPanel dlPanel = new JPanel (new BorderLayout());
    dlPanel.setBorder(new EmptyBorder(5,5,0,5));
    dlPanel.add(dlTopPanel,BorderLayout.NORTH);
    dlPanel.add(dlScrollPane,BorderLayout.CENTER);

    // ---------------------------------
    // Put everything together
    // ---------------------------------

    JPanel positionPanel = new JPanel (new BorderLayout());
    positionPanel.setBorder(new EmptyBorder(5,5,5,0));
    positionPanel.add (positionLabel,BorderLayout.NORTH);
    positionPanel.add (positionButtonPanel,BorderLayout.CENTER);
    positionPanel.add (sizeButtonPanel,BorderLayout.SOUTH);

    JPanel fontAndFilePanel = new JPanel (new BorderLayout());
    fontAndFilePanel.setBorder(new EmptyBorder(0,5,0,0));
    fontAndFilePanel.add (fontPanel,BorderLayout.NORTH);
    //fontAndFilePanel.add (loadFilePanel,BorderLayout.SOUTH);

    //JPanel aspectPanel=new JPanel (new java.awt.GridLayout(0,1));
    //aspectPanel.setBorder(new EmptyBorder(0,5,5,0));
    //aspectPanel.add(experimentsCB);

    // Put HTML and body tags together
    JPanel htmlPanel = new JPanel (new BorderLayout());
    htmlPanel.setBorder(new EmptyBorder(5,5,0,5));
    htmlPanel.add(htmlLabel,BorderLayout.NORTH);
    htmlPanel.add(htmlOptionsPanel,BorderLayout.CENTER);
    htmlPanel.add(htmlBodyPanel,BorderLayout.SOUTH);

    JPanel createPanel=new JPanel (new java.awt.GridLayout(0,1));
//    Box createPanel=Box.createVerticalBox();
    createPanel.setBorder(new EmptyBorder(2,5,5,0));
    createPanel.add(showPropertyErrorsCB);
    createPanel.add(saveWhenRunningCB);
    createPanel.add(includeModelCB);
    //createPanel.add(runAsAppletCB);
    createPanel.add(removeJavaFileCB);
    createPanel.add(targetVMPanel);
    createPanel.add(vmOptionsPanel);
    createPanel.add(lookandfeelPanel);


  // ------------- End of Options ---------------

    Box globalAspectPanel = Box.createVerticalBox();
//    JPanel globalAspectPanel=new JPanel (new java.awt.GridLayout(0,1));
    globalAspectPanel.setBorder(new EmptyBorder(2,5,5,0));

    globalAspectPanel.add (positionPanel);
    globalAspectPanel.add (fontAndFilePanel);
    globalAspectPanel.add (shortHeadersPanel);
    globalAspectPanel.add (deprecatedElementsPanel);
    globalAspectPanel.add (checkOnExitPanel);
    globalAspectPanel.add (borderPanel);

    JPanel globalAspectTopPanel = new JPanel(new BorderLayout());
    globalAspectTopPanel.add(globalAspectPanel,BorderLayout.NORTH);

    Box webPanel = Box.createVerticalBox();
    webPanel.setBorder(new EmptyBorder(2,5,5,0));
    webPanel.add (htmlPanel);
//    webPanel.add (eMersionPanel);


    JPanel webTopPanel = new JPanel(new BorderLayout());
    webTopPanel.add(webPanel,BorderLayout.NORTH);

    JPanel createTopPanel = new JPanel(new BorderLayout());
    createTopPanel.add(createPanel,BorderLayout.NORTH);

    tabbedPanel = new JTabbedPane(SwingConstants.TOP);
    tabbedPanel.add(res.getString("EjsOptions.RunningOptions"),createTopPanel);
    tabbedPanel.add(res.getString("EjsOptions.ExportOptions"),webTopPanel);
    tabbedPanel.add(res.getString("EjsOptions.AspectOptions"),globalAspectTopPanel);
    tabbedPanel.add(res.getString("EjsOptions.DigitalLibraries"),dlPanel);
    tabbedPanel.add(res.getString("EjsOptions.Author"),authorInfoTopPanel);

    JSeparator sep1 = new JSeparator (SwingConstants.HORIZONTAL);

    JPanel bottomPanel = new JPanel (new BorderLayout());
    bottomPanel.add (sep1,BorderLayout.NORTH);
    bottomPanel.add (buttonPanel,BorderLayout.SOUTH);

    dialog = new JDialog(_frame);
    dialog.getContentPane().setLayout (new BorderLayout(0,0));
    dialog.getContentPane().add (tabbedPanel,BorderLayout.CENTER);
    dialog.getContentPane().add (bottomPanel,BorderLayout.SOUTH);
    dialog.setTitle (res.getString("EjsOptions.Title"));
    dialog.validate();
    dialog.pack();
//    dialog.setSize(res.getDimension("EjsOptions.Size"));
    dialog.setModal(false);
  }

  private void initDLlist() {
    dlListModel.clear();
    String dlList = FileUtils.readTextFile(new File(ejs.getDocDirectory(),"LibraryService/EJS_digital_libraries.txt"),null);
    if (dlList!=null) {
      StringTokenizer tkn = new StringTokenizer(dlList,"\n");
      while (tkn.hasMoreTokens()) {
        String library = tkn.nextToken();
        if (library.toLowerCase().startsWith("http://")) dlListModel.addElement(library);
      }
    }
  }


  private void checkDLchanges() {
    DigitalLibraryUtils.recreateGUI();
    ejs.showDLButton (dlCompadreCB.isSelected() || dlMasterCB.isSelected() || dlListModel.getSize()>0);
  }
  
  
  public void edit (Component _parent) {
    dialog.setLocationRelativeTo (_parent);
    dialog.setVisible (true);
  }

  /**
   * Reads the EJS options.
   * Returns true if the autho info panel is empty and must be filled in
   * @return
   */
  public boolean read () {
    File optionFile=null;
    boolean firstTime=false;
    try {
      optionFile = new File (ejs.getConfigDirectory(),"EjsOptions.txt");
      if (!optionFile.exists()) {
        optionFile = new File (ejs.getBinDirectory(),OsejsCommon.CONFIG_DIR_PATH+"/EjsOptions.txt");
        firstTime = true;
      }
      dlListModel.clear();
      boolean hasDLs = false;
      Reader reader = new FileReader(optionFile);
      contactTF.setText("");
      LineNumberReader l = new LineNumberReader(reader);
      String sl = l.readLine();
      while (sl != null) {
        if (sl.indexOf("UseCompadre=")>=0) hasDLs = true; // This is for backwards compatibility for the time where DLs where not written
        readOptionLine (sl);
        sl = l.readLine();
      }
      reader.close();
      setBorderWidth(borderColorLabel.getBackground(),borderTitleColorLabel.getBackground());
      if (!hasDLs) initDLlist(); // This is for backwards compatibility for the time where DLs where not written
//      if (firstTime) {
//        tabbedPanel.setSelectedComponent(authorInfoTopPanel);
//        edit(ejsFrame);
//        JOptionPane.showMessageDialog(dialog, res.getString("EjsOptions.FillAuthorInfo"));
//      }
    } catch (Exception ex) {
      ex.printStackTrace();
      System.out.println("Can't read options from: "+optionFile.getAbsolutePath());
    }
    return firstTime;
  }

  public void askAuthorInfo(JFrame _frame) {
    tabbedPanel.setSelectedComponent(authorInfoTopPanel);
    edit(_frame);
    JOptionPane.showMessageDialog(dialog, res.getString("EjsOptions.FillAuthorInfo"));
  }

  /**
   * Reads one line of the options file
   * @param _line String
   */
  private void readOptionLine (String _line) {
//    System.err.println ("Reading option line = "+_line);
    if (_line.startsWith("position=")) {
      if (_line.indexOf("TOPLEFT")>=0) {
        topLeftRB.setSelected(true);
        placeEjsFrame(TOPLEFT);
      }
      else if (_line.indexOf("CUSTOM")>=0)  {
        _line = _line.substring(_line.indexOf(':')+1);
        String piece = _line.substring(0,_line.indexOf(','));
        int x = Integer.parseInt(piece);
        piece = _line.substring(_line.indexOf(',')+1);
        int y = Integer.parseInt(piece);
        customLocation = new Point(x,y);
        customRB.setSelected(true);
        placeEjsFrame(CUSTOM);
      }
      else { // CENTER
        centerRB.setSelected(true);
        placeEjsFrame(CENTER);
      }
    }
    
    if (_line.startsWith("size=")) {
      if (_line.indexOf("DEFAULT")>=0) defaultSizeRB.setSelected(true);
      else if (_line.indexOf("CUSTOM")>=0)  {
        _line = _line.substring(_line.indexOf(':')+1);
        String piece = _line.substring(0,_line.indexOf(','));
        int w = Integer.parseInt(piece);
        piece = _line.substring(_line.indexOf(',')+1);
        int h = Integer.parseInt(piece);
        Rectangle bounds = OsejsCommon.getScreenBounds(ejs.getMainFrame());
        w = Math.min(w,bounds.width-10);
        h = Math.min(h,bounds.height-10);
        customSize = new Dimension (w,h);
        currentSizeRB.setSelected(true);
      }
    }
    
    else if (_line.startsWith("font=")) {
      currentFont = _line.substring(5);
      if (currentFont.equals("<default>")) ejs.setFont(def_font);
      else ejs.setFont(InterfaceUtils.font(def_font,currentFont));
    }

    else if (_line.startsWith("loadFile=")) {
      loadFileTF.setText(_line.substring(9));
    }

    else if (_line.startsWith("showPropertyErrors=")) {
      if (_line.indexOf("false")>0) showPropertyErrorsCB.setSelected(false);
      else showPropertyErrorsCB.setSelected(true);
    }

    else if (_line.startsWith("experiments=")) {
      if (_line.indexOf("false")>0) experimentsCB.setSelected(false);
      else experimentsCB.setSelected(true);
      ejs.showExperimentsPanel(experimentsCB.isSelected());
    }

    else if (_line.startsWith("generateHtml=")) {
      if      (_line.indexOf("NONE")>0)      generateNoHtmlRB.setSelected(true);
      else if (_line.indexOf("ONE_PAGE")>0)  generateOneHtmlRB.setSelected(true);
      else if (_line.indexOf("TOP_FRAME")>0) generateHtmlTB.setSelected(true);
      else generateHtmlRB.setSelected(true);
    }

    else if (_line.startsWith("htmlBody=")) {
      htmlBodyTF.setText(_line.substring(9));
    }

    else if (_line.startsWith("generateJNLPFile=")) {
      if (_line.indexOf("false")>0) generateJNLPCB.setSelected(false);
      else generateJNLPCB.setSelected(true);
    }
    else if (_line.startsWith("jnlpURL=")) {
      jnlpURLTF.setText(_line.substring(8));
    }

//    else if (_line.startsWith("eMersionEnabled=")) {
//      if (_line.indexOf("true")>0) eMersionCB.setSelected(true);
//      else eMersionCB.setSelected(false);
//    }
//    else if (_line.startsWith("eMersionURL=")) {
//      eMersionURLTF.setText(_line.substring(12));
//    }

    else if (_line.startsWith("saveWhenRunning=")) {
      if (_line.indexOf("false")>0) saveWhenRunningCB.setSelected(false);
      else saveWhenRunningCB.setSelected(true);
    }

    else if (_line.startsWith("includeModel=")) {
      if (_line.indexOf("true")>0) includeModelCB.setSelected(true);
      else includeModelCB.setSelected(false);
    }

    else if (_line.startsWith("runAsApplet=")) {
      if (_line.indexOf("true")>0) runAsAppletCB.setSelected(true);
      else runAsAppletCB.setSelected(false);
    }

    else if (_line.startsWith("removeJavaFile=")) {
      if (_line.indexOf("true")>0) removeJavaFileCB.setSelected(true);
      else removeJavaFileCB.setSelected(false);
    }
    
    else if (_line.startsWith("targetVM=")) {
      String target = _line.substring(9);
      boolean found = false;
      for (int i=0; i<targetVMOptions.length; i++) {
        if (targetVMOptions[i].equalsIgnoreCase(target)) {
          targetVMCB.setSelectedIndex(i);
          found = true;
        }
      }
      if (!found) targetVMCB.setSelectedIndex(def_targetVM);
    }

    else if (_line.startsWith("lookAndFeel=")) {
      String look = _line.substring(12);
      NamedLookAndFeel nlf = NamedLookAndFeel.getLookAndFeel(look);
      if (nlf!=null) lookandfeelCombo.setSelectedItem(nlf);
      else lookandfeelCombo.setSelectedIndex(lookandfeelCombo.getComponentCount()-1);
    }

    else if (_line.startsWith("VMOptions=")) vmOptionsTF.setText(_line.substring(10));
    else if (_line.startsWith("HTMLPanelDimension=")) {
      String[] b = org.colos.ejs.osejs.utils.ResourceUtil.tokenizeString(_line.substring(19));
      htmlPanelSize =  new Dimension(Integer.parseInt(b[0]), Integer.parseInt(b[1]));
    }

    else if (_line.startsWith("LastFile=")) {
      lastXMLFilePath = _line.substring(9).trim();
      if (lastXMLFilePath.length()<=0) lastXMLFilePath = null;
    }
    
    else if (_line.startsWith("fileRead=")) ejs.getOpenedFilePathList().add(_line.substring(9).trim());

    else if (_line.startsWith("UseCompadre=")) {
      if (_line.indexOf("false")>0) dlCompadreCB.setSelected(false);
      else dlCompadreCB.setSelected(true);
    }
    else if (_line.startsWith("UseMasterDL=")) {
      if (_line.indexOf("false")>0) dlMasterCB.setSelected(false);
      else dlMasterCB.setSelected(true);
    }
    else if (_line.startsWith("ShowDeprecatedElements=")) {
      if (_line.indexOf("true")>0) deprecatedElementsCB.setSelected(true);
      else deprecatedElementsCB.setSelected(false);
    }
    else if (_line.startsWith("UseShortHeaders=")) {
      if (_line.indexOf("true")>0) shortHeadersCB.setSelected(true);
      else shortHeadersCB.setSelected(false);
    }
    else if (_line.startsWith("CheckOnExit=")) {
      if (_line.indexOf("false")>0) checkOnExitCB.setSelected(false);
      else checkOnExitCB.setSelected(true);
    }
    else if (_line.startsWith("BorderWidth=")) {
      borderTF.setText(_line.substring(12));
    }
    else if (_line.startsWith("BorderColor=")) {
      StringTokenizer tkn = new StringTokenizer(_line.substring(12),",");
      try {
        int red = Integer.parseInt(tkn.nextToken()); 
        int green = Integer.parseInt(tkn.nextToken()); 
        int blue = Integer.parseInt(tkn.nextToken()); 
        int alpha = Integer.parseInt(tkn.nextToken());
        Color color = new Color(red,green,blue,alpha);
        borderColorLabel.setBackground(color);
        borderColorLabel.setForeground(color);
      }
      catch (Exception exc) { 
        borderColorLabel.setBackground(Color.RED); 
        borderColorLabel.setForeground(Color.RED); 
      }
    }
    else if (_line.startsWith("BorderTitleColor=")) {
      StringTokenizer tkn = new StringTokenizer(_line.substring(17),",");
      try {
        int red = Integer.parseInt(tkn.nextToken()); 
        int green = Integer.parseInt(tkn.nextToken()); 
        int blue = Integer.parseInt(tkn.nextToken()); 
        int alpha = Integer.parseInt(tkn.nextToken());
        Color color = new Color(red,green,blue,alpha);
        borderTitleColorLabel.setBackground(color);
        borderTitleColorLabel.setForeground(color);
      }
      catch (Exception exc) { 
        borderTitleColorLabel.setBackground(Color.BLACK); 
        borderTitleColorLabel.setForeground(Color.BLACK); 
      }
    }

    else if (_line.startsWith("DigitalLibrary=")) dlListModel.addElement(_line.substring(15).trim());

    else if (_line.startsWith("Author=")) authorTF.setText(_line.substring(7));
    else if (_line.startsWith("Affiliation=")) affiliationTF.setText(_line.substring(12));
    else if (_line.startsWith("Filiation=")) affiliationTF.setText(_line.substring(10)); // backwards compatibility
    else if (_line.startsWith("Contact=")) contactTF.setText(contactTF.getText()+_line.substring(8)+"\n");

}

  /**
   * Saves all the options in a text file
   */
  public void save () {
    File file = new File (ejs.getConfigDirectory(),"EjsOptions.txt");
    try {
      if (ejs.isVerbose()) System.out.println ("Saving options file to: "+file.getAbsolutePath());
      file.getParentFile().mkdirs();
      FileWriter fout = new FileWriter(file);
      switch (position) {
        default :
        case CENTER  : fout.write("position=CENTER\n"); break;
        case TOPLEFT : fout.write("position=TOPLEFT\n"); break;
        case CUSTOM  :
          Point p = ejs.getMainFrame().getLocation();
          Rectangle bounds = OsejsCommon.getScreenBounds(ejs.getMainFrame());
          fout.write("position=CUSTOM:"+(p.x-bounds.x)+","+(p.y-bounds.y)+"\n"); break;
      }
      if (defaultSizeRB.isSelected()) fout.write("size=DEFAULT\n");
      else {
          Dimension dim = ejs.getMainFrame().getSize();
          fout.write("size=CUSTOM:"+dim.width+","+dim.height+"\n");
      }
      fout.write("font="+currentFont+"\n");
      fout.write("loadFile="+loadFileTF.getText()+"\n");
      fout.write("showPropertyErrors="+showPropertyErrorsCB.isSelected()+"\n");
      fout.write("experiments="+experimentsCB.isSelected()+"\n");

      if      (generateHtmlRB.isSelected())    fout.write("generateHtml=LEFT_FRAME\n");
      else if (generateHtmlTB.isSelected())    fout.write("generateHtml=TOP_FRAME\n");
      else if (generateOneHtmlRB.isSelected()) fout.write("generateHtml=ONE_PAGE\n");
      else fout.write("generateHtml=NONE\n");
      fout.write("htmlBody="+getHtmlBody()+"\n");///FKH

      fout.write("generateJNLPFile="+generateJNLPCB.isSelected()+"\n");
      fout.write("jnlpURL="+jnlpURLTF.getText()+"\n");

//      fout.write("eMersionEnabled="+eMersionCB.isSelected()+"\n");
//      fout.write("eMersionURL="+eMersionURLTF.getText()+"\n");

      fout.write("saveWhenRunning="+saveWhenRunningCB.isSelected()+"\n");
      fout.write("includeModel="+includeModelCB.isSelected()+"\n");
      fout.write("runAsApplet="+runAsAppletCB.isSelected()+"\n");
      fout.write("removeJavaFile="+removeJavaFileCB.isSelected()+"\n");
      fout.write("targetVM="+targetVMCB.getSelectedItem()+"\n");
      fout.write("VMOptions="+vmOptionsTF.getText()+"\n");
      fout.write("lookAndFeel="+getRunningLookAndFeel().getName()+"\n");
      
      
      java.awt.Dimension size = ejs.getDescriptionEditor().getCurrentPanelSize();
      fout.write("HTMLPanelDimension="+size.width+" "+size.height+"\n");

      fout.write("UseCompadre="+dlCompadreCB.isSelected()+"\n");
      fout.write("UseMasterDL="+dlMasterCB.isSelected()+"\n");
      
      
      fout.write("UseShortHeaders="+shortHeadersCB.isSelected()+"\n");
      fout.write("ShowDeprecatedElements="+deprecatedElementsCB.isSelected()+"\n");
      fout.write("CheckOnExit="+checkOnExitCB.isSelected()+"\n");
      
      fout.write("BorderWidth="+borderTF.getText()+"\n");
      {
        Color color = borderColorLabel.getBackground();
        fout.write("BorderColor="+color.getRed()+","+color.getGreen()+","+color.getBlue()+","+color.getAlpha()+"\n");;
      }
      {
        Color color = borderTitleColorLabel.getBackground();
        fout.write("BorderTitleColor="+color.getRed()+","+color.getGreen()+","+color.getBlue()+","+color.getAlpha()+"\n");;
      }

      for (int i=0,n=dlListModel.getSize(); i<n; i++) {
        fout.write("DigitalLibrary="+dlListModel.getElementAt(i)+"\n");
      }

      fout.write("Author="+authorTF.getText()+"\n");
      fout.write("Affiliation="+affiliationTF.getText()+"\n");


      String contact = contactTF.getText().trim();
      int index = contact.indexOf('\n');
      while (index>=0) {
        fout.write("Contact="+contact.substring(0,index)+"\n");
        contact = contact.substring(index+1);
        //else contact = "";
        index = contact.indexOf('\n');
      }
      if (contact.length()>0) fout.write("Contact="+contact+"\n");
      
      File lastFile = ejs.getCurrentXMLFile();
      if (lastFile==null || lastFile==ejs.getUnnamedXMLFile()) fout.write("LastFile=\n");
      else fout.write("LastFile="+FileUtils.getPath(lastFile)+"\n");

      for (String path : ejs.getOpenedFilePathList()) fout.write("fileRead="+path+"\n");

      fout.close();
    } catch (IOException ex) {
      ex.printStackTrace();
      System.out.println("Can't save options to: "+file.getAbsolutePath());
    }
  }

}
