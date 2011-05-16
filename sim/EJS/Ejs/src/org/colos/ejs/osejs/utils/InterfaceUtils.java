/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.utils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.colos.ejs.library.control.ConstantParserUtil;

/**
 * Some utility functions
 */
public class InterfaceUtils {

  static private final ResourceUtil res    = new ResourceUtil("Resources");

  static public Font font (Font _currentFont, String _value) {
    Font font = ConstantParserUtil.fontConstant(_currentFont,_value);
    if (font==null) return _currentFont;
    return font;
  }

  static public Color color (String _value) {
    if (_value==null) return Color.black;
    Color color = ConstantParserUtil.colorConstant(_value);
    if (color==null) return Color.black;
    return color;
  }

  static public void showTempDialog (final JDialog _tmpDialog, String _message, final Thread _thread) {
    JLabel label = new JLabel (_message);
    label.setBorder(new javax.swing.border.EmptyBorder(10,10,10,10));
    label.setFont(label.getFont().deriveFont(14f));
    
    JButton cancelButton = new JButton (res.getString("EditorFor.Cancel"));
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent _evt) { 
        _tmpDialog.setVisible(false);
        _thread.interrupt(); 
      }
    });
    JPanel panel = new JPanel (new FlowLayout(FlowLayout.CENTER));
    panel.add(cancelButton);

    _tmpDialog.getContentPane().setLayout(new BorderLayout());
    _tmpDialog.getContentPane().add(label,BorderLayout.CENTER);
    _tmpDialog.getContentPane().add(panel,BorderLayout.SOUTH);
    _tmpDialog.pack();
    
    Window owner = _tmpDialog.getOwner();
    if (owner!=null){
      Point loc = owner.getLocation();
      Dimension size = owner.getSize();
      Dimension mysize = _tmpDialog.getSize();
      _tmpDialog.setLocation(loc.x+(size.width-mysize.width)/2,loc.y+(size.height-mysize.height)/2);
    }
  }


} // end of class
