package org.colos.ejs.model_elements.input_output;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.colos.ejs.library.control.EjsControl;
import org.colos.ejs.model_elements.*;
import org.colos.ejs.library.utils.ModelElementsUtilities;
import org.opensourcephysics.desktop.OSPDesktop;
import org.opensourcephysics.tools.ResourceLoader;

public class WebServerReaderElement implements ModelElement {
  static ImageIcon ELEMENT_ICON = ResourceLoader.getIcon("org/colos/ejs/model_elements/input_output/WebServerReader.png"); // This icon is included in this jar
  static ImageIcon LINK_ICON = org.opensourcephysics.tools.ResourceLoader.getIcon("data/icons/link.gif");      // This icon is bundled with EJS
  
  static private final String BEGIN_FILE_HEADER = "<ServerAdress><![CDATA["; // Used to delimit my XML information
  static private final String END_FILE_HEADER = "]]></ServerAdress>";        // Used to delimit my XML information
  
  private ModelElementsCollection elementsCollection; // A provider of services for edition under EJS
  private JDialog helpDialog; // The dialog for help
  private JDialog  editorDialog; // The dialog for edition
  private JTextField field = new JTextField();  // needs to be created to avoid null references
  
  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "WebReader"; }
  
  public String getConstructorName() { return "org.colos.ejs.model_elements.input_output.WebServerReader"; }
  
  public String getInitializationCode(String _name) {
    String value = field.getText().trim();
    if (value.length()<=0) return _name + " = new " + getConstructorName() + "(this,null); // Constructor with no filename";
    return _name + " = new " + getConstructorName()+"(this," + ModelElementsUtilities.getQuotedValue(value)+"); // Constructor with a filename";
  }
  
  public String getDestructionCode(String _name) { return null; } // This element requires no destruction code

  public String getResourcesRequired() { return null; } // This element requires no resources
  
  public String getPackageList() { return null; } // No non-class file from my jar is required to package this element

  public String getDisplayInfo() {
    String value = field.getText().trim();
    if (value.length()<=0) return null;
    return "("+value+")";
  }

  public String savetoXML() {
    return BEGIN_FILE_HEADER+field.getText()+END_FILE_HEADER;
  }

  public void readfromXML(String _inputXML) {
    int begin = _inputXML.indexOf(BEGIN_FILE_HEADER);
    if (begin<0) return; // A syntax error
    int end = _inputXML.indexOf(END_FILE_HEADER,begin);
    if (end<0) return; // Another syntax error
    String text = _inputXML.substring(begin+BEGIN_FILE_HEADER.length(),end);
    field.setText(text);
  }

  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() {
    return "sends commands to an HTTP web server and reads its response";
  }
  
  public void showHelp(Component _parentComponent) {
    if (helpDialog==null) { // create the dialog
      helpDialog = new JDialog((JFrame) null,"Web server reader: "+ getGenericName());
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

  /**
   * This editor chooses to include the help for the element (because it is just so small). 
   * But this is not compulsory.
   */
  public void showEditor(String _name, Component _parentComponent, ModelElementsCollection _collection) {
    this.elementsCollection = _collection;
    if (editorDialog==null) { // create the dialog
      field.getDocument().addDocumentListener (new DocumentListener() {
        public void changedUpdate(DocumentEvent e) { elementsCollection.reportChange(WebServerReaderElement.this); }
        public void insertUpdate(DocumentEvent e)  { elementsCollection.reportChange(WebServerReaderElement.this); }
        public void removeUpdate(DocumentEvent e)  { elementsCollection.reportChange(WebServerReaderElement.this); }
      });
      JLabel fieldLabel = new JLabel(" Server address:");
      
      JButton linkButton = new JButton(LINK_ICON);
      linkButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          String value = field.getText().trim();
          if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
          else value = ModelElementsUtilities.getPureValue(value);
          String variable = elementsCollection.chooseVariable(field,"String", value);
          if (variable!=null) field.setText("%"+variable+"%");
        }
      });

      JPanel buttonsPanel = new JPanel(new GridLayout(1,0));
      buttonsPanel.add(linkButton);

      JPanel topPanel = new JPanel(new BorderLayout());
      topPanel.add(fieldLabel,BorderLayout.WEST);
      topPanel.add(field,BorderLayout.CENTER);
      topPanel.add(buttonsPanel,BorderLayout.EAST);

      editorDialog = new JDialog((JFrame) null,"Web server reader: "+ _name);
      editorDialog.getContentPane().setLayout(new BorderLayout());
      editorDialog.getContentPane().add(topPanel,BorderLayout.NORTH);
      editorDialog.getContentPane().add(createHelpComponent(),BorderLayout.CENTER);
      editorDialog.setModal(false);
      editorDialog.pack();
    }
    java.awt.Rectangle bounds = EjsControl.getDefaultScreenBounds();
    if (_parentComponent==null) editorDialog.setLocation(bounds.x + (bounds.width - editorDialog.getWidth())/2, bounds.y + (bounds.height - editorDialog.getHeight())/2);
    else editorDialog.setLocationRelativeTo(_parentComponent);
    editorDialog.setVisible(true);
  }
  
  public void refreshEditor(String _name) { 
    if (editorDialog!=null) editorDialog.setTitle("Web server reader: "+ _name);
  }

  public java.util.List<ModelElementSearch> search (String info, String searchString, int mode, ModelElementsCollection collection) {
    return null;
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
      java.net.URL htmlURL = ResourceLoader.getResource("org/colos/ejs/model_elements/input_output/WebServerReader.html").getURL();
      htmlArea.setPage(htmlURL);
    } catch(Exception exc) { exc.printStackTrace(); }
    return helpComponent;
  }
  
}
