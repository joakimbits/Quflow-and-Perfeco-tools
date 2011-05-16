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

public class DigitalLibraryPHP implements DigitalLibrary {
  static private final String PHP_COMMAND="/indexEJSdl.php";

  private String siteName, urlString;
  
  static private final ResourceUtil res = new ResourceUtil("Resources");
  
  public String toString() { return "EJS digital library at "+siteName; }

  public DigitalLibraryPHP (String _url) {
    siteName = _url;
    urlString = FileUtils.correctUrlString(_url);
  }
  
  public javax.swing.JComponent getAdditionalComponent() { return null; }

  public DefaultMutableTreeNode getRootNode(Osejs _ejs) {
    DigitalLibraryNode node = new DigitalLibraryNode (this,siteName,toString(),null);
    java.net.URL url=null;
    try {
      url = new java.net.URL(urlString+"/info.html");
      url.openStream();
    } 
    catch (Exception exc2) { 
      url = DigitalLibraryUtils.getLibraryServiceURL(_ejs,"EJS_dl"); 
    }
    node.setInfoURL(url);
    DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(node); 
    return rootNode;
  }
  
  public boolean getCatalog(javax.swing.tree.DefaultTreeModel _treeModel, DefaultMutableTreeNode _parentNode) { 
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      URL url = new URL(urlString+PHP_COMMAND);
      Document doc = factory.newDocumentBuilder().parse(url.openStream());
      NodeList list = doc.getChildNodes();
      for (int i=0,n=list.getLength(); i<n; i++) {
        if (! (list.item(i) instanceof Element)) continue;
        Element node = (Element) list.item(i);
        if (node.getNodeName().equals("dir")) {
          String dirUrl = node.getAttribute("url");
          addDirectory(_treeModel,_parentNode, dirUrl, node.getChildNodes());
        }
      }
      return true;
    } 
    catch(Exception e) { e.printStackTrace(); }
    return false;
  }
  
  private void addDirectory(javax.swing.tree.DefaultTreeModel _treeModel, DefaultMutableTreeNode _parentNode, String _dirUrl, NodeList _list) {
    for (int i=0,n=_list.getLength(); i<n;i++) {
      if (! (_list.item(i) instanceof Element)) continue;
      Element node = (Element) _list.item(i);
//      System.out.println ("Depth = "+_depth+" node = "+node.getNodeName()+ " type = "+node.getAttribute("type"));
      if (node.getNodeName().equals("dir")) {
        String name = node.getAttribute("name");
        String dirUrl = node.getAttribute("url");
        String html = node.getAttribute("html");
        DigitalLibraryNode nodeInfo = new DigitalLibraryNode (this,name,name,null);
        nodeInfo.setIsTree(true);
//        nodeInfo.setUserString(dirUrl);
        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(nodeInfo);
//        _parentNode.add(childNode);
        _treeModel.insertNodeInto(childNode, _parentNode, _parentNode.getChildCount());
        addDirectory (_treeModel,childNode,dirUrl,node.getChildNodes());
        try { if (html!=null && html.trim().length()>0) nodeInfo.setInfoURL(new URL(FileUtils.correctUrlString(dirUrl+html))); }
        catch (Exception exc) {exc.printStackTrace(); }
      }
      else if (node.getNodeName().equals("model")) {
        String name = DigitalLibraryUtils.getNodeValue(node);
        String html = node.getAttribute("html");
        DigitalLibraryNode nodeInfo = new DigitalLibraryNode (this,name,name,FileUtils.correctUrlString(_dirUrl+name+".zip"));
        try {
          if (html!=null && html.trim().length()>0) nodeInfo.setInfoURL(new URL(FileUtils.correctUrlString(_dirUrl+html)));
        } 
        catch (Exception exc) {exc.printStackTrace(); }
        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(nodeInfo);
//        _parentNode.add(childNode);
        _treeModel.insertNodeInto(childNode, _parentNode, _parentNode.getChildCount());
      }
      DigitalLibraryNode parentInfo = (DigitalLibraryNode) _parentNode.getUserObject();
      if (parentInfo.getInfoURL()==null && _parentNode.getChildCount()>0) { // list the subcategories and/or models
        List<DefaultMutableTreeNode> subcatList = new ArrayList<DefaultMutableTreeNode>();
        List<DefaultMutableTreeNode> modelList = new ArrayList<DefaultMutableTreeNode>();
        for (Enumeration<?> children = _parentNode.children(); children.hasMoreElements(); ) {
          DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
          DigitalLibraryNode childDLNode = (DigitalLibraryNode) child.getUserObject();
          if (childDLNode.isTree()) subcatList.add(child);
          else modelList.add(child);
        }
        StringBuffer listBuffer = new StringBuffer();
        listBuffer.append("<h2>"+_parentNode+"</h2>");
        if (subcatList.size()>0) {
          listBuffer.append("<p>"+res.getString("DigitalLibrary.ListOfSubcategories")+" "+_parentNode+":</p>\n");
          listBuffer.append("<ul>\n");
          for (DefaultMutableTreeNode subcat : subcatList) listBuffer.append("<li>"+subcat.toString()+"</li>\n");
          listBuffer.append("</ul>\n");
        }
        if (modelList.size()>0) {
          String catStr = res.getString("DigitalLibrary.Category");
          if (catStr.trim().length()>0) catStr = " "+ catStr;
          listBuffer.append("<p>"+res.getString("DigitalLibrary.ListOfModels")+" "+_parentNode+catStr+":</p>\n");
          listBuffer.append("<ul>\n");
          for (DefaultMutableTreeNode model : modelList) listBuffer.append("<li>"+model.toString()+"</li>\n");
          listBuffer.append("</ul>\n");
        }
        parentInfo.setDescription(listBuffer.toString());
      }

    }
  }
    
  public void expandNode(javax.swing.tree.DefaultTreeModel _treeModel, DefaultMutableTreeNode _treeNode, javax.swing.JEditorPane _htmlEditor) { }

}

