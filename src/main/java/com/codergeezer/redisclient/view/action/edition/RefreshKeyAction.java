package com.codergeezer.redisclient.view.action.edition;

import com.codergeezer.redisclient.view.KeyTreeDisplayPanel;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author haidv
 * @version 1.0
 */
public class RefreshKeyAction extends AnAction {

  private final KeyTreeDisplayPanel keyTreeDisplayPanel;

  public RefreshKeyAction(KeyTreeDisplayPanel keyTreeDisplayPanel) {
    super("Refresh", "Refresh", AllIcons.Actions.Refresh);
    this.keyTreeDisplayPanel = keyTreeDisplayPanel;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
    keyTreeDisplayPanel.refreshKeyHandle();
  }

  @Override
  public boolean isDumbAware() {
    return true;
  }
}
