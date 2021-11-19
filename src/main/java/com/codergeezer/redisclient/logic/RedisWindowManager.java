package com.codergeezer.redisclient.logic;

import com.codergeezer.redisclient.utils.GuiUtils;
import com.codergeezer.redisclient.view.RedisExplorer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

/**
 * @author haidv
 * @version 1.0
 */
public class RedisWindowManager implements ToolWindowFactory {

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    RedisExplorer redisExplorer = new RedisExplorer(project, Notifier.getInstance(project));
    ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
    Content content = contentFactory.createContent(redisExplorer.getContent(), "", false);
    toolWindow.getContentManager().addContent(content);
    toolWindow.setIcon(GuiUtils.Redis);
  }
}
