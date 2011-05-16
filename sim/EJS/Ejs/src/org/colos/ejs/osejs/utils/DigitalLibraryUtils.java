/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * Last Revision: March 2008
 */

package org.colos.ejs.osejs.utils;

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import org.colos.ejs.osejs.*;
import org.opensourcephysics.display.OSPRuntime;
import org.opensourcephysics.tools.ResourceLoader;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DigitalLibraryUtils {
  static private final ResourceUtil res = new ResourceUtil("Resources");
  static private final Icon DOCUMENT_ICON = ResourceLoader.getIcon("/data/icons/EjsDocument.gif");
  static private final Icon HTML_ICON = ResourceLoader.getIcon("/org/opensourcephysics/resources/tools/images/html.gif");
  
  static private JDialog netDialog=null;
  static private JTextPane htmlPane; // Previously was an EditorPane
  static private JTree tree;
  static private DefaultTreeModel treeModel;
  static private JButton disconnectButton;
  static private JPanel topPanel;
  static private JComponent additionalComponent;

  static public void showDialog(Osejs _ejs) {
    if (netDialog==null) createGUI(_ejs);
    netDialog.setVisible(true);
  }

  static private void createGUI (final Osejs _ejs) {
    DigitalLibraryNode nodeInfo = new DigitalLibraryNode (null,res.getString("DigitalLibrary.RootNode"),
                                                          res.getString("DigitalLibrary.Title"),null);
    nodeInfo.setIsTree(true);
    java.net.URL url = DigitalLibraryUtils.getLibraryServiceURL(_ejs,"index");
    if (url!=null) nodeInfo.setInfoURL(url);
    final DefaultMutableTreeNode ejsNode = new DefaultMutableTreeNode(nodeInfo);
    
    final JButton okButton = new JButton (res.getString("DigitalLibrary.Download"));
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) { 
        if (tree.getSelectionPath()!=null) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
          readNode(_ejs, node); 
        }
      }
    });
    okButton.setEnabled(false);

    treeModel = new DefaultTreeModel(ejsNode);
    tree = new JTree(treeModel);
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    tree.setCellRenderer(new MyRenderer());
    tree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node == null) return;
        DigitalLibraryNode userNodeInfo = (DigitalLibraryNode) node.getUserObject();
//        System.out.println ("Node = "+nodeInfo);
//        System.out.println ("InfoURL = "+nodeInfo.getInfoURL());
//        System.out.println ("Desc = "+nodeInfo.getDescription());
        
        if (userNodeInfo.getInfoURL()==null) htmlPane.setText(userNodeInfo.getDescription());
        else try {
          javax.swing.text.Document doc = htmlPane.getDocument();
          doc.putProperty(javax.swing.text.Document.StreamDescriptionProperty, null);
          htmlPane.setPage(userNodeInfo.getInfoURL()); 
        } 
        catch (Exception exc) { 
          htmlPane.setText(userNodeInfo.getDescription()); 
        }
        htmlPane.setCaretPosition(0);
        okButton.setEnabled(userNodeInfo.getDownloadURL()!=null);
      }
    });
    tree.addMouseListener(new MouseAdapter() {
      public void mousePressed (MouseEvent _e) {
        TreePath selPath = tree.getPathForLocation(_e.getX(), _e.getY());
        if (selPath==null) return;
        if (SwingUtilities.isLeftMouseButton(_e) && _e.getClickCount()>1) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) selPath.getLastPathComponent();
          readNode(_ejs,node);
        }
      }
    });
    
    //Create the scroll pane and add the tree to it. 
    JScrollPane treeView = new JScrollPane(tree);

    //Create the HTML viewing pane.
//    htmlPane = new JEditorPane();
    htmlPane = new JTextPane() {
      private static final long serialVersionUID = 1L;
      public void paintComponent(Graphics g) {
        if(OSPRuntime.antiAliasText) {
          Graphics2D g2 = (Graphics2D) g;
          RenderingHints rh = g2.getRenderingHints();
          rh.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
          rh.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        super.paintComponent(g);
      }
    };
    htmlPane.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if(e.getEventType()==HyperlinkEvent.EventType.ACTIVATED)
          org.opensourcephysics.desktop.OSPDesktop.displayURL(e.getURL().toString());
      }
    });
    htmlPane.setContentType ("text/html"); //; charset=UTF-16");
    htmlPane.setEditable(false);
    
    JScrollPane htmlView = new JScrollPane(htmlPane);
    tree.setSelectionRow(0);

    //Add the scroll panes to a split pane.
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    splitPane.setLeftComponent(treeView);
    splitPane.setRightComponent(htmlView);

    Dimension minimumSize = new Dimension(100, 50);
    htmlView.setMinimumSize(minimumSize);
    treeView.setMinimumSize(minimumSize);
    Dimension windowSize = res.getDimension("DigitalLibrary.Size");
    splitPane.setDividerLocation(windowSize.width/3); 
    splitPane.setPreferredSize(windowSize);
    
    JLabel dlLabel = new JLabel(res.getString("DigitalLibrary.ConnectTo"));
    dlLabel.setBorder(new javax.swing.border.EmptyBorder(0,3,0,3));

    final JComboBox dlCombo = new JComboBox();
    if (_ejs.getOptions().useCompadreDL()) dlCombo.addItem(new DigitalLibraryComPADRE());
    for (String library : _ejs.getOptions().getDigitalLibraries()) dlCombo.addItem(new DigitalLibraryPHP(library));
    
    dlCombo.addActionListener(new ActionListener() {
      public void actionPerformed (ActionEvent _evt) {
        DigitalLibrary dl = (DigitalLibrary) dlCombo.getSelectedItem();
        if (dl==null) {
          dlCombo.setSelectedIndex(0);
          dl = (DigitalLibrary) dlCombo.getItemAt(0);
        }
        if (additionalComponent!=null) topPanel.remove(additionalComponent);
        additionalComponent = dl.getAdditionalComponent(); 
        if (additionalComponent!=null) topPanel.add(additionalComponent,BorderLayout.SOUTH);
        topPanel.repaint();
        netDialog.validate();
        createNodes(_ejs,dl);
      }
    });
    
    JButton refreshButton = new JButton (res.getString("DigitalLibrary.Refresh"));
    refreshButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) { createNodes(_ejs,(DigitalLibrary)dlCombo.getSelectedItem()); }
    });

    disconnectButton = new JButton (res.getString("DigitalLibrary.Disconnect"));
    disconnectButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) { 
        treeModel.setRoot(ejsNode);
        disconnectButton.setEnabled(false);
        tree.setSelectionRow(0);
      }
    });
    disconnectButton.setEnabled(false);

    JButton cancelButton = new JButton (res.getString("SearchDialog.Close"));
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) { netDialog.setVisible(false); }
    });

    topPanel = new JPanel (new BorderLayout());
    topPanel.add(dlLabel, BorderLayout.WEST);
    topPanel.add(dlCombo, BorderLayout.CENTER);
    topPanel.add(refreshButton, BorderLayout.EAST);
    if (dlCombo.getItemCount()>0) {
      additionalComponent = ((DigitalLibrary) dlCombo.getItemAt(0)).getAdditionalComponent(); 
      if (additionalComponent!=null) topPanel.add(additionalComponent,BorderLayout.SOUTH);
    }

    JPanel buttonPanel = new JPanel (new FlowLayout(FlowLayout.CENTER));
    buttonPanel.add (okButton);
    buttonPanel.add (disconnectButton);
    buttonPanel.add (cancelButton);
    
    netDialog = new JDialog(_ejs.getMainFrame(),res.getString("DigitalLibrary.Title"));
    netDialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    netDialog.getContentPane().setLayout (new BorderLayout());
    netDialog.getContentPane().add(splitPane, BorderLayout.CENTER);
    netDialog.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    netDialog.getContentPane().add(topPanel, BorderLayout.NORTH);
    netDialog.validate();  
    netDialog.pack();
    netDialog.setLocationRelativeTo (_ejs.getMainPanel());
  }

  // --------------------------------------------
  // Actions 
  // --------------------------------------------

  static public void recreateGUI() {
    if (netDialog!=null) netDialog.setVisible(false);
    netDialog = null; 
  }
  
  static public void createNodes (Osejs _ejs, final DigitalLibrary _dl) {
    final DefaultMutableTreeNode top = _dl.getRootNode(_ejs);
    DigitalLibraryNode node = (DigitalLibraryNode) top.getUserObject();
    node.setIsTree(true);
    
    treeModel.setRoot(top);
    disconnectButton.setEnabled(true);
    tree.setSelectionRow(0);

    final JDialog tmpDialog = new JDialog(netDialog,res.getString("Information"));

    final Thread thread = new Thread(new Runnable() {
      public void run() {
        boolean ok = _dl.getCatalog(treeModel,top);
        tmpDialog.setVisible(false);
        tmpDialog.dispose();
        if (!ok) {
          JOptionPane.showMessageDialog(null,res.getString("DigitalLibrary.ServerNotAvailable"),
              res.getString("Osejs.File.Error"),JOptionPane.ERROR_MESSAGE);
          return;
        }
        tree.expandRow(0);
      }
    });
    thread.setPriority(Thread.NORM_PRIORITY);
    InterfaceUtils.showTempDialog(tmpDialog,res.getString("DigitalLibrary.Connecting")+" "+_dl.toString(),thread);
    tmpDialog.setVisible(true);
    thread.start();
  }

  /**
   * Reads a node or expands it if it is a non-expanded tree
   * @param _ejs
   * @param _node
   */
  static private void readNode(final Osejs _ejs, final DefaultMutableTreeNode _node) {
    final DigitalLibraryNode dlNode = (DigitalLibraryNode) _node.getUserObject();
    if (dlNode.getDownloadURL()==null) {
      if (dlNode.isToBeExpanded()) {
        final JDialog tmpDialog = new JDialog(netDialog,res.getString("Information"));
        final org.colos.ejs.library.control.value.BooleanValue displayDialog = new org.colos.ejs.library.control.value.BooleanValue(true);

        final Thread thread = new Thread(new Runnable() {
          public void run() {
            dlNode.expand(treeModel,_node,htmlPane);
            displayDialog.value = false;
            tmpDialog.setVisible(false);
            tmpDialog.dispose();
            if (_node.getChildCount()<=0) {
              JOptionPane.showMessageDialog(null,res.getString("DigitalLibrary.NoChildren"),
                  res.getString("Warning"),JOptionPane.ERROR_MESSAGE);
            }
            else SwingUtilities.invokeLater(new Runnable() {
                public void run() { tree.scrollPathToVisible(new TreePath(_node.getLastLeaf().getPath())); } 
//              try {}
//              catch (Exception exc) { exc.printStackTrace(); }; // sometimes I get an exception! 
            });
          }
        });
        thread.setPriority(Thread.NORM_PRIORITY);
        InterfaceUtils.showTempDialog(tmpDialog,res.getString("DigitalLibrary.Connecting")+" "+dlNode.getDigitalLibrary().toString(),thread);
        thread.start();
        ActionListener taskPerformer = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if (displayDialog.value) tmpDialog.setVisible(true);
//                else System.out.println ("Load was faster this time");
            }
        };
        javax.swing.Timer timer = new javax.swing.Timer(2000, taskPerformer);
        timer.setRepeats(false);
        timer.start();
      }
      return;
    }
    netDialog.setVisible(false);
    SwingUtilities.invokeLater(new Runnable() {
      public void run () { 
        try {
          URL url = new URL(dlNode.getDownloadURL());
          UnzipUtility.unzipWithWarning (_ejs,_ejs.getMainFrame(),dlNode.toString(),url.openStream());
        }
        catch (Exception exc) { 
          exc.printStackTrace();
          return;
        }
      }
    });
  }

  // --------------------------------------------
  // Utils
  // --------------------------------------------


  /**
   * Gets the localized HTML file for a filename (no .html) in the LibraryService directory
   */
  static public URL getLibraryServiceURL(Osejs _ejs, String _filename) {
    try {
      File file = new File(_ejs.getDocDirectory(),"LibraryService/"+_filename+"_"+Locale.getDefault().getLanguage()+".html");
      if (!file.exists()) file = new File(_ejs.getDocDirectory(),"LibraryService/"+_filename+".html");
      return file.toURI().toURL();
    } catch (Exception exc) { return null; }
  }

  /**
   * Gets the value of a node
   * @param _node
   * @return
   */
  static final public String getNodeValue(Node _node) {
    for (Node child = _node.getFirstChild(); child != null; child = child.getNextSibling() ){
      if (child.getNodeType()==Node.TEXT_NODE) return child.getNodeValue();
    }
    return null;
  }    

  /**
   * Gets the value of a subnode with a given name
   * @param _node
   * @return
   */
  static final public String getNodeValue(Node _parent, String _name) {
    Node node = getNode(_parent,_name);
    if (node!=null) return getNodeValue (node);
    return null;
  }    

  /**
   * Gets a node of the given name
   * @param _parent
   * @param _name
   * @return
   */
  static final public Node getNode(Node _parent, String _name) {
    NodeList childrenList = _parent.getChildNodes();
    for (int i=0,n=childrenList.getLength(); i<n; i++) {
      Node child = childrenList.item(i);
      if (child.getNodeName().equals(_name)) return child;
    }
    return null;
  }

  /**
   * Gets all nodes of a given name
   * @param _parent
   * @param _name
   * @return
   */
  static final public java.util.List<Node> getAllNodes(Node _parent, String _name) {
    java.util.List<Node> list = new ArrayList<Node>();
    NodeList childrenList = _parent.getChildNodes();
    for (int i=0,n=childrenList.getLength(); i<n; i++) {
      Node child = childrenList.item(i);
      if (child.getNodeName().equals(_name)) list.add(child);
    }
    return list;
  }

  /**
   * Returns the contents of a URL text file
   *
  static public String getTextFile (String urlAddress) {
    try {
      URL url = new URL(urlAddress);
      BufferedReader  in = new BufferedReader(new InputStreamReader(url.openStream()));
      String inputLine;
      StringBuffer buffer = new StringBuffer();
      while ((inputLine = in.readLine()) != null) buffer.append(inputLine);
      return buffer.toString();
    }
    catch (MalformedURLException mue) {}
    catch (IOException ioe) {}
    return null;
  }
*/
  
  /**
   * Removes HTML code for "&amp"
   * @param _url
   * @return
   */
  static public String processURL (String _url) {
    StringBuffer processed=new StringBuffer();
    int index = _url.indexOf("&amp;");
    while (index>=0) {
      processed.append(_url.subSequence(0,index+1));
      _url = _url.substring(index+5);
      index = _url.indexOf("&amp;");
    }
    processed.append(_url);
    return processed.toString();
  }


  /**
   * Retrieves a ZIP file from a URL addres, uncompresses it in a given 
   * directory, and opens it with EJS (if sucessfull).
   * @param _ejs
   * @param urlAddress
   * @param targetDirectory
   * @return
   *
  static private void unzipWithWarning (Osejs _ejs, String _name, String urlAddress, File targetDirectory) {
    JDialog tmpDialog = new JDialog(netDialog,res.getString("Information"));
    
    JLabel label = new JLabel (res.getString("Osejs.Init.ReadingFile")+" "+_name+"      ");
    label.setBorder(new javax.swing.border.EmptyBorder(10,10,10,10));
    label.setFont(label.getFont().deriveFont(14f));
    
    InputStream inputStream;
    try {
      URL url = new URL(urlAddress);
      inputStream = url.openStream();
    }
    catch (Exception exc) { 
      exc.printStackTrace();
      return;
    }
    final UnzipUtility runnable = new UnzipUtility(_ejs,inputStream,targetDirectory,label,tmpDialog);
    final Thread thread = new Thread(runnable);
    thread.setPriority(Thread.NORM_PRIORITY);

    JButton cancelButton = new JButton (res.getString("EditorFor.Cancel"));
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) { runnable.setCancelled(true); thread.interrupt(); }
    });
    JPanel panel = new JPanel (new FlowLayout(FlowLayout.CENTER));
    panel.add(cancelButton);

    tmpDialog.getContentPane().setLayout(new BorderLayout());
    tmpDialog.getContentPane().add(label,BorderLayout.CENTER);
    tmpDialog.getContentPane().add(panel,BorderLayout.SOUTH);
    tmpDialog.pack();
    Dimension size = netDialog.getSize();
    Dimension mysize = tmpDialog.getSize();
    Point loc = netDialog.getLocation();
    tmpDialog.setLocation(loc.x+(size.width-mysize.width)/2,loc.y+(size.height-mysize.height)/2);
    tmpDialog.setVisible(true);
    
    thread.start();
  }
  
  /*
  static private class DownloadRunnable implements Runnable {
    boolean canceled=false;
    Osejs ejs;
    File targetDirectory;
    String name,urlAddress;
    JLabel label;
    JDialog tmpDialog;
    
    DownloadRunnable(Osejs _ejs, String _urlAddress, File _targetDir, JLabel _label, JDialog _tmpDialog) {
      ejs = _ejs;
      targetDirectory = _targetDir;
      urlAddress = _urlAddress;
      label = _label;
      tmpDialog = _tmpDialog;
    }
    
    public void run() {
      String readingTxt = res.getString("Osejs.Init.ReadingFile")+" ";
      try {
        //System.out.println ("Unzipping "+urlAddress);
        URL url = new URL(urlAddress);
        InputStream in = url.openStream();
        BufferedInputStream bufIn = new BufferedInputStream(in);
        ZipInputStream input = new ZipInputStream(bufIn);
        ZipEntry zipEntry=null;
//        File xmlFile = null;
//        boolean multipleFiles=false;
        ArrayList<PathAndFile> pafList = new ArrayList<PathAndFile>();
        byte[] buffer = new byte[1024];
        int overwrite = JarTool.NO;
        while ( (zipEntry=input.getNextEntry()) != null) {
          if (canceled) {
            tmpDialog.setVisible(false);
            return;
          }
          if (zipEntry.isDirectory()) continue;
          label.setText(readingTxt+zipEntry.getName());
          //try { Thread.sleep(1000); } catch (Exception excc2) {};
          String filename = zipEntry.getName();
          File newFile = new File(targetDirectory,filename);
          if (newFile.exists()) {
            switch (overwrite) {
              case JarTool.NO_TO_ALL : continue;
              case JarTool.YES_TO_ALL : break; // will overwrite
              default :
                switch (overwrite = JarTool.confirmOverwrite(filename)) {
                  case JarTool.NO_TO_ALL:
                  case JarTool.NO : continue;
                  default : // Do nothing, i.e., will overwrite the file
                }
            }
          }
          newFile.getParentFile().mkdirs();
          int bytesRead;
          FileOutputStream output = new FileOutputStream (newFile);
          while ((bytesRead = input.read(buffer)) != -1) output.write(buffer, 0, bytesRead);
          output.close();
          input.closeEntry();
          if (OsejsCommon.isEJSfile(newFile)) pafList.add(new PathAndFile(filename,newFile));
        }
        input.close();
        tmpDialog.setVisible(false);
        int size = pafList.size();
        if (size>1) { // There are more than one models
          File file = chooseOne(ejs.getMainPanel(),res.getDimension("Package.ConfirmList.Size"), 
              res.getString("DigitalLibrary.ChooseOneModel"),res.getString("EditorFor.ChooseOne"), pafList, null);
          if (file!=null) ejs.readFile(file, false); // false = not merging
        }
        else if (size==1) ejs.readFile(pafList.get(0).getFile(), false); // Only one model was found
        else JOptionPane.showMessageDialog(ejs.getMainPanel(),res.getString("DigitalLibrary.NoXMLFound"),
            res.getString("Osejs.File.Error"),JOptionPane.ERROR_MESSAGE);
      } catch (Exception exc) { 
        tmpDialog.setVisible(false);
        exc.printStackTrace(); 
      }
    }
  }

  public static File chooseOne (Component _target, Dimension _size, 
      String _message, String _title, java.util.List<PathAndFile> _list, JComponent _bottomComponent) {
    class ReturnValue { boolean value = false; }
    final ReturnValue returnValue=new ReturnValue();

    DefaultListModel listModel = new DefaultListModel();
    for (int i=0,n=_list.size(); i<n; i++) listModel.addElement(_list.get(i));
    JList list = new JList(listModel);
    list.setEnabled(true);
    list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    list.setSelectionInterval(0,listModel.getSize()-1);
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

//    dialog.setSize (_size);
    dialog.validate();
    dialog.pack();
    dialog.setTitle (_title);
    dialog.setLocationRelativeTo (_target);
    dialog.setModal(true);

    dialog.setVisible (true);
    if (!returnValue.value) return null;
    PathAndFile paf = (PathAndFile) list.getSelectedValue();
    if (paf==null) return null;
    return paf.getFile();
  }

  */
  
  
  
  static class MyRenderer extends DefaultTreeCellRenderer {
    Icon openIcon,closedIcon;
    public MyRenderer() { 
      openIcon = this.getOpenIcon(); 
      closedIcon = this.getClosedIcon(); 
    }
    public Component getTreeCellRendererComponent (JTree _tree,Object _value,
                    boolean _sel, boolean _expanded, boolean _leaf, int _row, boolean _hasFocus) {
      super.getTreeCellRendererComponent(_tree, _value, _sel, _expanded, _leaf, _row, _hasFocus);
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) _value;
      DigitalLibraryNode nodeInfo = (DigitalLibraryNode) node.getUserObject();
      if (nodeInfo.isHTMLnode()) setIcon(HTML_ICON);
      else if (nodeInfo.isTree()) setIcon(_expanded ? openIcon:closedIcon);
      else setIcon(DOCUMENT_ICON);
      if (nodeInfo.isToBeExpanded()) setForeground(java.awt.Color.RED);
      return this;
    }
  }

}  // end of Class