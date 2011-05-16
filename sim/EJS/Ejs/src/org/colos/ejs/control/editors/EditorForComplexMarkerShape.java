/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.control.editors;

public class EditorForComplexMarkerShape extends EditorMultiuse {

  public static String edit (String _classname, String _property, String _type, javax.swing.JTextField returnField) {
    options = new String[] {"AMP_CURVE", "RE_IM_CURVE", "PHASE_CURVE", "PHASE_BAR", "PHASE_POST"};
    prefix = "ComplexMarkerShape";
    optionsPanel.setLayout(new java.awt.GridLayout(1,0,0,0));
    resetButtons();
    return EditorMultiuse.edit  (_classname, _property, _type, returnField);
  }

}