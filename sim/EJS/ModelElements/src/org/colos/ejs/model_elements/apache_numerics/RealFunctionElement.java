package org.colos.ejs.model_elements.apache_numerics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.JTextComponent;

import org.colos.ejs.library.control.EjsControl;
import org.colos.ejs.model_elements.*;
import org.colos.ejs.osejs.edition.SearchResult;
import org.opensourcephysics.desktop.OSPDesktop;
import org.opensourcephysics.tools.ResourceLoader;

import com.cdsc.eje.gui.EJEArea;

public class RealFunctionElement implements ModelElement {
  static private ImageIcon ELEMENT_ICON = ResourceLoader.getIcon("org/colos/ejs/model_elements/apache_numerics/RealFunction.gif"); // This icon is included in this jar
  static private final String BEGIN_CODE_HEADER = "<Code><![CDATA["; // Used to delimit my XML information
  static private final String END_CODE_HEADER = "]]></Code>";        // Used to delimit my XML information
  static private final String BEGIN_SOLVER_HEADER = "<Solver><![CDATA["; // Used to delimit my XML information
  static private final String END_SOLVER_HEADER = "]]></Solver>";        // Used to delimit my XML information
  static private final String BEGIN_INTEGRATOR_HEADER = "<Integrator><![CDATA["; // Used to delimit my XML information
  static private final String END_INTEGRATOR_HEADER = "]]></Integrator>";        // Used to delimit my XML information
  static private final String DEFAULT_CODE = "public double value(double x) { // Define the 'value' function here\n  return x;\n}";

//  private ModelElementsCollection elementsCollection; // A provider of services for edition under EJS
  private JDialog helpDialog; // The dialog for help
  private JDialog  editorDialog; // The dialog for edition
  private JTextComponent codeEditor;  // The editor for the code
  private JComboBox solverCB, integratorCB; // Comboboxes to select the solver and integrator methods 
  private String codeStr=DEFAULT_CODE; // The actual code
  private String solverStr=RealFunction.SOLVER_BISECTION; // The actual solver
  private String integratorStr=RealFunction.INTEGRATOR_ROMBERG; // The actual integrator

  
  private String getCode() {
    if (codeEditor!=null) return codeEditor.getText();
    return codeStr;
  }
  
  private String getSolver() {
    if (solverCB!=null)  return solverCB.getSelectedItem().toString();
    return solverStr;
  }
  
  private String getIntegrator() {
    if (integratorCB!=null)  return integratorCB.getSelectedItem().toString();
    return integratorStr;
  }

  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "Function"; }
  
  public String getConstructorName() { return "org.colos.ejs.model_elements.apache_numerics.RealFunction"; }
  
  public String getInitializationCode(String _name) {
    StringBuffer buffer = new StringBuffer();
    buffer.append(_name + " = new " + getConstructorName() + "(new org.apache.commons.math.analysis.UnivariateRealFunction() {\n");
    buffer.append(getCode());
    buffer.append("\n});\n");
    buffer.append(_name + ".setSolver(\""+getSolver()+"\");\n");
    buffer.append(_name + ".setIntegrator(\""+getIntegrator()+"\");\n");
    return buffer.toString();
  }
  
  public String getDestructionCode(String _name) { return null; } // This element requires no destruction code

  public String getResourcesRequired() { return null; }

  public String getPackageList() { return null; } // No non-class file from my jar is required to package this element

  public String getDisplayInfo() { return null; } // Nothing to add

  public String savetoXML() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(BEGIN_CODE_HEADER+getCode()+END_CODE_HEADER + "\n");
    buffer.append(BEGIN_SOLVER_HEADER+getSolver()+END_SOLVER_HEADER + "\n");
    buffer.append(BEGIN_INTEGRATOR_HEADER+getIntegrator()+END_INTEGRATOR_HEADER);
    return buffer.toString();
  }

  public void readfromXML(String _inputXML) {
    int begin = _inputXML.indexOf(BEGIN_CODE_HEADER);
    if (begin>=0) {
      int end = _inputXML.indexOf(END_CODE_HEADER,begin);
      if (end>=0) codeStr = _inputXML.substring(begin+BEGIN_CODE_HEADER.length(),end);
      if (codeEditor!=null) codeEditor.setText(codeStr);
    }
    begin = _inputXML.indexOf(BEGIN_SOLVER_HEADER);
    if (begin>=0) {
      int end = _inputXML.indexOf(END_SOLVER_HEADER,begin);
      if (end>=0) solverStr = _inputXML.substring(begin+BEGIN_SOLVER_HEADER.length(),end);
      if (solverCB!=null) solverCB.setSelectedItem(solverStr);
    }
    begin = _inputXML.indexOf(BEGIN_INTEGRATOR_HEADER);
    if (begin>=0) {
      int end = _inputXML.indexOf(END_INTEGRATOR_HEADER,begin);
      if (end>=0) integratorStr = _inputXML.substring(begin+BEGIN_INTEGRATOR_HEADER.length(),end);
      if (integratorCB!=null) integratorCB.setSelectedItem(integratorStr);
    }
  }

  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() {
    return "defines a y = f(x) function with some utilities";
  }
  
  public void showHelp(Component _parentComponent) {
    if (helpDialog==null) { // create the dialog
      helpDialog = new JDialog((JFrame) null,"RealFunction: "+ getGenericName());
      helpDialog.getContentPane().setLayout(new BorderLayout());
      helpDialog.getContentPane().add(createHelpComponent(),BorderLayout.CENTER);
      helpDialog.setModal(false);
      helpDialog.pack();
    }
    java.awt.Rectangle bounds = EjsControl.getDefaultScreenBounds();
    if (_parentComponent==null) helpDialog.setLocation(bounds.x + (bounds.width - helpDialog.getWidth())/2, bounds.y + (bounds.height - helpDialog.getHeight())/2);
    else helpDialog.setLocationRelativeTo(_parentComponent);
    helpDialog.setVisible(true);
  }

  private void createEditor(final ModelElementsCollection _collection) {
      codeEditor = new EJEArea(_collection.getEJS());
      codeEditor.setPreferredSize(new Dimension(500,300));
      codeEditor.setText(codeStr);
      codeEditor.getDocument().addDocumentListener (new DocumentListener() {
        public void changedUpdate(DocumentEvent e) { _collection.reportChange(RealFunctionElement.this); }
        public void insertUpdate(DocumentEvent e)  { _collection.reportChange(RealFunctionElement.this); }
        public void removeUpdate(DocumentEvent e)  { _collection.reportChange(RealFunctionElement.this); }
      });
      JScrollPane scrollPanel = new JScrollPane(codeEditor);

      JLabel solverLabel = new JLabel(" Solver algorithm:",SwingConstants.RIGHT);
      JLabel integratorLabel = new JLabel(" Integration algorithm:",SwingConstants.RIGHT);
      // Make both labels the same dimension
      int maxWidth  = solverLabel.getPreferredSize().width;
      int maxHeight = solverLabel.getPreferredSize().height;
      maxWidth  = Math.max(maxWidth,  integratorLabel.getPreferredSize().width);
      maxHeight = Math.max(maxHeight, integratorLabel.getPreferredSize().height);
      Dimension dim = new Dimension (maxWidth,maxHeight);
      solverLabel.setPreferredSize(dim);
      integratorLabel.setPreferredSize(dim);

      ItemListener itemListener = new ItemListener() {
        public void itemStateChanged(ItemEvent arg0) { _collection.reportChange(RealFunctionElement.this); }
      };

      solverCB = new JComboBox();

      solverCB.addItem(RealFunction.SOLVER_BISECTION);
      solverCB.addItem(RealFunction.SOLVER_BRENT_DEKKER);
      solverCB.addItem(RealFunction.SOLVER_NEWTON);
      solverCB.addItem(RealFunction.SOLVER_SECANT);
      solverCB.addItem(RealFunction.SOLVER_MULLER);
      solverCB.addItem(RealFunction.SOLVER_LAGUERRE);
//      solverCB.addItem(RealFunction.SOLVER_RIDDER);
      solverCB.setSelectedItem(solverStr);
      solverCB.addItemListener(itemListener);

      integratorCB = new JComboBox();

      integratorCB.addItem(RealFunction.INTEGRATOR_ROMBERG);
      integratorCB.addItem(RealFunction.INTEGRATOR_SIMPSON);
      integratorCB.addItem(RealFunction.INTEGRATOR_TRAPEZOID);
      integratorCB.addItem(RealFunction.INTEGRATOR_LEGENDRE_GAUSS);
      integratorCB.setSelectedItem(integratorStr);
      integratorCB.addItemListener(itemListener);

      JPanel solverPanel = new JPanel(new BorderLayout());
      solverPanel.add(solverLabel, BorderLayout.WEST);
      solverPanel.add(solverCB, BorderLayout.CENTER);
      
      JPanel integratorPanel = new JPanel(new BorderLayout());
      integratorPanel.add(integratorLabel, BorderLayout.WEST);
      integratorPanel.add(integratorCB, BorderLayout.CENTER);

      JPanel topPanel = new JPanel(new GridLayout(0,1));
      topPanel.add(solverPanel);
      topPanel.add(integratorPanel);

      editorDialog = new JDialog((JFrame) null,"RealFunction: ");
      editorDialog.getContentPane().setLayout(new BorderLayout());
      editorDialog.getContentPane().add(topPanel,BorderLayout.NORTH);
      editorDialog.getContentPane().add(scrollPanel,BorderLayout.CENTER);
      editorDialog.setModal(false);
      editorDialog.pack();
  }

  public void showEditor(String _name, Component _parentComponent, final ModelElementsCollection _collection) {
    if (editorDialog==null) createEditor(_collection);
    editorDialog.setTitle("RealFunction: "+_name);
    java.awt.Rectangle bounds = EjsControl.getDefaultScreenBounds();
    if (_parentComponent==null) editorDialog.setLocation(bounds.x + (bounds.width - editorDialog.getWidth())/2, bounds.y + (bounds.height - editorDialog.getHeight())/2);
    else editorDialog.setLocationRelativeTo(_parentComponent);
    editorDialog.setVisible(true);

  }
  
  public void refreshEditor(String _name) { } // This element does not need to refresh the editor
  
  public java.util.List<ModelElementSearch> search (String info, String searchString, int mode, ModelElementsCollection collection) {
    if (editorDialog==null) createEditor(collection);
    java.util.List<ModelElementSearch> list = new ArrayList<ModelElementSearch>();
    boolean toLower = (mode & SearchResult.CASE_INSENSITIVE) !=0;
    if (toLower) searchString = searchString.toLowerCase();
    if (info==null) info = "";
    int lineCounter=1,caretPosition=0;
    StringTokenizer t = new StringTokenizer(codeEditor.getText(), "\n",true);
    while (t.hasMoreTokens()) {
      String line = t.nextToken();
      int index;
      if (toLower) index = line.toLowerCase().indexOf(searchString);
      else index = line.indexOf(searchString);
      if (index>=0) list.add(new ModelElementSearch(collection,this,info,line.trim(),codeEditor,lineCounter,caretPosition+index));
      caretPosition += line.length();
      lineCounter++;
    }
    return list;
  }

  // -------------------------------
  // Utilities
  // -------------------------------

  /**
   * Creates an HTML viewer with information about the class
   */
  static private Component createHelpComponent() {
    JEditorPane htmlArea = new JEditorPane ();
    htmlArea.setContentType ("text/html");
    htmlArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    htmlArea.setEditable(false);
    htmlArea.addHyperlinkListener(new HyperlinkListener() { // Make hyperlinks work
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType()==HyperlinkEvent.EventType.ACTIVATED)
          OSPDesktop.displayURL(e.getURL().toString());
      }
    });
    JScrollPane helpComponent = new JScrollPane(htmlArea);
    helpComponent.setPreferredSize(new Dimension(600,500));
    
    try { // read the help for this element
      java.net.URL htmlURL = ResourceLoader.getResource("org/colos/ejs/model_elements/apache_numerics/RealFunction.html").getURL();
      htmlArea.setPage(htmlURL);
    } catch(Exception exc) { exc.printStackTrace(); }
    return helpComponent;
  }
  
}
