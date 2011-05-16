/**
 * The edition package contains generic tools to edit
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition.ode_editor;

import org.colos.ejs.osejs.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.*;
import org.colos.ejs.osejs.edition.CodeEditor;

class ZenoEditor extends CodeEditor {

  private JLabel zenoActionLabel;
  private JCheckBox zenoStopAfterEffect;

  public ZenoEditor (Osejs _ejs) {
    super (_ejs,null);
    zenoStopAfterEffect = new JCheckBox(res.getString("EquationEditor.Events.ZenoStopAtEffect"),true);
    zenoStopAfterEffect.addItemListener(new ItemListener(){
      public void itemStateChanged(ItemEvent arg0) { changed = true; }
    }); 

    zenoActionLabel = new JLabel(res.getString ("EquationEditor.Events.ZenoAction"));
    zenoActionLabel.setBorder (EquationEditor.BORDER_SMALL);

    JPanel actionRow = new JPanel(new BorderLayout());
    actionRow.add(zenoActionLabel,BorderLayout.WEST);
    actionRow.add(zenoStopAfterEffect,BorderLayout.EAST);

    textComponent.requestFocus();
    mainPanel.add(actionRow, BorderLayout.NORTH);
  }

  public void setColor (Color _color) {
    zenoActionLabel.setForeground(_color);
    zenoStopAfterEffect.setForeground(_color); 
    super.setColor(_color);
  }

  public void setEditable (boolean _editable) {
    zenoStopAfterEffect.setEnabled(_editable);
    super.setEditable(_editable);
  }

  public void setActive (boolean _active) {
    zenoStopAfterEffect.setEnabled(_active);
    super.setActive(_active);
  }

  public StringBuffer saveStringBuffer () {
    StringBuffer buffer = super.saveStringBuffer();
    buffer.append("<StopAfterEffect>"+zenoStopAfterEffect.isSelected()+"</StopAfterEffect>\n");
    return buffer;
  }

  public void readString (String _input) {
    if ("true".equals(OsejsCommon.getPiece(_input,"<StopAfterEffect>","</StopAfterEffect>",false))) zenoStopAfterEffect.setSelected(true);
    else zenoStopAfterEffect.setSelected(false);
    super.readString(_input);
    textComponent.requestFocus();
  }

  public boolean isSelected() { return zenoStopAfterEffect.isSelected(); }
  
//  public StringBuffer generateCode (int _type, String _info) {
//    if (_type==Editor.GENERATE_PLAINCODE) {
//      StringBuffer zenoCode = new StringBuffer();
//      zenoCode.append("    public boolean zenoEffectAction(org.opensourcephysics.numerics.GeneralStateEvent _event, double[] _state) {\n");
//      zenoCode.append(super.generateCode(Editor.GENERATE_PLAINCODE,_info));
//      zenoCode.append("      return "+zenoStopAfterEffect.isSelected()+";\n");
//      zenoCode.append("    }\n\n");
//      return zenoCode;
//    }
//    return super.generateCode(_type, _info);
//  }

} // end of class
