/*
 * The control.swing package contains subclasses of control.ControlElement
 * that create visuals using Java's Swing library
 * Copyright (c) June 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.swing;

import org.colos.ejs.library.control.value.*;
import javax.swing.*;
import java.awt.Color;

/**
 * A configurable control button. It will trigger an action when clicked.
 * It has no internal value.
 */
public class ControlTwoStateButton extends ControlSwingElement {
  static protected final int TWO_STATE_ADDED = 12;
  static protected final int VARIABLE = 11;
  static protected final int MY_FOREGROUND = FOREGROUND+TWO_STATE_ADDED;
  static protected final int MY_BACKGROUND = BACKGROUND+TWO_STATE_ADDED;

  protected JButton button;
//  private boolean on = true;
  private String imageFileOn = null, imageFileOff=null;
  private Icon iconOn = null, iconOff=null;
  private String textOn="", textOff="";
  private char mnemonicOn=(char) -1, mnemonicOff=(char) -1;
  private Color frgdOn=null, frgdOff=null, bkgdOn=null, bkgdOff=null;
  protected BooleanValue internalValue=new BooleanValue(true);


// ------------------------------------------------
// Visual component
// ------------------------------------------------

  protected java.awt.Component createVisual () {
      button = new JButton();
      button.setText(textOn);
    frgdOn = frgdOff = button.getForeground();
    bkgdOn = bkgdOff = button.getBackground();

    button.addActionListener (
      new java.awt.event.ActionListener() {
        public void actionPerformed (java.awt.event.ActionEvent _e) {
          invokeActions(internalValue.value ? ControlSwingElement.ACTION_ON : ControlSwingElement.ACTION_OFF);
          internalValue.value = !internalValue.value;
          variableChanged (VARIABLE,internalValue);
          if (isUnderEjs) setFieldListValue(VARIABLE,internalValue);
          updateGUI();
        }
      }
    );
    return button;
  }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  static private java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String>();
      infoList.add ("textOn");
      infoList.add ("textOff");
      infoList.add ("imageOn");
      infoList.add ("imageOff");
      infoList.add ("actionOn");
      infoList.add ("actionOff");
      infoList.add ("mnemonicOn");
      infoList.add ("mnemonicOff");
      infoList.add ("foregroundOff");
      infoList.add ("backgroundOff");
      infoList.add ("alignment");
      infoList.add ("variable");

      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("textOn"))      return "String NotTrimmed TRANSLATABLE";
    if (_property.equals("textOff"))     return "String NotTrimmed TRANSLATABLE";
    if (_property.equals("imageOn"))     return "File|String TRANSLATABLE";
    if (_property.equals("imageOff"))    return "File|String TRANSLATABLE";
    if (_property.equals("actionOn"))    return "Action CONSTANT";
    if (_property.equals("actionOff"))   return "Action CONSTANT";
    if (_property.equals("mnemonicOn"))  return "String  TRANSLATABLE";
    if (_property.equals("mnemonicOff")) return "String  TRANSLATABLE";
    if (_property.equals("foregroundOff")) return "Color|Object";
    if (_property.equals("backgroundOff")) return "Color|Object";
    if (_property.equals("alignment"))   return "Alignment|int";
    if (_property.equals("variable"))    return "boolean";

    return super.getPropertyInfo(_property);
  }

  private void updateGUI() {
    button.setText(internalValue.value ? textOn : textOff);
    button.setIcon(internalValue.value ? iconOn : iconOff);
    button.setMnemonic(internalValue.value ? mnemonicOn : mnemonicOff);
    button.setForeground(internalValue.value ? frgdOn : frgdOff);
    button.setBackground(internalValue.value ? bkgdOn : bkgdOff);
  }

// ------------------------------------------------
// Set and Get the values of the properties
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : textOn  = org.opensourcephysics.display.TeXParser.parseTeX(_value.getString()); if (internalValue.value)  button.setText(textOn);  break;
      case 1 : textOff = org.opensourcephysics.display.TeXParser.parseTeX(_value.getString()); if (!internalValue.value) button.setText(textOff); break;
      case 2 :
        if (_value.getString().equals(imageFileOn)) return;
        iconOn = getIcon(imageFileOn = _value.getString());
        if (internalValue.value) button.setIcon (iconOn);
        break;
      case 3 :
        if (_value.getString().equals(imageFileOff)) return;
        iconOff = getIcon(imageFileOff = _value.getString());
        if (!internalValue.value) button.setIcon (iconOff);
        break;
      case 4 : // actionOn
        removeAction (ControlSwingElement.ACTION_ON,getProperty("actionOn"));
        addAction(ControlSwingElement.ACTION_ON,_value.getString());
        break;
      case 5 : // actionOff
        removeAction (ControlSwingElement.ACTION_OFF,getProperty("actionOff"));
        addAction(ControlSwingElement.ACTION_OFF,_value.getString());
        break;
      case 6 : mnemonicOn  = _value.getString().charAt(0); if (internalValue.value)  button.setMnemonic(mnemonicOn);  break;
      case 7 : mnemonicOff = _value.getString().charAt(0); if (!internalValue.value) button.setMnemonic(mnemonicOff); break;
      case 8 :
        if (_value.getObject() instanceof Color) {
          frgdOff = (Color) _value.getObject();
          if (!internalValue.value) button.setForeground(frgdOff);
        }
        break;
      case 9 : // Background
        if (_value.getObject() instanceof Color) {
          bkgdOff = (Color) _value.getObject();
          if (!internalValue.value) button.setBackground(bkgdOff);
        }
        break;
      case 10 : button.setHorizontalAlignment(_value.getInteger()); break; // alignment
      case VARIABLE : internalValue.value = _value.getBoolean(); updateGUI(); break;
      default: super.setValue(_index-TWO_STATE_ADDED,_value); break;
      case MY_FOREGROUND :
        if (_value.getObject() instanceof Color) {
          frgdOn = (Color) _value.getObject();
          if (internalValue.value) button.setForeground(frgdOn);
        }
        break;
      case MY_BACKGROUND :
        if (_value.getObject() instanceof Color) {
          bkgdOn = (Color) _value.getObject();
          if (internalValue.value) button.setBackground(bkgdOn);
        }
        break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : textOn  = "";  if (internalValue.value)  button.setText(textOn);  break;
      case 1 : textOff = "";  if (!internalValue.value) button.setText(textOff); break;
      case 2 : imageFileOn  = null; iconOn  = null; if (internalValue.value)  button.setIcon (iconOn);  break;
      case 3 : imageFileOff = null; iconOff = null; if (!internalValue.value) button.setIcon (iconOff); break;
      case 4 : removeAction (ControlSwingElement.ACTION_ON, getProperty("actionOn"));  break;
      case 5 : removeAction (ControlSwingElement.ACTION_OFF,getProperty("actionOff")); break;
      case 6 : mnemonicOn  = (char) -1; if (internalValue.value)  button.setMnemonic(mnemonicOn);  break;
      case 7 : mnemonicOff = (char) -1; if (!internalValue.value) button.setMnemonic(mnemonicOff); break;
      case 8 : frgdOff = myDefaultFrgd; if (!internalValue.value) button.setForeground(frgdOff); break;
      case 9 : bkgdOff = myDefaultBkgd; if (!internalValue.value) button.setBackground(bkgdOff); break;
      case 10 : button.setHorizontalAlignment(SwingConstants.CENTER); break;
      case VARIABLE : internalValue.value = true; updateGUI(); break;
      default: super.setDefaultValue(_index-TWO_STATE_ADDED); break;
      case MY_FOREGROUND : frgdOn = myDefaultFrgd; if (internalValue.value) button.setForeground(frgdOn); break;
      case MY_BACKGROUND : bkgdOn = myDefaultBkgd; if (internalValue.value) button.setBackground(bkgdOn); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case 0 : case 1 : 
      case 2 : case 3 : return "<none>";
      case 4 : case 5 : return "<no_action>";
      case 6 : case 7 : 
      case 8 : case 9 : return "<none>";
      case 10 : return "CENTER";
      case VARIABLE : return "true";
      default : return super.getDefaultValueString(_index-TWO_STATE_ADDED);
    }
  }

  public Value getValue (int _index) {
    switch (_index) {
      case 0 : case 1 : case 2 : case 3 :
      case 4 : case 5 : case 6 : case 7 :
      case 8 : case 9 : case 10 :
        return null;
      case VARIABLE : return internalValue;
      default: return super.getValue(_index-TWO_STATE_ADDED);
    }
  }

} // End of class
