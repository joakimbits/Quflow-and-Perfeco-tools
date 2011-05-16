/**
 * The edition package contains generic tools to edit
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition;

import org.colos.ejs.osejs.*;
import org.colos.ejs.osejs.utils.*;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import com.cdsc.eje.gui.*;

public class CodeEditor implements Editor {
  static protected ResourceUtil res = new ResourceUtil ("Resources");

  protected Osejs ejs;
  protected boolean changed=false, showTitle = true, visible=true;
  protected String name="",title,generateName="";
//  private JPopupMenu popup;
  protected JTextComponent textComponent;
  protected JTextField commentField;
  protected JScrollPane scrollPanel;
  protected TitledBorder titleBorder;
  protected JPanel mainPanel, commentPanel;
  protected TabbedEditor parentTabbedEditor;

  public CodeEditor (Osejs _ejs, TabbedEditor aTabbedEditor) {
    this(_ejs,aTabbedEditor,false);
  }

  public CodeEditor (Osejs _ejs, TabbedEditor aTabbedEditor, boolean _forceTitle) {
    ejs = _ejs;
    parentTabbedEditor = aTabbedEditor;
    if ((!_forceTitle) && "false".equals(res.getString("TabbedEditor.ShowTitles"))) showTitle = false;
    changed = false;

    MyDocumentListener MDL = new MyDocumentListener();
    textComponent = new EJEArea(_ejs);// editor.getTextArea();
    textComponent.getDocument().addDocumentListener(MDL);
    scrollPanel = new JScrollPane(textComponent);

    commentField = new JTextField();
    commentField.setEditable (true);
    commentField.setFont(InterfaceUtils.font(null,res.getString("Osejs.DefaultFont")));
    commentField.getDocument().addDocumentListener(MDL);

    JLabel commentLabel = new JLabel (res.getString ("Editor.Comment"));
    commentLabel.setFont (InterfaceUtils.font(null,res.getString("Editor.DefaultFont")));
    commentLabel.setBorder(new EmptyBorder(0,0,0,3));
    commentPanel = new JPanel (new BorderLayout());
    commentPanel.add (commentLabel,BorderLayout.WEST);
    commentPanel.add (commentField,BorderLayout.CENTER);

    mainPanel = new JPanel (new BorderLayout ());
    mainPanel.add(scrollPanel,BorderLayout.CENTER);
    mainPanel.add (commentPanel,BorderLayout.SOUTH);

    title = res.getString("CodeEditor.CodeFor").trim()+" ";
    titleBorder = new TitledBorder (new EmptyBorder(10,0,0,0),title+name);
    titleBorder.setTitleJustification (TitledBorder.LEFT);
    Font font = InterfaceUtils.font(null,res.getString("Editor.TitleFont"));
    titleBorder.setTitleFont (font);
    if (showTitle) {
      if (title.length()>0) mainPanel.setBorder (titleBorder);
      else mainPanel.setBorder (new EmptyBorder(10,5,5,5));
    }
    else mainPanel.setBorder (new EmptyBorder(5,2,0,2));
    new Undo2(textComponent,_ejs.getModelEditor());
  }

  public void addDocumentListener(DocumentListener _dl) {
    textComponent.getDocument().addDocumentListener(_dl);
    commentField.getDocument().addDocumentListener(_dl);
  }
  
  public JTextField getCommentField() { return commentField; }
  
  public JTextComponent getTextComponent() { return textComponent; }
  
  public java.util.List<SearchResult> search (String _info, String _searchString, int _mode) {
    java.util.List<SearchResult> list = new ArrayList<SearchResult>();
    boolean toLower = (_mode & SearchResult.CASE_INSENSITIVE) !=0;
    if (toLower) _searchString = _searchString.toLowerCase();
    if (_info==null) _info = "";
    else _info=res.getString(_info);//FKH 021020
    int lineCounter=1,caretPosition=0;
    StringTokenizer t = new StringTokenizer(textComponent.getText(), "\n",true);
    while (t.hasMoreTokens()) {
      String line = t.nextToken();
      int index;
      if (toLower) index = line.toLowerCase().indexOf(_searchString);
      else index = line.indexOf(_searchString);
      if (index>=0) list.add(createSearchResult(_info,line.trim(),lineCounter,caretPosition+index));
      caretPosition += line.length();
      lineCounter++;
    }
    return list;
  }

  protected SearchResult createSearchResult(String _info, String _line, int _lineCounter, int _pos) {
    return new CodeSearchResult(_info,_line,_lineCounter,_pos);
  }
  
  public void setName (String _newName) {
    name = _newName;
    if (title.length()>0) {
      titleBorder.setTitle (title+name);
      if (showTitle) mainPanel.setBorder (titleBorder);
      mainPanel.repaint();
    }
    setCodeName (name);
    changed = true;
  }

  public String getName() { return name; }

  public void clear () {
    textComponent.setText("");
    commentField.setText("");
  }

  public Component getComponent () { return mainPanel; }

  public void setColor (Color _color) { titleBorder.setTitleColor (_color); }

  public void setFont (Font _font) {
    textComponent.setFont (_font);
    //commentField.setFont (_font);
  }

  public void setEditable (boolean _editable) {
    textComponent.setEditable(_editable);
    commentField.setEditable(_editable);
  }

  public void setVisible (boolean _visible) { visible = _visible; }

  public boolean isVisible () { return visible; }

  public void refresh (boolean _hiddensToo) {
    scrollPanel.setVisible (visible || _hiddensToo);
  }

  public boolean isChanged () { return changed; }

  public void setChanged (boolean _ch) { changed = _ch; }

  public void setActive (boolean _active) {
    textComponent.setEditable (_active);
    textComponent.setEnabled (_active);
    commentField.setEditable(_active);
    commentField.setEnabled(_active);
    changed = true;
    activeEditor=_active;
  }
  private boolean activeEditor=true;//FKH 021024
  public boolean isActive () { return activeEditor;}
//FKH021024  public boolean isActive () { return textComponent.isEnabled (); }

 /**
  * Sets the name for code generation
  */
  public void setCodeName (String _name) { generateName = _name.trim().replace(' ','_'); }

  public StringBuffer generateCode (int _type, String _info) {
    StringBuffer code = new StringBuffer();
    switch (_type) {
      case Editor.GENERATE_ENABLED_CONDITION :
        code.append("  private boolean _isEnabled_" + generateName + " = "+isActive()+"; // Enabled condition for " + _info + "." + getName()+ "\n");
        break;
      case Editor.GENERATE_CHANGE_ENABLED_CONDITION :
        code.append("    if (\""+getName()+"\".equals(_pageName)) { _pageFound = true; _isEnabled_" + generateName + " = _enabled; } // Change enabled condition for " + _info + "." + getName()+ "\n");
        break;
      case Editor.GENERATE_RESET_ENABLED_CONDITION :
        code.append("    _isEnabled_" + generateName + " = "+isActive()+"; // Reset enabled condition for " + _info + "." + getName()+ "\n");
        break;
      case Editor.GENERATE_PLAINCODE : 
        code.append(splitCode(getName(),textComponent.getText(),_info,"      "));
        break;
      case Editor.GENERATE_CODE: 
        if (_info==null) _info = "";
        else _info=res.getString(_info);// looking for the info in the resources must be done after GENERATE_PLAINCODE
        code.append("  public void _" + generateName + " () { // > " + _info + "." + getName()+ "\n");
        code.append(splitCode(getName(),textComponent.getText(),_info,"    "));
        code.append("  }"+"  // > " + _info + "." + getName()+"\n\n");//FKH 021020
        break;
      case Editor.GENERATE_DECLARATION : 
        code.append("    if (_isEnabled_" + generateName+") _" + generateName + " ();\n");
        break;
    }
    return code;
  }

//  public void setCode (String _code) { textComponent.setText(_code); changed = false; }

  public StringBuffer saveStringBuffer () {
    return new StringBuffer("<Comment><![CDATA["+commentField.getText()+"]]></Comment>\n"
      + "<Code><![CDATA[\n"+textComponent.getText()+"\n]]></Code>\n");
  }

  public void readString (String _input) {
//    _input = _input.trim();
    String comment = OsejsCommon.getPiece(_input,"<Comment><![CDATA[","]]></Comment>",false);
    commentField.setText(comment);
    String code = OsejsCommon.getPiece(_input,"<Code><![CDATA[\n","\n]]></Code>",false);
    textComponent.setText(code);
    textComponent.setCaretPosition(0);
    // beautifyCode(0,code.length());
  }

  // --- methods and classes

  /**
   * Removes comments of the form / * -- * / and of the form //
   */
  static public String removeComments (String _code) {
    StringBuffer buffer = new StringBuffer();
    // remove the /* */ comments
    int pos=0, start = _code.indexOf("/*");
    while (start>=0) {
      buffer.append(_code.substring(pos,start));
      pos = _code.indexOf("*/",start);
      if (pos>0) { pos += 2; start = _code.indexOf("/*",pos); }
      else start = -1;
    }
    if (pos>=0) buffer.append(_code.substring(pos));
    // Now remove the // comments
    StringTokenizer tkn = new StringTokenizer(buffer.toString(),"\n",true);
    buffer = new StringBuffer();
    while (tkn.hasMoreTokens()) {
      String line = tkn.nextToken();
      int index = line.indexOf("//");
      if (index<0) buffer.append(line);
      else buffer.append(line.substring(0,index));
    }
    return buffer.toString();
  }
  
  
  public static StringBuffer splitCode (String _name, String _code, String _info, String _prefix) {
    return splitCode (_name,_code, _info, _prefix, false);
  }

  public static StringBuffer splitCode (String _name, String _codeStr, String _info, String _prefix, boolean _noComments) {
    StringBuffer splitCode = new StringBuffer ();
    String externalApp=null;
    boolean externalCode = false;
    if (_noComments) _codeStr = removeComments (_codeStr);
    
    int lineNumber = 1;
    StringTokenizer tkn = new StringTokenizer(_codeStr, "\n");
    while (tkn.hasMoreTokens()) {
      String line = tkn.nextToken();
      if (line.trim().startsWith("% BEGIN CODE")) {
        externalCode = true;
        int index = line.trim().indexOf(':');
        if (index<0) externalApp = null;
        else {
          externalApp = line.trim().substring(index + 1).trim();
          if (externalApp.length()<=0) externalApp = null;
        }
        continue;
      }
      else if (line.trim().startsWith("% END CODE")) {
        externalCode = false;
        continue;
      }

      if (externalCode) {
        if (externalApp!=null) splitCode.append(_prefix + " _external.getApplication(\""+externalApp+"\")");
        else                   splitCode.append(_prefix + " _external");
        splitCode.append(".eval(\"" + line + "\");");
        if (_info!=null) splitCode.append(" // > " + _info + "." + _name + ":" + lineNumber + "\n");
        else splitCode.append("\n");
        lineNumber++;
      }
      else if (_noComments) {
        if (line.trim().startsWith("//")) continue;
        splitCode.append(line + "\n");
        lineNumber++;
      }
      else {
        splitCode.append(_prefix + line);
        if (_info!=null) splitCode.append("  // > " + _info + "." + _name + ":" + lineNumber + "\n");
        else splitCode.append("\n");
        lineNumber++;
      }
    }
    return splitCode;
  }

  class MyDocumentListener implements DocumentListener {
    public void changedUpdate (DocumentEvent evt)  { changed = true; }
    public void insertUpdate  (DocumentEvent evt)  { changed = true; }
    public void removeUpdate  (DocumentEvent evt)  { changed = true; }
  }

  class CodeSearchResult extends SearchResult {
    public CodeSearchResult (String anInformation, String aText, int aLineNumber, int aCaretPosition) {
      super (anInformation+"."+getName(),aText,textComponent,aLineNumber,aCaretPosition);
    }

    public void show () {
      if (parentTabbedEditor!=null) parentTabbedEditor.showPage(CodeEditor.this);
      super.show();
    }
  }

} // end of class
