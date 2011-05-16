/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * Last Revision: March 2008
 */

package org.colos.ejs.osejs.utils;

import java.net.URL;
import java.util.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.parsers.*;

import org.colos.ejs.osejs.Osejs;
import org.w3c.dom.*;

public class DigitalLibraryComPADRE implements DigitalLibrary {
  static private final String SERVER_TREE="http://www.compadre.org/osp/services/REST/osp_jars.cfm?verb=Identify&OSPType=EJS%20Model&AttachedDocument=Source%20Code";
  static private final String SERVER_RECORDS="http://www.compadre.org/osp/services/REST/osp_jars.cfm?OSPType=EJS%20Model&AttachedDocument=Source%20Code";

  static private final ResourceUtil res = new ResourceUtil("Resources");

  private javax.swing.JCheckBox mainClassificationOnlyCB;
  
  public DigitalLibraryComPADRE() {
    mainClassificationOnlyCB  = new javax.swing.JCheckBox(res.getString("DigitalLibrary.DuplicateClassification"),true);
  }
  
  
  public String toString() { return "OSP collection in the comPADRE digital library"; }

  public javax.swing.JComponent getAdditionalComponent() { return mainClassificationOnlyCB; }

  public DefaultMutableTreeNode getRootNode(Osejs _ejs) {
    DigitalLibraryNode node = new DigitalLibraryNode (this,"OSP Digital Library Models","OSP collection in the comPADRE digital library",null);
    try { node.setInfoURL(new java.net.URL("http://www.compadre.org/OSP/online_help/EjsDL/DLModels.html")); } 
    catch (Exception exc2) {}
    DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(node); 
    DigitalLibraryNode firstLeaf = new DigitalLibraryNode(this,"About OSP and comPADRE" ,null,null);
    try { firstLeaf.setInfoURL(new java.net.URL("http://www.compadre.org/OSP/online_help/EjsDL/OSPCollection.html")); }
    catch (Exception exc2) {}
    firstLeaf.setToBeExpanded(false);
    firstLeaf.setHTMLnode(true);
    rootNode.add(new DefaultMutableTreeNode(firstLeaf));
    return rootNode;
  }
  
  public boolean getCatalog(javax.swing.tree.DefaultTreeModel _treeModel, DefaultMutableTreeNode _parentNode) { 
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      String urlStr = SERVER_TREE;
      if (mainClassificationOnlyCB.isSelected()) urlStr += "&OSPPrimary=Subject";
      URL url = new URL(urlStr);
      Document doc = factory.newDocumentBuilder().parse(url.openStream());
      NodeList list = doc.getElementsByTagName("Identify");
      for (int i=0,n=list.getLength(); i<n; i++) addSubtrees(_treeModel,_parentNode, list.item(i).getChildNodes(),"osp-subject",1,"");
      return true;
    } 
    catch(Exception e) { e.printStackTrace(); }
    return false;
  }
  
  private void addSubtrees(javax.swing.tree.DefaultTreeModel _treeModel, DefaultMutableTreeNode _parentNode, NodeList _list,String _type, int _depth, String _serviceParameter) {
    DigitalLibraryNode dlNode = (DigitalLibraryNode) _parentNode.getUserObject();
    String dbClickStr = "<p>"+res.getString("DigitalLibrary.DoubleClick")+" ";
    String catStr = res.getString("DigitalLibrary.Category");
    if (catStr.trim().length()>0) catStr = " "+ catStr+".</p>";
    else catStr = catStr+".</p>";
    
    for (int i=0,n=_list.getLength(); i<n;i++) {
      if (! (_list.item(i) instanceof Element)) continue;
      Element node = (Element) _list.item(i);
//      System.out.println ("Depth = "+_depth+" node = "+node.getNodeName()+ " type = "+node.getAttribute("type"));
      if (node.getNodeName().equals("sub-tree-set") && _type.equals(node.getAttribute("type")) ) {
        List<Node> subTrees = DigitalLibraryUtils.getAllNodes(node, "sub-tree"); //node.getChildNodes();
        StringBuffer listBuffer = null;
        if (subTrees.size()>0) {
          listBuffer = new StringBuffer();
          listBuffer.append("<p>"+res.getString("DigitalLibrary.ListOfSubcategories")+" "+dlNode+":</p>\n");
          listBuffer.append("<ul>\n");
        }
        String unclassifiedNodeURL = null;
        if (listBuffer!=null) for (int j=0,m=subTrees.size();j<m;j++) {
          if (! (subTrees.get(j) instanceof Element)) continue;
          Element subtree = (Element) subTrees.get(j);
          String name = subtree.getAttribute("name");
          String serviceParam = subtree.getAttribute("service-parameter");
          serviceParam = _serviceParameter+"&"+FileUtils.correctUrlString(serviceParam);
          DigitalLibraryNode nodeInfo = new DigitalLibraryNode (this,name,name,null);
          if (name.equals("Unclassified")) { // The unclassified node is processed last and adds its models to the parent
//            System.out.println ("Unclassified in "+_parentNode);
            unclassifiedNodeURL = serviceParam;
            continue;
          }
          listBuffer.append("<li>"+name+"</li>\n");
          nodeInfo.setIsTree(true);
          String description = DigitalLibraryUtils.getNodeValue(subtree,"description");
          if (description!=null) nodeInfo.setUserString("<p>"+description+"</p>");
          DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(nodeInfo);
          if (DigitalLibraryUtils.getAllNodes(subtree,"sub-tree-set").size()<=0) { // has no subtree: i.e. it is a final node with models
            nodeInfo.setToBeExpanded(true);
            if (nodeInfo.getUserString()==null) nodeInfo.setDescription(dbClickStr+name+catStr);
            else nodeInfo.setDescription(nodeInfo.getUserString()+dbClickStr+name+catStr);
            nodeInfo.setExpansionInfo(serviceParam);
//            Thread thread = new Thread(new Runnable() {
//              public void run() {
//                expandNode(childNode);
//              }
//            });
//            thread.setPriority(Thread.MIN_PRIORITY);
//            thread.start();
          }
//          _parentNode.add(childNode);
          _treeModel.insertNodeInto(childNode, _parentNode, _parentNode.getChildCount());
          addSubtrees(_treeModel,childNode,subtree.getChildNodes(),_type+"-detail",_depth+1,serviceParam);
        }
        if (listBuffer!=null) listBuffer.append("</ul>\n");
        if (unclassifiedNodeURL!=null) {
          dlNode.setToBeExpanded(true);
          dlNode.setExpansionInfo(unclassifiedNodeURL);
//          List<DefaultMutableTreeNode> modelList = getModels (_parentNode, unclassifiedNode.getExpansionInfo());
//          if (modelList.size()>0) {
//            listBuffer.append("</ul>\n");
//            listBuffer.append("<p>"+res.getString("DigitalLibrary.ListOfModels")+" "+dlNode.toString()+" "
//                +res.getString("DigitalLibrary.Category")+":</p>\n");
//            listBuffer.append("<ul>\n");
//            for (DefaultMutableTreeNode model : modelList) listBuffer.append("<li>"+model.toString()+"</li>\n");
//          }
        }
        if (listBuffer!=null) {
          String newUserString = dlNode.getUserString()==null? listBuffer.toString() :  dlNode.getUserString()+listBuffer.toString();
          dlNode.setUserString(newUserString);
          if (unclassifiedNodeURL!=null) dlNode.setDescription(newUserString+dbClickStr+dlNode.toString()+catStr);
          else dlNode.setDescription(newUserString);
        }
      } 
    }
  }
    
  public void expandNode(javax.swing.tree.DefaultTreeModel _treeModel, DefaultMutableTreeNode _treeNode, javax.swing.JEditorPane _htmlEditor) {
    DigitalLibraryNode dlNode = (DigitalLibraryNode) _treeNode.getUserObject();
    if (!dlNode.isToBeExpanded()) return;
    dlNode.setToBeExpanded(false);
    // Read the nodes and add them to this node
    List<DefaultMutableTreeNode> modelList = getModels (_treeModel, _treeNode, dlNode.getExpansionInfo()); 
    if (modelList.size()>0) { // Create an html page with a listing of models for this node
      StringBuffer listBuffer = new StringBuffer();
      if (dlNode.getUserString()!=null) listBuffer.append(dlNode.getUserString());
      String catStr = res.getString("DigitalLibrary.Category");
      if (catStr.trim().length()>0) catStr = " "+ catStr;
      listBuffer.append("<p>"+res.getString("DigitalLibrary.ListOfModels")+" "+dlNode.toString()+catStr+":</p>\n");
      listBuffer.append("<ul>\n");
      for (DefaultMutableTreeNode model : modelList) listBuffer.append("<li>"+model.toString()+"</li>\n");
      listBuffer.append("</ul>\n");
      dlNode.setDescription(listBuffer.toString());
    }
    else { // default html page
      if (dlNode.getUserString()!=null) dlNode.setDescription(dlNode.getUserString());
      else dlNode.setDescription(dlNode.toString());
    }
    _htmlEditor.setText(dlNode.getDescription());
  }

  /**
   * Returns a list of tree nodes for the models at a given location
   * @param _parentNode The node at which the model will be added
   * @param _urlString The url that gives the list of models
   * @return
   */
  private List<DefaultMutableTreeNode> getModels (javax.swing.tree.DefaultTreeModel _treeModel, DefaultMutableTreeNode _parentNode, String _urlString) {
    List<DefaultMutableTreeNode> modelList = new ArrayList<DefaultMutableTreeNode>();
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      String urlBase = SERVER_RECORDS;
      if (mainClassificationOnlyCB.isSelected()) urlBase += "&OSPPrimary=Subject";
//System.out.println ("Command is "+urlBase+_urlString);
      URL url = new URL(urlBase+_urlString);
      Document doc = factory.newDocumentBuilder().parse(url.openStream());
      String authorTitle = res.getString("EjsOptions.Author");
      String sizeTitle = res.getString("DigitalLibrary.DownloadSize");
      String infoFieldTitle = res.getString("DigitalLibrary.InfoField");

      // Construct the full list location of the model
      String parentList = "";
      javax.swing.tree.TreeNode rootNode = _parentNode;
      while (rootNode!=null) {
        if (rootNode.getParent()!=null) parentList = rootNode.toString()+": " + parentList;
        rootNode = rootNode.getParent();
      }
      
      NodeList list = doc.getElementsByTagName("record");
      for (int i=0,n=list.getLength(); i<n; i++) { // Process records
        Node record = list.item(i);
        NodeList childrenList = record.getChildNodes();
        TwoStrings attachment = getAttachment (childrenList);
        if (attachment==null || attachment.getFirstString()==null) continue; // No source code for this record
        // Extract information
        String name = DigitalLibraryUtils.getNodeValue(DigitalLibraryUtils.getNode(record,"title")); 
        String downloadURL = DigitalLibraryUtils.processURL(attachment.getFirstString()); 
        String description = DigitalLibraryUtils.getNodeValue(DigitalLibraryUtils.getNode(record,"description"));
        String infoField = DigitalLibraryUtils.getNodeValue(DigitalLibraryUtils.getNode(record,"information-url"));
        String thumbnailURL = DigitalLibraryUtils.getNodeValue(DigitalLibraryUtils.getNode(record,"thumbnail-url"));
        String authorField = "";
        for (Node node : DigitalLibraryUtils.getAllNodes(DigitalLibraryUtils.getNode(record,"contributors"),"contributor")) {
          Element el = (Element) node;
          if ("Author".equals(el.getAttribute("role"))) authorField += DigitalLibraryUtils.getNodeValue(node)+", ";
        }
        if (authorField.endsWith(", ")) authorField = authorField.substring(0,authorField.length()-2);
        // Create the tree node
        StringBuffer buffer = new StringBuffer();
        buffer.append ("<p align=\"center\"><img src=\""+thumbnailURL+"\" alt=\""+name+"\"></p>");
        buffer.append ("<p><b>"+parentList+"</b></p>");
        buffer.append ("<h2>"+name+"</h2>");
        if (authorField.length()>0) buffer.append ("<p><b>"+authorTitle+":</b> "+ authorField+"</p>");
        StringTokenizer tkn = new StringTokenizer(description,"\n");
        while (tkn.hasMoreTokens()) buffer.append("<p>"+tkn.nextToken()+"</p>");
        buffer.append ("<p><b>"+infoFieldTitle+"</b><br><a href=\""+infoField+"\">"+infoField+"</a></p>");
        buffer.append ("<p><b>"+sizeTitle+"</b> "+attachment.getSecondString()+" bytes</p>");
        DefaultMutableTreeNode modelNode = new DefaultMutableTreeNode(new DigitalLibraryNode(this,name,buffer.toString(),downloadURL));
//        _parentNode.add(modelNode);  // Using this causes redisplay problems
        _treeModel.insertNodeInto(modelNode, _parentNode, _parentNode.getChildCount());
//        System.out.println ("Added node "+modelNode+" to parent "+_parentNode);
        modelList.add(modelNode);

      }
    }
    catch(Exception e) { e.printStackTrace(); }
    return modelList;
  }
  
  
  /**
   * Returns tow strings with the source code attachment or null if no such attachment is found
   * @param childrenList
   * @return
   */
  static private TwoStrings getAttachment (NodeList _childrenList) {
    for (int i=0,n=_childrenList.getLength(); i<n; i++) {
      Node child = _childrenList.item(i);
      if (!child.getNodeName().equals("attached-document")) continue;
      Node fileTypeNode = DigitalLibraryUtils.getNode(child,"file-type");
      if (fileTypeNode!=null && "Source Code".equals(DigitalLibraryUtils.getNodeValue(fileTypeNode))) {
        Node urlNode = DigitalLibraryUtils.getNode(child,"download-url");
        if (urlNode!=null) {
          String attachmentURL = DigitalLibraryUtils.getNodeValue(urlNode);
          Element sizeNode = (Element) DigitalLibraryUtils.getNode(child,"file-name");
          if (sizeNode!=null) return new TwoStrings(attachmentURL,sizeNode.getAttribute("file-size"));
          return new TwoStrings(attachmentURL,null);
        }
      }
    }
    return null;
  }

}

