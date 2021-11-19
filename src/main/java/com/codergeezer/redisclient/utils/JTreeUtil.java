package com.codergeezer.redisclient.utils;

import com.intellij.ide.TreeExpander;
import java.util.Enumeration;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * @author haidv
 * @version 1.0
 */
public class JTreeUtil {

  public static void expandTree(JTree tree, boolean expand) {
    TreeNode root = (TreeNode) tree.getModel().getRoot();
    expandAll(tree, new TreePath(root), expand);
  }

  public static void expandAll(JTree tree, TreePath parent, boolean expand) {
    TreeNode node = (TreeNode) parent.getLastPathComponent();
    if (node.getChildCount() >= 0) {
      for (Enumeration<? extends TreeNode> e = node.children(); e.hasMoreElements(); ) {
        TreeNode n = e.nextElement();
        TreePath path = parent.pathByAddingChild(n);
        expandAll(tree, path, expand);
      }
    }

    if (expand) {
      tree.expandPath(parent);
    } else {
      tree.collapsePath(parent);
    }
  }

  public static TreeExpander getTreeExpander(JTree tree) {
    return new TreeExpander() {
      @Override
      public void expandAll() {
        expandTree(tree, true);
      }

      @Override
      public boolean canExpand() {
        return true;
      }

      @Override
      public void collapseAll() {
        expandTree(tree, false);
      }

      @Override
      public boolean canCollapse() {
        return true;
      }
    };
  }
}
