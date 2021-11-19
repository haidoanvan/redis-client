package com.codergeezer.redisclient.view;

import static com.codergeezer.redisclient.utils.JTreeUtil.expandTree;

import com.codergeezer.redisclient.logic.ConnectionManager;
import com.codergeezer.redisclient.logic.Notifier;
import com.codergeezer.redisclient.logic.RedisConfiguration;
import com.codergeezer.redisclient.logic.RedisManager;
import com.codergeezer.redisclient.model.RedisDatabase;
import com.codergeezer.redisclient.model.ServerConfiguration;
import com.codergeezer.redisclient.utils.Constants;
import com.codergeezer.redisclient.utils.GuiUtils;
import com.codergeezer.redisclient.utils.JTreeUtil;
import com.codergeezer.redisclient.view.action.explorer.AddConfigAction;
import com.codergeezer.redisclient.view.action.explorer.DeleteConfigAction;
import com.codergeezer.redisclient.view.action.explorer.DisconnectAction;
import com.codergeezer.redisclient.view.action.explorer.DuplicateConfigAction;
import com.codergeezer.redisclient.view.action.explorer.OpenConsoleAction;
import com.codergeezer.redisclient.view.action.explorer.RefreshConnectAction;
import com.codergeezer.redisclient.view.action.explorer.ShowPropertiesConfigAction;
import com.codergeezer.redisclient.view.dialog.ConfigurationDialog;
import com.codergeezer.redisclient.view.editor.ConsoleFileSystem;
import com.codergeezer.redisclient.view.editor.ConsoleVirtualFile;
import com.codergeezer.redisclient.view.editor.KeyValueDisplayFileSystem;
import com.codergeezer.redisclient.view.editor.KeyValueDisplayVirtualFile;
import com.codergeezer.redisclient.view.render.ConnectionTreeCellRenderer;
import com.google.common.collect.Sets;
import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.TreeExpander;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LoadingDecorator;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.apache.commons.collections.CollectionUtils;
import redis.clients.jedis.Jedis;

/**
 * @author haidv
 * @version 1.0
 */
public class ConfigurationHelper {

  private static final URL pluginSettingsUrl = GuiUtils.class.getResource("/general/add.png");

  private final Project project;

  private final RedisConfiguration redisConfiguration;

  private final DefaultMutableTreeNode configurationTreeRoot = new DefaultMutableTreeNode();

  private final DefaultTreeModel configurationTreeModel =
      new DefaultTreeModel(configurationTreeRoot);

  private final Notifier notifier;
  private final ConnectionManager connectionManager;
  private LoadingDecorator configurationTreeLoadingDecorator;

  public ConfigurationHelper(
      Project project, Notifier notifier, ConnectionManager connectionManager) {
    this.project = project;
    this.notifier = notifier;
    this.connectionManager = connectionManager;
    this.redisConfiguration = RedisConfiguration.getInstance(project);
  }

  private static TreeExpander getTreeExpander(JTree tree) {
    return new TreeExpander() {
      @Override
      public void expandAll() {
        expandTree(tree, true);
      }

      @Override
      public boolean canExpand() {
        return true;
      }

      @Override
      public void collapseAll() {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        for (int i = 0; i < root.getChildCount(); i++) {
          DefaultMutableTreeNode child = (DefaultMutableTreeNode) root.getChildAt(i);
          JTreeUtil.expandAll(tree, new TreePath(child.getPath()), false);
        }
      }

      @Override
      public boolean canCollapse() {
        return true;
      }
    };
  }

  public void initConnections(Tree connectionTree) {
    List<ServerConfiguration> serverConfigurations = redisConfiguration.getServerConfigurations();
    for (ServerConfiguration serverConfiguration : serverConfigurations) {
      addConnectionToList(configurationTreeModel, serverConfiguration);
    }

    connectionTree.setModel(configurationTreeModel);
    connectionTree.setRootVisible(false);
  }

  public Tree createConnectionTree(RedisExplorer parent, JPanel connectionPanel) {
    Tree connectionTree =
        new Tree() {

          private final JLabel myLabel =
              new JLabel(
                  String.format(
                      "<html><center>No Redis server available<br><br>You may use <img src=\"%s\"> to add configuration</center></html>",
                      pluginSettingsUrl));

          @Override
          protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!redisConfiguration.getServerConfigurations().isEmpty()) {
              return;
            }

            myLabel.setFont(getFont());
            myLabel.setBackground(getBackground());
            myLabel.setForeground(getForeground());
            Rectangle bounds = getBounds();
            Dimension size = myLabel.getPreferredSize();
            myLabel.setBounds(0, 0, size.width, size.height);

            int x = (bounds.width - size.width) / 2;
            Graphics g2 = g.create(bounds.x + x, bounds.y + 20, bounds.width, bounds.height);
            try {
              myLabel.paint(g2);
            } finally {
              g2.dispose();
            }
          }
        };
    connectionTree.getEmptyText().clear();
    configurationTreeLoadingDecorator =
        new LoadingDecorator(new JBScrollPane(connectionTree), parent, 0);
    connectionPanel.add(configurationTreeLoadingDecorator.getComponent(), BorderLayout.CENTER);

    connectionTree.setCellRenderer(new ConnectionTreeCellRenderer());
    connectionTree.setAlignmentX(Component.LEFT_ALIGNMENT);
    ConfigurationHelper configurationHelper = this;
    connectionTree.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);

            if (e.getClickCount() == 2) {

              TreePath selectionPath = connectionTree.getSelectionPath();
              if (selectionPath == null) {
                return;
              }

              Object[] path = selectionPath.getPath();
              if (path.length == 2) {
                DefaultMutableTreeNode connectionNode = (DefaultMutableTreeNode) path[1];
                if (connectionNode.getChildCount() == 0) {
                  configurationTreeLoadingDecorator.startLoading(false);
                  ApplicationManager.getApplication()
                      .invokeLater(
                          () -> {
                            addDbs2Connection(connectionNode);
                            configurationTreeModel.reload(connectionNode);
                            JTreeUtil.expandAll(
                                connectionTree,
                                new TreePath(new Object[] {configurationTreeRoot, connectionNode}),
                                true);
                          });
                }
              }

              if (path.length == 3) {
                DefaultMutableTreeNode connectionNode = (DefaultMutableTreeNode) path[1];
                DefaultMutableTreeNode dbNode = (DefaultMutableTreeNode) path[2];

                ServerConfiguration serverConfiguration =
                    (ServerConfiguration) connectionNode.getUserObject();
                RedisDatabase redisDatabase = (RedisDatabase) dbNode.getUserObject();
                configurationTreeLoadingDecorator.startLoading(false);

                ApplicationManager.getApplication()
                    .invokeLater(
                        () -> {
                          String configName = serverConfiguration.getName();
                          KeyValueDisplayVirtualFile keyValueDisplayVirtualFile =
                              new KeyValueDisplayVirtualFile(
                                  serverConfiguration.getName() + "-DB" + redisDatabase.getIndex(),
                                  project,
                                  serverConfiguration,
                                  redisDatabase,
                                  connectionManager.getConfig(configName),
                                  configurationHelper);
                          KeyValueDisplayFileSystem.getInstance(project)
                              .openEditor(keyValueDisplayVirtualFile);
                          addEditorToMap(configName, keyValueDisplayVirtualFile);
                        });
              }
              configurationTreeLoadingDecorator.stopLoading();
            }
          }
        });
    return connectionTree;
  }

  public ActionToolbar createConnectionActionToolbar(
      Tree connectionTree, JComponent connectionPanel) {
    CommonActionsManager actionManager = CommonActionsManager.getInstance();
    DefaultActionGroup actions = new DefaultActionGroup();
    actions.add(new AddConfigAction(this, connectionTree));
    actions.add(new DuplicateConfigAction(this, connectionTree));
    actions.add(new DeleteConfigAction(this, connectionTree));
    actions.addSeparator();
    actions.add(new RefreshConnectAction(this, connectionTree));
    actions.add(new ShowPropertiesConfigAction(this, connectionTree));
    actions.add(new DisconnectAction(this, connectionTree));
    actions.addSeparator();
    actions.add(new OpenConsoleAction(this, connectionTree));
    actions.addSeparator();
    actions.add(
        actionManager.createExpandAllAction(getTreeExpander(connectionTree), connectionTree));
    actions.add(
        actionManager.createCollapseAllAction(getTreeExpander(connectionTree), connectionTree));
    ActionToolbar actionToolbar =
        ActionManager.getInstance()
            .createActionToolbar(ActionPlaces.TOOLWINDOW_TOOLBAR_BAR, actions, true);
    actionToolbar.setTargetComponent(connectionPanel);
    actionToolbar.adjustTheSameSize(true);
    return actionToolbar;
  }

  public void addDbs2Connection(DefaultMutableTreeNode connectionNode) {
    ServerConfiguration configuration = (ServerConfiguration) connectionNode.getUserObject();
    if (configuration == null) {
      return;
    }
    RedisManager redisManager = connectionManager.getConfig(configuration.getName());
    if (redisManager == null) {
      redisManager = new RedisManager(configuration, notifier);
    }
    int dbCount = redisManager.getDbCount();
    redisConfiguration.setDbCount(configuration.getName(), dbCount);

    connectionNode.removeAllChildren();
    for (int i = 0; i < dbCount; i++) {
      Long keyCount = redisManager.dbSize(i);
      RedisDatabase redisDatabase =
          RedisDatabase.builder()
              .index(i)
              .keyCount(keyCount)
              .configName(configuration.getName())
              .build();
      connectionNode.add(new DefaultMutableTreeNode(redisDatabase, false));
    }
    connectionManager.addConfig(configuration.getName(), redisManager);
  }

  public void addConnectionToList(DefaultTreeModel treeModel, ServerConfiguration configuration) {
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
    root.add(new DefaultMutableTreeNode(configuration));

    treeModel.reload();
  }

  public void removeConnectionFromTree(List<ServerConfiguration> serverConfigurations) {
    if (CollectionUtils.isEmpty(serverConfigurations)) {
      return;
    }

    for (ServerConfiguration serverConfiguration : serverConfigurations) {
      RedisManager redisManager = connectionManager.getConfig(serverConfiguration.getName());

      connectionManager.removeConfig(serverConfiguration.getName());

      redisConfiguration.removeServerConfiguration(serverConfiguration);

      if (redisManager != null) {
        redisManager.invalidate();
      }

      closeAllEditor(serverConfiguration.getName());
    }
  }

  private List<ServerConfiguration> getSelectedConnectionAndRemove(
      Tree connectionTree, boolean remove) {
    DefaultTreeModel configurationTreeModel = (DefaultTreeModel) connectionTree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) configurationTreeModel.getRoot();

    TreePath[] selectionPaths = connectionTree.getSelectionPaths();
    if (selectionPaths == null || selectionPaths.length == 0) {
      return null;
    }

    List<ServerConfiguration> result = new ArrayList<>();
    for (TreePath selectionPath : selectionPaths) {
      Object[] path = selectionPath.getPath();
      if (path.length != 2) {
        continue;
      }

      DefaultMutableTreeNode connectionNode = (DefaultMutableTreeNode) path[1];
      result.add((ServerConfiguration) connectionNode.getUserObject());
      if (remove) {
        root.remove(connectionNode);
      }
    }

    if (remove && !result.isEmpty()) {
      configurationTreeModel.reload();
    }
    return result;
  }

  private ServerConfiguration duplicateConnections(Tree connectionTree) {
    TreePath selectionPath = connectionTree.getSelectionPath();
    if (selectionPath == null || selectionPath.getPathCount() != 2) {
      return null;
    }
    DefaultMutableTreeNode connectionNode = (DefaultMutableTreeNode) selectionPath.getPath()[1];
    ServerConfiguration serverConfiguration = (ServerConfiguration) connectionNode.getUserObject();
    return ServerConfiguration.builder()
        .name(ServerConfiguration.cloneLabel(serverConfiguration.getName()))
        .url(serverConfiguration.getUrl())
        .port(serverConfiguration.getPort())
        .password(serverConfiguration.getPassword())
        .comment(serverConfiguration.getComment())
        .isNew(true)
        .build();
  }

  public void removeEditor(
      ServerConfiguration serverConfiguration, KeyValueDisplayVirtualFile virtualFile) {
    CopyOnWriteArraySet<KeyValueDisplayVirtualFile> keyValueDisplayVirtualFiles =
        connectionManager.getConnectionDbEditor(serverConfiguration.getName());
    if (CollectionUtils.isEmpty(keyValueDisplayVirtualFiles)) {
      return;
    }
    keyValueDisplayVirtualFiles.remove(virtualFile);
  }

  private void closeAllEditor(String configName) {
    CopyOnWriteArraySet<KeyValueDisplayVirtualFile> keyValueDisplayVirtualFiles =
        connectionManager.getConnectionDbEditor(configName);
    if (CollectionUtils.isEmpty(keyValueDisplayVirtualFiles)) {
      return;
    }

    for (KeyValueDisplayVirtualFile keyValueDisplayVirtualFile : keyValueDisplayVirtualFiles) {
      FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
      fileEditorManager.closeFile(keyValueDisplayVirtualFile);
    }

    connectionManager.removeConnectionDbEditor(configName);
  }

  private void addEditorToMap(String configName, KeyValueDisplayVirtualFile virtualFile) {
    CopyOnWriteArraySet<KeyValueDisplayVirtualFile> keyValueDisplayVirtualFiles =
        connectionManager.getConnectionDbEditor(configName);
    if (keyValueDisplayVirtualFiles == null) {
      keyValueDisplayVirtualFiles = Sets.newCopyOnWriteArraySet();
      keyValueDisplayVirtualFiles.add(virtualFile);
      connectionManager.addConnectionDbEditor(configName, keyValueDisplayVirtualFiles);
    } else {
      keyValueDisplayVirtualFiles.add(virtualFile);
    }
  }

  public void addConfigHandle(Tree connectionTree) {
    ConfigurationDialog configurationDialog =
        new ConfigurationDialog(project, null, connectionTree, this, notifier, connectionManager);
    configurationDialog.show();
  }

  public void deleteConfigHandle(Tree connectionTree) {
    List<ServerConfiguration> serverConfigurations =
        getSelectedConnectionAndRemove(connectionTree, false);
    if (!CollectionUtils.isEmpty(serverConfigurations)) {
      String p =
          serverConfigurations.stream()
              .map(ServerConfiguration::getName)
              .collect(Collectors.joining(","));
      int v =
          Messages.showYesNoDialog(
              String.format(Constants.CONFIRM_DELETE_SERVER_MSG, p),
              Constants.CONFIRM_LABEL,
              Constants.OK_LABEL,
              Constants.CANCEL_LABEL,
              Messages.getWarningIcon());
      if (v == 0) {
        removeConnectionFromTree(serverConfigurations);
        getSelectedConnectionAndRemove(connectionTree, true);
        notifier.notifyInfo(String.format(Constants.REMOVE_SERVER_SUCCESS_NOTY, p));
      }
    }
  }

  public void refreshConnectHandle(
      Tree connectionTree,
      DefaultTreeModel configurationTreeModel,
      LoadingDecorator configurationTreeLoadingDecorator) {
    configurationTreeLoadingDecorator.startLoading(false);
    TreePath[] selectionPaths = connectionTree.getSelectionPaths();
    if (selectionPaths == null) {
      configurationTreeLoadingDecorator.stopLoading();
      return;
    }

    for (TreePath selectionPath : selectionPaths) {
      if (selectionPath.getPathCount() != 2) {
        continue;
      }

      DefaultMutableTreeNode connectionNode = (DefaultMutableTreeNode) selectionPath.getPath()[1];
      ApplicationManager.getApplication()
          .invokeLater(
              () -> {
                addDbs2Connection(connectionNode);
                configurationTreeModel.reload(connectionNode);
              });
    }
    configurationTreeLoadingDecorator.stopLoading();
  }

  public void showPropertiesConfigHandle(Tree connectionTree) {
    TreePath selectionPath = connectionTree.getSelectionPath();
    if (selectionPath == null || selectionPath.getPathCount() != 2) {
      return;
    }

    DefaultMutableTreeNode connectionNode = (DefaultMutableTreeNode) selectionPath.getPath()[1];
    ServerConfiguration serverConfiguration = (ServerConfiguration) connectionNode.getUserObject();
    serverConfiguration.setNew(false);
    ConfigurationDialog configurationDialog =
        new ConfigurationDialog(
            project, serverConfiguration, connectionTree, this, notifier, connectionManager);
    configurationDialog.show();
  }

  public void duplicationConfigHandle(Tree connectionTree) {
    TreePath selectionPath = connectionTree.getSelectionPath();
    if (selectionPath == null || selectionPath.getPathCount() != 2) {
      return;
    }

    ServerConfiguration newServerConfiguration = duplicateConnections(connectionTree);
    ConfigurationDialog configurationDialog =
        new ConfigurationDialog(
            project, newServerConfiguration, connectionTree, this, notifier, connectionManager);
    configurationDialog.show();
  }

  public void openConsoleHandle(Tree connectionTree) {
    TreePath selectionPath = connectionTree.getSelectionPath();
    if (selectionPath == null || selectionPath.getPathCount() != 2) {
      return;
    }

    DefaultMutableTreeNode connectionNode = (DefaultMutableTreeNode) selectionPath.getPath()[1];
    ServerConfiguration serverConfiguration = (ServerConfiguration) connectionNode.getUserObject();

    RedisManager redis = connectionManager.getConfig(serverConfiguration.getName());
    try (Jedis jedis = redis.getJedis(0)) {
      if (jedis == null) {
        return;
      }
    }

    ConsoleVirtualFile consoleVirtualFile =
        new ConsoleVirtualFile(
            serverConfiguration.getName() + "-Console", project, serverConfiguration, redis);
    ConsoleFileSystem.getInstance(project).openEditor(consoleVirtualFile);
  }

  public void disconnectHandle(Tree connectionTree, DefaultTreeModel configurationTreeModel) {
    TreePath selectionPath = connectionTree.getSelectionPath();
    if (selectionPath == null || selectionPath.getPathCount() != 2) {
      return;
    }

    DefaultMutableTreeNode connectionNode = (DefaultMutableTreeNode) selectionPath.getPath()[1];
    ServerConfiguration serverConfiguration = (ServerConfiguration) connectionNode.getUserObject();

    String configName = serverConfiguration.getName();
    RedisManager redisManager = connectionManager.getConfig(configName);
    if (redisManager != null) {
      redisManager.invalidate();
    }
    connectionManager.removeConfig(configName);

    closeAllEditor(configName);

    connectionNode.removeAllChildren();
    configurationTreeModel.reload(connectionNode);
  }

  public boolean checkConnectHandle(Tree connectionTree) {
    TreePath selectionPath = connectionTree.getSelectionPath();
    DefaultMutableTreeNode connectionNode = (DefaultMutableTreeNode) selectionPath.getPath()[1];
    ServerConfiguration serverConfiguration = (ServerConfiguration) connectionNode.getUserObject();

    String configName = serverConfiguration.getName();
    return connectionManager.getConfig(configName) != null;
  }

  public void dispose() {
    for (RedisManager redisManager : connectionManager.getAllConfig()) {
      if (redisManager != null) {
        redisManager.invalidate();
      }
    }
    connectionManager.removeAll();
  }

  public DefaultTreeModel getConfigurationTreeModel() {
    return configurationTreeModel;
  }

  public LoadingDecorator getConfigurationTreeLoadingDecorator() {
    return configurationTreeLoadingDecorator;
  }
}
