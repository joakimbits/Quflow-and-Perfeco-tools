/*
 * The control.displayejs package contains subclasses of
 * control.ControlElement that deal with the displayejs package
 * Copyright (c) October 2003 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.library.control.displayejs;

import org.colos.ejs.library.control.value.Value;
import org.opensourcephysics.displayejs.*;
import org.opensourcephysics.display.Drawable;

/**
 * An interactive particle
 */
public class ControlPoints extends ControlInteractiveElement {

  protected Drawable createDrawable () {
    return new InteractivePoints();
  }

  protected int getPropertiesDisplacement () { return 0; }

// ------------------------------------------------
// Properties
// ------------------------------------------------

  static java.util.List<String> infoList=null;

  public java.util.List<String> getPropertyList() {
    if (infoList==null) {
      infoList = new java.util.ArrayList<String> ();
      infoList.add ("data");
      infoList.addAll(super.getPropertyList());
    }
    return infoList;
  }

  public String getPropertyInfo(String _property) {
    if (_property.equals("data"))             return "double[][]";
    return super.getPropertyInfo(_property);
  }

// ------------------------------------------------
// Set and Get values
// ------------------------------------------------

  public void setValue (int _index, Value _value) {
    switch (_index) {
      case 0 : if (_value.getObject() instanceof double[][]) ((InteractivePoints) myElement).setData((double[][])_value.getObject()); break;
      default : super.setValue(_index-1,_value); break;
    }
  }

  public void setDefaultValue (int _index) {
    switch (_index) {
      case 0 : ((InteractivePoints) myElement).setData(null); break;
      default : super.setDefaultValue(_index-1); break;
    }
  }

  public String getDefaultValueString (int _index) {
    switch (_index) {
      case  0 : return "<none>";
      default : return super.getDefaultValueString(_index-1);
    }
  }

} // End of class
