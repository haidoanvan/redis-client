package com.codergeezer.redisclient.view.action.paging;

import com.codergeezer.redisclient.view.KeyTreeDisplayPanel;
import com.intellij.icons.AllIcons.Actions;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author haidv
 * @version 1.0
 */
public class LastPageAction extends AnAction {

  private final KeyTreeDisplayPanel keyTreeDisplayPanel;

  public LastPageAction(KeyTreeDisplayPanel keyTreeDisplayPanel) {
    super("Last Page", "Last Page", Actions.Play_last);
    this.keyTreeDisplayPanel = keyTreeDisplayPanel;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
    keyTreeDisplayPanel.lastPageHandle();
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
            keyTreeDisplayPanel.getPageCount() > 1
                && keyTreeDisplayPanel.getPageIndex() < keyTreeDisplayPanel.getPageCount());
  }
}
