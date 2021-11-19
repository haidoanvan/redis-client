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
public class AddKeyAction extends AnAction {

  private final KeyTreeDisplayPanel keyTreeDisplayPanel;

  public AddKeyAction(KeyTreeDisplayPanel keyTreeDisplayPanel) {
    super("New", "New", AllIcons.General.Add);
    this.keyTreeDisplayPanel = keyTreeDisplayPanel;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
    keyTreeDisplayPanel.addKeyHandle();
  }

  @Override
  public boolean isDumbAware() {
    return true;
  }
}
