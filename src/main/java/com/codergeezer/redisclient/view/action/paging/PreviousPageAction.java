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
public class PreviousPageAction extends AnAction {

  private final KeyTreeDisplayPanel keyTreeDisplayPanel;

  public PreviousPageAction(KeyTreeDisplayPanel keyTreeDisplayPanel) {
    super("Previous Page", "Previous Page", AllIcons.Actions.Play_back);
    this.keyTreeDisplayPanel = keyTreeDisplayPanel;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
    keyTreeDisplayPanel.previousPageHandle();
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
