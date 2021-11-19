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
public class FlushDBAction extends AnAction {

  private final KeyTreeDisplayPanel keyTreeDisplayPanel;

  public FlushDBAction(KeyTreeDisplayPanel keyTreeDisplayPanel) {
    super("Flush DB", "Flush DB", AllIcons.Actions.GC);
    this.keyTreeDisplayPanel = keyTreeDisplayPanel;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
    keyTreeDisplayPanel.flushDBHandle();
  }

  @Override
  public boolean isDumbAware() {
    return true;
  }

  @Override
  public void update(AnActionEvent event) {
    event.getPresentation().setEnabled(keyTreeDisplayPanel.getRedisDatabase().getKeyCount() > 0);
  }
}
