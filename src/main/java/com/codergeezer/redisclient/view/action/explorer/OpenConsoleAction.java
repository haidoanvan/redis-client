package com.codergeezer.redisclient.view.action.explorer;

import com.codergeezer.redisclient.utils.GuiUtils;
import com.codergeezer.redisclient.view.ConfigurationHelper;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

/**
 * @author haidv
 * @version 1.0
 */
public class OpenConsoleAction extends AnAction {

  private final ConfigurationHelper configurationHelper;

  private final Tree connectionTree;

  public OpenConsoleAction(ConfigurationHelper configurationHelper, Tree connectionTree) {
    super("Jump to query Console...", "Jump to query Console...", GuiUtils.ConsoleRun);
    this.configurationHelper = configurationHelper;
    this.connectionTree = connectionTree;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
    configurationHelper.openConsoleHandle(connectionTree);
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
            connectionTree.getSelectionPath() != null
                && connectionTree.getSelectionPath().getPathCount() == 2);
  }
}
