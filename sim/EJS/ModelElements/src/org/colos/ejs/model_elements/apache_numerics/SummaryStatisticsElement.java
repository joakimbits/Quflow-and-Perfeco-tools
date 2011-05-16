package org.colos.ejs.model_elements.apache_numerics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.colos.ejs.library.control.EjsControl;
import org.colos.ejs.model_elements.*;
import org.opensourcephysics.desktop.OSPDesktop;
import org.opensourcephysics.tools.ResourceLoader;

public class SummaryStatisticsElement implements ModelElement {
  static ImageIcon ELEMENT_ICON = ResourceLoader.getIcon("org/colos/ejs/model_elements/apache_numerics/SummaryStatistics.gif"); // This icon is included in this jar
  
  private JDialog helpDialog; // The dialog for help
  
  // -------------------------------
  // Implementation of ModelElement
  // -------------------------------
  
  public ImageIcon getImageIcon() { return ELEMENT_ICON; }
  
  public String getGenericName() { return "SummStats"; }
  
  public String getConstructorName() { return "org.apache.commons.math.stat.descriptive.SummaryStatistics"; }
  
  public String getInitializationCode(String _name) {
    return _name + " = new " + getConstructorName() + "();";
  }
  
  public String getDestructionCode(String _name) { return null; } // This element requires no destruction code

  public String getResourcesRequired() { return null; }

  public String getPackageList() { return null; } // No non-class file from my jar is required to package this element

  public String getDisplayInfo() { return null; } // Nothing to add

  public String savetoXML() { return null; } // Nothing to save

  public void readfromXML(String _inputXML) { } // Nothing to read

  // -------------------------------
  // Help and edition
  // -------------------------------

  public String getTooltip() {
    return "computes summary statistics of a number of double values without storing them";
  }
  
  public void showHelp(Component _parentComponent) {
    if (helpDialog==null) { // create the dialog
      helpDialog = new JDialog((JFrame) null,"SummaryStatistics: "+ getGenericName());
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
    showHelp(_parentComponent); 
  }
  
  public void refreshEditor(String _name) { } // This element does not need to refresh the editor
  
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
      java.net.URL htmlURL = ResourceLoader.getResource("org/colos/ejs/model_elements/apache_numerics/SummaryStatistics.html").getURL();
      htmlArea.setPage(htmlURL);
    } catch(Exception exc) { exc.printStackTrace(); }
    return helpComponent;
  }
  
}
