package com.codergeezer.redisclient.view.action.paging;

import com.codergeezer.redisclient.view.KeyTreeDisplayPanel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author haidv
 * @version 1.0
 */
public class FirstPageAction extends AnAction {

  private final KeyTreeDisplayPanel keyTreeDisplayPanel;

  public FirstPageAction(KeyTreeDisplayPanel keyTreeDisplayPanel) {
    super("First Page", "First Page", AllIcons.Actions.Play_first);
    this.keyTreeDisplayPanel = keyTreeDisplayPanel;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
    keyTreeDisplayPanel.firstsPageHandle();
  }

  @Override
  public boolean isDumbAware() {
    return true;
  }

  @Override
  public void update(AnActionEvent event) {
    event
        .getPresentation()
        .setEnabled(
            keyTreeDisplayPanel.getPageIndex() != 1
                && keyTreeDisplayPanel.getPageIndex() <= keyTreeDisplayPanel.getPageCount());
  }
}
