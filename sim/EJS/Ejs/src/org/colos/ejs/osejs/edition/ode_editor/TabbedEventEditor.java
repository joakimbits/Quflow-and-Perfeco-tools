/**
 * The edition package contains generic tools to edit parts
 * of a simulation
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.edition.ode_editor;

import org.colos.ejs.osejs.edition.Editor;
import org.colos.ejs.osejs.edition.TabbedEditor;

public class TabbedEventEditor extends TabbedEditor {
  private EquationEditor equationEditor;

  public TabbedEventEditor (org.colos.ejs.osejs.Osejs _ejs, EquationEditor anEquationEditor) {
    super (_ejs, Editor.EVENT_EDITOR, "EquationEditor.Events");
    equationEditor = anEquationEditor;
  }

  protected Editor createPage (String _type, String _name, String _code) {
    Editor page = new EventEditor (ejs,this,equationEditor);
    page.setName(_name);
    if (_code!=null) page.readString(_code);
    else page.clear();
    return page;
  }

  public void adjust () {
    for (int i=0,n=pageList.size(); i<n; i++) ((EventEditor) pageList.get(i)).adjust();
  }

  protected void addPage (String _typeOfPage, String _name, String _code, boolean _enabled) {
    super.addPage(_typeOfPage, _name, _code, _enabled);
    adjust();
  }

  public void setActive (boolean _active) {
    super.setActive(_active);
    for (Editor page : pageList) ((EventEditor)page).setActiveComponents();
  }

}
