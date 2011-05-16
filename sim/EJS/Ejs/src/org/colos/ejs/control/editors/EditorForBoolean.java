/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.control.editors;

public class EditorForBoolean extends EditorMultiuse {

  public static String edit (String _classname, String _property, String _type, javax.swing.JTextField returnField) {
    options = new String[] {"true", "false"};
    prefix = "Boolean";
    optionsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER));
    resetButtons();
    return EditorMultiuse.edit  (_classname, _property, _type, returnField);
  }

}