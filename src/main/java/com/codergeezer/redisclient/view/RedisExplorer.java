package com.codergeezer.redisclient.view;

import com.codergeezer.redisclient.logic.ConnectionManager;
import com.codergeezer.redisclient.logic.Notifier;
import com.codergeezer.redisclient.logic.RedisConfiguration;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.Tree;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author haidv
 * @version 1.0
 */
public class RedisExplorer implements Disposable {

  private final Project project;

  private final RedisConfiguration redisConfiguration;
  private final Notifier notifier;
  private JPanel windowContent;
  private JPanel explorerPanel;
  private Tree configurationTree;
  private ConfigurationHelper configurationHelper;
  private ConnectionManager connectionManager;

  public RedisExplorer(Project project, Notifier notifier) {
    this.project = project;
    this.notifier = notifier;
    this.redisConfiguration = RedisConfiguration.getInstance(project);
    configurationHelper.initConnections(configurationTree);

    new TreeSpeedSearch(
        configurationTree,
        treePath -> {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
          if (node.getUserObject() != null) {
            return node.getUserObject().toString();
          }
          return "";
        },
        true);
  }

  public JPanel getContent() {
    return windowContent;
  }

  @Override
  public void dispose() {
    configurationHelper.dispose();
  }

  private void createUIComponents() {
    explorerPanel = new JPanel();
    explorerPanel.setLayout(new BorderLayout());

    connectionManager = ConnectionManager.getInstance();
    configurationHelper = new ConfigurationHelper(project, notifier, connectionManager);
    configurationTree = configurationHelper.createConnectionTree(this, explorerPanel);

    explorerPanel.add(
        configurationHelper
            .createConnectionActionToolbar(configurationTree, explorerPanel)
            .getComponent(),
        BorderLayout.NORTH);
  }

  public Project getProject() {
    return project;
  }

  public RedisConfiguration getRedisConfiguration() {
    return redisConfiguration;
  }
}
