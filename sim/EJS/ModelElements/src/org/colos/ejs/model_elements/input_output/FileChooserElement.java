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
import org.colos.ejs.model_elements.ModelElementsCollection;
import org.colos.ejs.library.utils.ModelElementsUtilities;
import org.opensourcephysics.desktop.OSPDesktop;
import org.opensourcephysics.tools.ResourceLoader;

public class FileChooserElement implements ModelElement {
  static ImageIcon ELEMENT_ICON = ResourceLoader.getIcon("org/colos/ejs/model_elements/input_output/FileChooser.png"); // This icon is included in this jar
  static ImageIcon LINK_ICON = org.opensourcephysics.tools.ResourceLoader.getIcon("data/icons/link.gif");      // This icon is bundled with EJS
  static ImageIcon FILE_ICON = org.opensourcephysics.tools.ResourceLoader.getIcon("data/icons/openSmall.gif"); // This icon is bundled with EJS
  
  static private final String BEGIN_DESCRIPTION_HEADER = "<Description><![CDATA["; // Used to delimit my XML information
  static private final String END_DESCRIPTION_HEADER = "]]></Description>";        // Used to delimit my XML information
  static private final String BEGIN_EXTENSIONS_HEADER = "<Extensions><![CDATA["; // Used to delimit my XML information
  static private final String END_EXTENSIONS_HEADER = "]]></Extensions>";        // Used to delimit my XML information
  
  private ModelElementsCollection elementsCollection; // A provider of services for edition under EJS
  private JDialog helpDialog; // The dialog for help
  private JDialog  editorDialog; // The dialog for edition
  private JTextField descriptionField = new JTextField();  // needs to be created to avoid null references
  private JTextField extensionsField = new JTextField();  // needs to be created to avoid null references
  
  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "FileChooser"; }
  
  public String getConstructorName() { return "org.colos.ejs.model_elements.input_output.FileChooser"; }
  
  public String getInitializationCode(String _name) {
    String description = descriptionField.getText().trim();
    String extensions = extensionsField.getText().trim();
    if (description.length()<=0) description = "XML files"; 
    if (extensions.length()<=0) extensions = "xml"; 
    return _name + " = new " + getConstructorName()+"(this," + ModelElementsUtilities.getQuotedValue(description)+","+ ModelElementsUtilities.getQuotedValue(extensions)+");";
  }
  
  public String getDestructionCode(String _name) { return null; } // This element requires no destruction code

  public String getResourcesRequired() { return null; } // Requires no resources
  
  public String getPackageList() { return null; } // No non-class file from my jar is required to package this element

  public String getDisplayInfo() {
    String description = descriptionField.getText().trim();
    String extensions = extensionsField.getText().trim();
    if (description.length()<=0) description = "XML files"; 
    if (extensions.length()<=0) extensions = "xml"; 
    return "("+extensions + " - " + description+")";
  }

  public String savetoXML() {
    return BEGIN_DESCRIPTION_HEADER+descriptionField.getText()+END_DESCRIPTION_HEADER + "\n" +
           BEGIN_EXTENSIONS_HEADER +extensionsField.getText() +END_EXTENSIONS_HEADER;
  }

  public void readfromXML(String _inputXML) {
    int begin = _inputXML.indexOf(BEGIN_DESCRIPTION_HEADER);
    if (begin>=0) {
      int end = _inputXML.indexOf(END_DESCRIPTION_HEADER,begin);
      if (end>=0) descriptionField.setText(_inputXML.substring(begin+BEGIN_DESCRIPTION_HEADER.length(),end));
    }
    begin = _inputXML.indexOf(BEGIN_EXTENSIONS_HEADER);
    if (begin>=0) {
      int end = _inputXML.indexOf(END_EXTENSIONS_HEADER,begin);
      if (end>=0) this.extensionsField.setText(_inputXML.substring(begin+BEGIN_EXTENSIONS_HEADER.length(),end));
    }
  }

  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() {
    return "encapsulates calls to OSPRuntime methods that let you choose a file for reading or writing";
  }

  public void showHelp(Component _parentComponent) {
    if (helpDialog==null) { // create the dialog
      helpDialog = new JDialog((JFrame) null,"File chooser: "+ getGenericName());
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

  public void showEditor(String _name, Component _parentComponent, ModelElementsCollection _collection) {
    this.elementsCollection = _collection;
    if (editorDialog==null) { // create the dialog
      JLabel descriptionLabel = new JLabel(" Description:",SwingConstants.RIGHT);
      JLabel extensionsLabel = new JLabel(" Extensions:",SwingConstants.RIGHT);
      // Make both labels the same dimension
      int maxWidth  = descriptionLabel.getPreferredSize().width;
      int maxHeight = descriptionLabel.getPreferredSize().height;
      maxWidth  = Math.max(maxWidth,  extensionsLabel.getPreferredSize().width);
      maxHeight = Math.max(maxHeight, extensionsLabel.getPreferredSize().height);
      Dimension dim = new Dimension (maxWidth,maxHeight);
      descriptionLabel.setPreferredSize(dim);
      extensionsLabel.setPreferredSize(dim);
      
      descriptionField.getDocument().addDocumentListener (new DocumentListener() {
        public void changedUpdate(DocumentEvent e) { elementsCollection.reportChange(FileChooserElement.this); }
        public void insertUpdate(DocumentEvent e)  { elementsCollection.reportChange(FileChooserElement.this); }
        public void removeUpdate(DocumentEvent e)  { elementsCollection.reportChange(FileChooserElement.this); }
      });
      extensionsField.getDocument().addDocumentListener (new DocumentListener() {
        public void changedUpdate(DocumentEvent e) { elementsCollection.reportChange(FileChooserElement.this); }
        public void insertUpdate(DocumentEvent e)  { elementsCollection.reportChange(FileChooserElement.this); }
        public void removeUpdate(DocumentEvent e)  { elementsCollection.reportChange(FileChooserElement.this); }
      });
      
      JButton descriptionLinkButton = new JButton(LINK_ICON);
      descriptionLinkButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          String value = descriptionField.getText().trim();
          if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
          else value = ModelElementsUtilities.getPureValue(value);
          String variable = elementsCollection.chooseVariable(descriptionField,"String", value);
          if (variable!=null) descriptionField.setText("%"+variable+"%");
        }
      });

      JButton extensionsLinkButton = new JButton(LINK_ICON);
      extensionsLinkButton.addActionListener(new ActionListener(){
        public void actionPerformed(ActionEvent e) {
          String value = extensionsField.getText().trim();
          if (!ModelElementsUtilities.isLinkedToVariable(value)) value = "";
          else value = ModelElementsUtilities.getPureValue(value);
          String variable = elementsCollection.chooseVariable(extensionsField,"String", value);
          if (variable!=null) extensionsField.setText("%"+variable+"%");
        }
      });

      JPanel descriptionPanel = new JPanel(new BorderLayout());
      descriptionPanel.add(descriptionLabel, BorderLayout.WEST);
      descriptionPanel.add(descriptionField, BorderLayout.CENTER);
      descriptionPanel.add(descriptionLinkButton, BorderLayout.EAST);
      
      JPanel extensionsPanel = new JPanel(new BorderLayout());
      extensionsPanel.add(extensionsLabel, BorderLayout.WEST);
      extensionsPanel.add(extensionsField, BorderLayout.CENTER);
      extensionsPanel.add(extensionsLinkButton, BorderLayout.EAST);

      JPanel topPanel = new JPanel(new GridLayout(0,1));
      topPanel.add(descriptionPanel);
      topPanel.add(extensionsPanel);

      editorDialog = new JDialog((JFrame) null,"File chooser: "+ _name);
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
    if (editorDialog!=null) editorDialog.setTitle("File chooser: "+ _name);
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
      java.net.URL htmlURL = ResourceLoader.getResource("org/colos/ejs/model_elements/input_output/FileChooser.html").getURL();
      htmlArea.setPage(htmlURL);
    } catch(Exception exc) { exc.printStackTrace(); }
    return helpComponent;
  }
  
}
