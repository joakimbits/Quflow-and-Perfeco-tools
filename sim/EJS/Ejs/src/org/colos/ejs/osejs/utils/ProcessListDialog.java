/**
 * The utils package contains generic utilities
 * Copyright (c) January 2002 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 */

package org.colos.ejs.osejs.utils;

import java.util.*;
import java.awt.*;
import javax.swing.*;
import org.colos.ejs.osejs.utils.ResourceUtil;
import java.text.DecimalFormat;

public class ProcessListDialog {

  private JDialog dialog;
  private JList list;
  private JButton killButton;
  private DefaultListModel listModel;
  private org.colos.ejs.osejs.Osejs ejs;

  static private ResourceUtil res = new ResourceUtil("Resources");

  public ProcessListDialog(org.colos.ejs.osejs.Osejs _ejs) {
    ejs = _ejs;
    java.awt.event.MouseAdapter mouseListener =  new java.awt.event.MouseAdapter () {
      public void mousePressed (java.awt.event.MouseEvent evt) {
        if (evt.getSource()==list) {
          if (evt.getClickCount()>1) killProcess((NamedProcess) list.getSelectedValue());
          return;
        }
        AbstractButton button = (AbstractButton) (evt.getSource());
        String aCmd = button.getActionCommand();
        if (aCmd.equals("kill")) killProcess((NamedProcess) list.getSelectedValue());
        else if (aCmd.equals("killAll")) killAllProcesses();
        else if (aCmd.equals("cancel")) dialog.setVisible (false);
      }
    };

    killButton = new JButton (res.getString("ProcessListDialog.Kill"));
    killButton.setActionCommand ("kill");
    killButton.addMouseListener (mouseListener);
    killButton.setEnabled(false);

    JButton killAllButton = new JButton (res.getString("ProcessListDialog.KillAll"));
    killAllButton.setActionCommand ("killAll");
    killAllButton.addMouseListener (mouseListener);

    JButton cancelButton = new JButton (res.getString("EditorFor.Cancel"));
    cancelButton.setActionCommand ("cancel");
    cancelButton.addMouseListener (mouseListener);

    JPanel buttonPanel = new JPanel (new FlowLayout(FlowLayout.CENTER));
    buttonPanel.add (killButton);
    buttonPanel.add (killAllButton);
    buttonPanel.add (cancelButton);

    listModel = new DefaultListModel();
    list = new JList(listModel);
    list.addMouseListener(mouseListener);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
      public void valueChanged(javax.swing.event.ListSelectionEvent e) {
        killButton.setEnabled(list.getSelectedIndex()!=-1);
      }
    });

    JScrollPane scrollPane = new JScrollPane(list);

    JSeparator sep1 = new JSeparator (SwingConstants.HORIZONTAL);

    JPanel southPanel = new JPanel (new java.awt.BorderLayout());
    southPanel.add (sep1,java.awt.BorderLayout.NORTH);
    southPanel.add (buttonPanel,java.awt.BorderLayout.SOUTH);

    dialog = new JDialog();
    dialog.setTitle(res.getString("ProcessListDialog.Title"));
    dialog.getContentPane().setLayout (new java.awt.BorderLayout(5,0));
    dialog.getContentPane().add (scrollPane,java.awt.BorderLayout.CENTER);
    dialog.getContentPane().add (southPanel,java.awt.BorderLayout.SOUTH);

    dialog.setSize (res.getDimension("ProcessListDialog.Size"));
    dialog.validate();
    dialog.setModal(false);
  }

  public void show (Component _target) {
    dialog.setLocationRelativeTo (_target);
    dialog.setVisible (true);
  }

  public int processesRunning () { return listModel.size(); }

  private void killProcess (NamedProcess _namedProcess) {
    if (_namedProcess==null) return;
    listModel.removeElement(_namedProcess);
    _namedProcess.process.destroy();
    if (ejs!=null && _namedProcess.process==ejs.getLastProcess()) ejs.getViewEditor().showWindows(true);
  }

/*
  public void killProcess (Process _process) {
    if (_process==null) return;
    for (Enumeration el = listModel.elements(); el.hasMoreElements(); ) {
      NamedProcess namedProcess = (NamedProcess) el.nextElement();
      if (namedProcess.process==_process) {
        killProcess (namedProcess);
        return;
      }
    }
  }
*/

  public void killLastProcess () {
    try { killProcess ((NamedProcess) listModel.lastElement()); } 
    catch (Exception exc) {}
  }

  public void addProcess (Process _process, String _name) {
    listModel.addElement(new NamedProcess(_process,_name));
  }

  public void killAllProcesses () {
    for (Enumeration<?> el = listModel.elements(); el.hasMoreElements(); ) {
      NamedProcess namedProcess = (NamedProcess) el.nextElement();
      namedProcess.process.destroy();
    }
    listModel.clear();
    if (ejs!=null) ejs.getViewEditor().showWindows(true);
  }

  public void removeProcess (Process _process) {
    for (Enumeration<?> el = listModel.elements(); el.hasMoreElements(); ) {
      NamedProcess namedProcess = (NamedProcess) el.nextElement();
      if (namedProcess.process==_process) {
        listModel.removeElement(namedProcess);
        return;
      }
    }
  }

  // -------------------- Private classes

  static private class NamedProcess {
    Process process;
    String name;
    static private DecimalFormat format = new DecimalFormat("00");

    public NamedProcess (Process _process, String _name) {
      process = _process;
      int index = _name.lastIndexOf('.');
      if (index>0) _name = _name.substring(0,index);
      GregorianCalendar cal = new GregorianCalendar();
      name = _name + " (" + format.format(cal.get(Calendar.HOUR_OF_DAY))+":"
                         + format.format(cal.get(Calendar.MINUTE))+":"
                         + format.format(cal.get(Calendar.SECOND))+")";


    }

    public String toString () { return name; }

  } // End of private class

}
