package com.codergeezer.redisclient.view.action.explorer;

import com.codergeezer.redisclient.view.ConfigurationHelper;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

/**
 * @author haidv
 * @version 1.0
 */
public class AddConfigAction extends AnAction {

  private final ConfigurationHelper configurationHelper;

  private final Tree connectionTree;

  public AddConfigAction(ConfigurationHelper configurationHelper, Tree connectionTree) {
    super("New", "New", AllIcons.General.Add);
    this.configurationHelper = configurationHelper;
    this.connectionTree = connectionTree;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
    configurationHelper.addConfigHandle(connectionTree);
  }

  @Override
  public boolean isDumbAware() {
    return true;
  }
}
