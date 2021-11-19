package com.codergeezer.redisclient.view.action.edition;

import com.codergeezer.redisclient.view.KeyTreeDisplayPanel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import org.jetbrains.annotations.NotNull;

/**
 * @author haidv
 * @version 1.0
 */
public class DeleteKeyAction extends AnAction {

  private final KeyTreeDisplayPanel keyTreeDisplayPanel;

  public DeleteKeyAction(KeyTreeDisplayPanel keyTreeDisplayPanel) {
    super("Delete", "Delete", AllIcons.General.Remove);
    this.keyTreeDisplayPanel = keyTreeDisplayPanel;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
    keyTreeDisplayPanel.deleteSelectedKeyHandle();
  }

  @Override
  public boolean isDumbAware() {
    return true;
  }

  @Override
  public void update(AnActionEvent event) {
    if (keyTreeDisplayPanel.getSelectionPaths() == null) {
      event.getPresentation().setEnabled(false);
    } else
      event
          .getPresentation()
          .setEnabled(
              keyTreeDisplayPanel.getSelectionPaths().length != 1
                  || ((DefaultMutableTreeNode)
                              keyTreeDisplayPanel.getSelectionPaths()[0].getLastPathComponent())
                          .getUserObject()
                      != null);
  }
}
