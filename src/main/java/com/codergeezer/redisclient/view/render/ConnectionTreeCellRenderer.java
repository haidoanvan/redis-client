package com.codergeezer.redisclient.view.render;

import com.codergeezer.redisclient.utils.GuiUtils;
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;

/**
 * @author haidv
 * @version 1.0
 */
public class ConnectionTreeCellRenderer extends DefaultTreeCellRenderer {

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
    TreeNode[] path = node.getPath();
    if (path.length == 2) {
      this.setIcon(GuiUtils.Redis);
    } else {
      this.setIcon(GuiUtils.Database);
    }

    return this;
  }
}
