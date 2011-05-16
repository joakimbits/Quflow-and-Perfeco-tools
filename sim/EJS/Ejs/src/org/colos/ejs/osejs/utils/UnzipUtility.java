package org.colos.ejs.osejs.utils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;

import javax.swing.*;

import org.colos.ejs.library.utils.TemporaryFilesManager;
import org.colos.ejs.osejs.Osejs;
import org.colos.ejs.osejs.OsejsCommon;
import org.opensourcephysics.display.DisplayRes;
import org.opensourcephysics.tools.minijar.PathAndFile;

public class UnzipUtility implements Runnable {

  static private final ResourceUtil res = new ResourceUtil("Resources");

  private Osejs ejs;
  private InputStream inputStream;
  private JLabel label;
  private JDialog tmpDialog;

  /**
   * Retrieves a ZIP file from a URL addres, uncompresses it in a given 
   * directory, and opens it with EJS (if sucessfull).
   */
  static public void unzipWithWarning (Osejs _ejs, Window _parentWindow, String _name, InputStream _inputStream) {
    JDialog tmpDialog;
    if (_parentWindow instanceof Frame) tmpDialog = new JDialog((Frame)_parentWindow,res.getString("Information"));
    else if (_parentWindow instanceof Dialog) tmpDialog = new JDialog((Dialog)_parentWindow,res.getString("Information"));
    else tmpDialog = new JDialog((Frame)null,res.getString("Information"));
    
    // tmpDialog = new JDialog((Window)_parentWindow,res.getString("Information")); This requires Java 1.6
    
    JLabel label = new JLabel (res.getString("Osejs.Init.ReadingFile")+" "+_name+"      ");
    label.setBorder(new javax.swing.border.EmptyBorder(10,10,10,10));
    label.setFont(label.getFont().deriveFont(14f));
    
    final UnzipUtility runnable = new UnzipUtility(_ejs,_inputStream,label,tmpDialog);
    final Thread thread = new Thread(runnable);
    thread.setPriority(Thread.NORM_PRIORITY);

    JButton cancelButton = new JButton (res.getString("EditorFor.Cancel"));
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) { TemporaryFilesManager.cancelProcess(); thread.interrupt(); }
    });
    JPanel panel = new JPanel (new FlowLayout(FlowLayout.CENTER));
    panel.add(cancelButton);

    tmpDialog.getContentPane().setLayout(new BorderLayout());
    tmpDialog.getContentPane().add(label,BorderLayout.CENTER);
    tmpDialog.getContentPane().add(panel,BorderLayout.SOUTH);
    tmpDialog.pack();
    if (_parentWindow!=null) {
      Dimension size = _parentWindow.getSize();
      Dimension mysize = tmpDialog.getSize();
      Point loc = _parentWindow.getLocation();
      tmpDialog.setLocation(loc.x+(size.width-mysize.width)/2,loc.y+(size.height-mysize.height)/2);
    }
    tmpDialog.setVisible(true); 
    thread.start();
  }
  
  private UnzipUtility(Osejs _ejs, InputStream _inputStream, JLabel _label, JDialog _tmpDialog) {
    ejs = _ejs;
    inputStream = _inputStream;
    label = _label;
    tmpDialog = _tmpDialog;
  }

  public void run() {
    Set<File> extractedFiles = null;
    File tmpDir = TemporaryFilesManager.createTemporaryDirectory("ZipModel", ejs.getTemporaryDirectory());
    if (tmpDir!=null) extractedFiles = TemporaryFilesManager.expandZip(inputStream, tmpDir, label, res.getString("Osejs.Init.ReadingFile")+" ");
    tmpDialog.setVisible(false);
    if (extractedFiles==null) {
      JOptionPane.showMessageDialog(ejs.getMainPanel(),res.getString("Osejs.File.NotReadOK"), res.getString("Osejs.File.ErrorReadingFile"), JOptionPane.ERROR_MESSAGE);
      return;
    }
    // Locate the EJS files
    String tmpDirPath = FileUtils.getPath(tmpDir);
    ArrayList<PathAndFile> pafList = new ArrayList<PathAndFile>();
    for (File file : extractedFiles) {
      if (OsejsCommon.isEJSfile(file)) pafList.add(new PathAndFile(FileUtils.getRelativePath(file, tmpDirPath, false),file));
    }
    // Select the model to read
    File modelFile=null;
    switch (pafList.size()) {
      case 0 : // no EJS files detected
        JOptionPane.showMessageDialog(ejs.getMainPanel(),res.getString("DigitalLibrary.NoXMLFound"), res.getString("Osejs.File.Error"),JOptionPane.ERROR_MESSAGE);
        break;
      case 1 : // Only one model was detected 
        modelFile = pafList.get(0).getFile(); 
        break;
      default : // There are more than one models
        modelFile = chooseOne(ejs.getMainPanel(),res.getDimension("Package.ConfirmList.Size"), res.getString("Package.ChooseOneModel"),res.getString("EditorFor.ChooseOne"), pafList, null);
        break;
    }
    if (modelFile!=null) {
      final File file = modelFile;
      SwingUtilities.invokeLater(new Runnable() {
        public void run() { ejs.readFile(file, false); }
      });
    }
  }

  /**
   * Choose one file out of a list
   * @return
   */
  static public File chooseOne (Component _parentComponent, Dimension _size, 
      String _message, String _title, java.util.List<PathAndFile> _list, JComponent _bottomComponent) {
    class ReturnValue { boolean value = false; }
    final ReturnValue returnValue=new ReturnValue();

    DefaultListModel listModel = new DefaultListModel();
    for (int i=0,n=_list.size(); i<n; i++) listModel.addElement(_list.get(i));
    JList list = new JList(listModel);
    list.setEnabled(true);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//    list.setSelectionInterval(0,listModel.getSize()-1);
    JScrollPane scrollPane = new JScrollPane(list);
    scrollPane.setPreferredSize(_size);

    final JDialog dialog = new JDialog();

    java.awt.event.MouseAdapter mouseListener =  new java.awt.event.MouseAdapter () {
      public void mousePressed (java.awt.event.MouseEvent evt) {
        AbstractButton button = (AbstractButton) (evt.getSource());
        String aCmd = button.getActionCommand();
        if      (aCmd.equals("ok"))      { returnValue.value = true;  dialog.setVisible (false); } //$NON-NLS-1$
        else if (aCmd.equals("cancel"))  { returnValue.value = false; dialog.setVisible (false); } //$NON-NLS-1$
      }
    };

    JButton okButton = new JButton (DisplayRes.getString("GUIUtils.Ok")); //$NON-NLS-1$
    okButton.setActionCommand ("ok"); //$NON-NLS-1$
    okButton.addMouseListener (mouseListener);

    JButton cancelButton = new JButton (DisplayRes.getString("GUIUtils.Cancel")); //$NON-NLS-1$
    cancelButton.setActionCommand ("cancel"); //$NON-NLS-1$
    cancelButton.addMouseListener (mouseListener);

    JPanel buttonPanel = new JPanel (new FlowLayout(FlowLayout.CENTER));
    buttonPanel.add (okButton);
    buttonPanel.add (cancelButton);

    JPanel topPanel1 = new JPanel (new BorderLayout ());

    JTextArea textArea   = new JTextArea (_message);
    textArea.setWrapStyleWord(true);
    textArea.setLineWrap(true);
    textArea.setEditable(false);
    textArea.setFont(textArea.getFont().deriveFont(Font.BOLD));
    textArea.setBackground(topPanel1.getBackground());
    textArea.setBorder(new javax.swing.border.EmptyBorder(5,5,10,5));

    topPanel1.setBorder(new javax.swing.border.EmptyBorder(5,10,5,10));
    topPanel1.add (textArea,BorderLayout.NORTH);
    topPanel1.add (scrollPane,BorderLayout.CENTER);
    if (_bottomComponent!=null) topPanel1.add (_bottomComponent,BorderLayout.SOUTH);

    JSeparator sep1 = new JSeparator (SwingConstants.HORIZONTAL);

    JPanel southPanel = new JPanel (new java.awt.BorderLayout());
    southPanel.add (sep1,java.awt.BorderLayout.NORTH);
    southPanel.add (buttonPanel,java.awt.BorderLayout.SOUTH);

    dialog.getContentPane().setLayout (new java.awt.BorderLayout(5,0));
    dialog.getContentPane().add (topPanel1,java.awt.BorderLayout.CENTER);
    dialog.getContentPane().add (southPanel,java.awt.BorderLayout.SOUTH);

    dialog.addWindowListener (
        new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent event) { returnValue.value = false; }
        }
    );

    //  dialog.setSize (_size);
    dialog.validate();
    dialog.pack();
    dialog.setTitle (_title);
    dialog.setLocationRelativeTo (_parentComponent);
    dialog.setModal(true);

    dialog.setVisible (true);
    if (!returnValue.value) return null;
    PathAndFile paf = (PathAndFile) list.getSelectedValue();
    if (paf==null) return null;
    return paf.getFile();
  }

}
