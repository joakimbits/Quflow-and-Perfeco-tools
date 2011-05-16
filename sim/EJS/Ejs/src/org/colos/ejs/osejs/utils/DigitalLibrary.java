/**
 * The utils package contains generic utilities
 * Copyright (c) November 2001 F. Esquembre
 * @author F. Esquembre (http://fem.um.es).
 * Last Revision: March 2008
 */

package org.colos.ejs.osejs.utils;

import javax.swing.tree.DefaultMutableTreeNode;
import org.colos.ejs.osejs.Osejs;

public interface DigitalLibrary {

  /**
   * Returns a node to be used as root
   * @return
   */
  public DefaultMutableTreeNode getRootNode(Osejs _ejs);

  /**
   * Returns an additional component for options
   * @return
   */
  public javax.swing.JComponent getAdditionalComponent();

  /**
   * Gets the catalog of EJS models and populates the parent node
   * @return null if something failed
   */
  public boolean getCatalog(javax.swing.tree.DefaultTreeModel _treeModel, DefaultMutableTreeNode _parentNode);

  /**
   * Expand a node that needs to be expanded.
   * Typically used to exopand tree nodes that were not read initially
   * @param _node
   */
  public void expandNode(javax.swing.tree.DefaultTreeModel _treeModel, DefaultMutableTreeNode _node, javax.swing.JEditorPane _htmlEditor);

}

