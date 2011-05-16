/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.control.editors;

public class EditorForArrowPosition extends EditorMultiuse {

  public static String edit (String _classname, String _property, String _type, javax.swing.JTextField returnField) {
    options = new String[] {"NORTH_EAST", "CENTERED", "SOUTH_WEST"};
    prefix = "ArrowPosition";
    optionsPanel.setLayout(new java.awt.GridLayout(1,3,0,0));
    resetButtons();

    return EditorMultiuse.edit  (_classname, _property, _type, returnField);
  }

}
