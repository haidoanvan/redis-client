package com.codergeezer.redisclient.view.render;

import com.codergeezer.redisclient.model.KeyInfo;
import com.intellij.icons.AllIcons;
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * @author haidv
 * @version 1.0
 */
public class KeyTreeCellRenderer extends DefaultTreeCellRenderer {

  @Override
  public Component getTreeCellRendererComponent(
      JTree tree,
      Object value,
      boolean sel,
      boolean expanded,
      boolean leaf,
      int row,
      boolean hasFocus) {
    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
    Object userObject = node.getUserObject();
    if (row == 0) {
      this.setIcon(AllIcons.Debugger.Db_array);
    } else if (leaf) {
      if (userObject instanceof KeyInfo) {
        this.setIcon(AllIcons.Debugger.Value);
      }
    } else {
      this.setIcon(AllIcons.Nodes.Folder);
    }

    return this;
  }
}
