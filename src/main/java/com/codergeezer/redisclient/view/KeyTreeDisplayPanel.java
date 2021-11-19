package com.codergeezer.redisclient.view;

import static com.codergeezer.redisclient.utils.JTreeUtil.getTreeExpander;
import static com.intellij.openapi.ui.DialogWrapper.OK_EXIT_CODE;
import static redis.clients.jedis.ScanParams.SCAN_POINTER_START;

import com.alibaba.fastjson.JSON;
import com.codergeezer.redisclient.logic.RedisManager;
import com.codergeezer.redisclient.model.FragmentedKey;
import com.codergeezer.redisclient.model.KeyInfo;
import com.codergeezer.redisclient.model.RedisDatabase;
import com.codergeezer.redisclient.view.action.edition.AddKeyAction;
import com.codergeezer.redisclient.view.action.edition.DeleteKeyAction;
import com.codergeezer.redisclient.view.action.edition.FlushDBAction;
import com.codergeezer.redisclient.view.action.edition.RefreshKeyAction;
import com.codergeezer.redisclient.view.action.paging.FirstPageAction;
import com.codergeezer.redisclient.view.action.paging.LastPageAction;
import com.codergeezer.redisclient.view.action.paging.NextPageAction;
import com.codergeezer.redisclient.view.action.paging.PageSizeAction;
import com.codergeezer.redisclient.view.action.paging.PreviousPageAction;
import com.codergeezer.redisclient.view.dialog.ConfirmDialog;
import com.codergeezer.redisclient.view.dialog.ErrorDialog;
import com.codergeezer.redisclient.view.dialog.NewKeyDialog;
import com.codergeezer.redisclient.view.render.KeyTreeCellRenderer;
import com.intellij.ide.CommonActionsManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LoadingDecorator;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI.Borders;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import redis.clients.jedis.Jedis;

/**
 * @author haidv
 * @version 1.0
 */
public class KeyTreeDisplayPanel extends JPanel {

  private final Tree keyTree;

  private final LoadingDecorator keyDisplayLoadingDecorator;

  private final RedisDatabase redisDatabase;

  private final RedisManager redisManager;

  private final Project project;

  private final KeyValueDisplayPanel parent;
  private final JPanel keyDisplayPanel;
  private final Map<Integer, String> pageIndexScanPointerMap = new HashMap<>();
  private int pageSize = 5;
  private DefaultTreeModel treeModel;

  private DefaultMutableTreeNode flatRootNode;

  private JBLabel pageLabel;

  private int pageIndex = 1;

  private List<String> allKeys;

  private List<String> currentPageKeys = new ArrayList<>();

  private TreePath[] selectionPaths = null;

  public KeyTreeDisplayPanel(
      Project project,
      KeyValueDisplayPanel parent,
      JBSplitter splitterContainer,
      RedisDatabase redisDatabase,
      RedisManager redisManager,
      Consumer<KeyInfo> doubleClickKeyAction) {
    this.project = project;
    this.redisDatabase = redisDatabase;
    this.redisManager = redisManager;
    this.parent = parent;
    pageIndexScanPointerMap.put(pageIndex, SCAN_POINTER_START);

    allKeys =
        redisManager.scan(
            SCAN_POINTER_START, parent.getKeyFilter(), pageSize, redisDatabase.getIndex());

    // exception occurred
    if (allKeys == null) {
      throw new RuntimeException("exception occurred");
    }

    keyTree = new Tree();
    new TreeSpeedSearch(
        keyTree,
        treePath -> {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) treePath.getLastPathComponent();
          if (node.getUserObject() != null) {
            return node.getUserObject().toString();
          }
          return "";
        },
        true);

    keyTree.setCellRenderer(new KeyTreeCellRenderer());
    keyTree.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            selectionPaths = keyTree.getSelectionPaths();
            if (e.getClickCount() == 2) {
              TreePath selectionPath = keyTree.getSelectionPath();
              if (selectionPath == null) {
                return;
              }
              if (selectionPath.getPathCount() >= 2) {
                DefaultMutableTreeNode lastNode =
                    (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
                if (lastNode.isLeaf()) {
                  doubleClickKeyAction.accept((KeyInfo) lastNode.getUserObject());
                }
              }
            }
          }
        });
    JBScrollPane keyTreeScrollPane = new JBScrollPane(keyTree);
    keyDisplayLoadingDecorator = new LoadingDecorator(keyTreeScrollPane, parent, 0);

    final CommonActionsManager commonActionsManager = CommonActionsManager.getInstance();
    final ActionManager actionManager = ActionManager.getInstance();
    final DefaultActionGroup actions = new DefaultActionGroup();
    actions.add(new FirstPageAction(this));
    actions.add(new PreviousPageAction(this));
    actions.add(new PageSizeAction(this));
    actions.add(new NextPageAction(this));
    actions.add(new LastPageAction(this));
    actions.addSeparator();
    actions.add(new RefreshKeyAction(this));
    actions.add(new AddKeyAction(this));
    actions.add(new DeleteKeyAction(this));
    actions.addSeparator();
    actions.add(new FlushDBAction(this));
    actions.addSeparator();
    actions.add(commonActionsManager.createExpandAllAction(getTreeExpander(keyTree), keyTree));
    actions.add(commonActionsManager.createCollapseAllAction(getTreeExpander(keyTree), keyTree));
    ActionToolbar actionToolbar =
        actionManager.createActionToolbar(ActionPlaces.TOOLBAR, actions, true);

    JPanel keyPagingPanel = createPagingPanel();

    keyDisplayPanel = new JPanel(new BorderLayout());
    keyDisplayPanel.setMinimumSize(new Dimension(270, 100));
    actionToolbar.setTargetComponent(keyDisplayPanel);
    keyDisplayPanel.add(actionToolbar.getComponent(), BorderLayout.NORTH);
    keyDisplayPanel.add(keyDisplayLoadingDecorator.getComponent(), BorderLayout.CENTER);
    keyDisplayPanel.add(keyPagingPanel, BorderLayout.SOUTH);

    splitterContainer.setFirstComponent(keyDisplayPanel);
  }

  private static void updateTree(
      DefaultMutableTreeNode parentTargetNode,
      DefaultMutableTreeNode originalChildNode,
      String[] explodedKey,
      KeyInfo key) {
    if (explodedKey.length == 0) {
      addChildren(parentTargetNode, originalChildNode);
      return;
    }
    String keyFragment = explodedKey[0];
    DefaultMutableTreeNode node = findNodeByKey(parentTargetNode, keyFragment);
    if (node == null) {
      if (explodedKey.length == 1) {
        node = new DefaultMutableTreeNode(key);
      } else {
        node =
            new DefaultMutableTreeNode(FragmentedKey.builder().fragmentedKey(keyFragment).build());
      }
    }
    updateTree(
        node, originalChildNode, Arrays.copyOfRange(explodedKey, 1, explodedKey.length), key);

    parentTargetNode.add(node);
  }

  private static DefaultMutableTreeNode findNodeByKey(
      DefaultMutableTreeNode parentTargetNode, String keyFragment) {
    for (int i = 0; i < parentTargetNode.getChildCount(); i++) {
      DefaultMutableTreeNode currentChild = (DefaultMutableTreeNode) parentTargetNode.getChildAt(i);
      Object keyObj = currentChild.getUserObject();
      String nodeKey;
      // 中间节点
      if (keyObj instanceof FragmentedKey) {
        nodeKey = ((FragmentedKey) keyObj).getFragmentedKey();
        if (keyFragment.equals(nodeKey)) {
          return currentChild;
        }
      }
    }
    return null;
  }

  private static void addChildren(
      DefaultMutableTreeNode parentNode, DefaultMutableTreeNode originalChildNode) {
    Enumeration<TreeNode> children = originalChildNode.children();
    while (children.hasMoreElements()) {
      DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) children.nextElement();
      parentNode.add((MutableTreeNode) childNode.clone());
    }
  }

  @NotNull
  private JPanel createPagingPanel() {

    JPanel root = new JPanel();
    pageLabel = new JBLabel(buildTextPageLabel());
    pageLabel.setBorder(Borders.empty());
    pageLabel.setHorizontalAlignment(SwingConstants.LEFT);
    root.add(pageLabel);
    return root;
  }

  public void renderKeyTree(String keyFilter, String groupSymbol) {
    keyDisplayLoadingDecorator.startLoading(false);

    ApplicationManager.getApplication()
        .invokeLater(
            () -> {
              Long dbSize = redisManager.dbSize(redisDatabase.getIndex());
              redisDatabase.setKeyCount(dbSize);
              flatRootNode = new DefaultMutableTreeNode();
              if (dbSize != 0) {
                allKeys =
                    redisManager.scan(
                        SCAN_POINTER_START, keyFilter, pageSize, redisDatabase.getIndex());

                if (allKeys == null) {
                  throw new RuntimeException("exception occurred");
                }

                if (CollectionUtils.isNotEmpty(allKeys)) {
                  allKeys = allKeys.stream().sorted().collect(Collectors.toList());

                  int size = allKeys.size();
                  int start = (pageIndex - 1) * pageSize;
                  int end = Math.min(start + pageSize, size);
                  currentPageKeys = allKeys.subList(start, end);
                  if (!CollectionUtils.isEmpty(currentPageKeys)) {
                    for (String key : currentPageKeys) {
                      DefaultMutableTreeNode keyNode =
                          new DefaultMutableTreeNode(KeyInfo.builder().key(key).del(false).build());
                      flatRootNode.add(keyNode);
                    }
                  }
                }
              } else {
                allKeys = new ArrayList<>();
              }

              updateKeyTree(groupSymbol);
              pageLabel.setText(buildTextPageLabel());
              selectionPaths = null;
            });

    keyDisplayLoadingDecorator.stopLoading();
    keyDisplayPanel.updateUI();
  }

  public void updateKeyTree(String groupSymbol) {
    if (flatRootNode == null) {
      return;
    }

    if (StringUtils.isEmpty(groupSymbol)) {
      treeModel = new DefaultTreeModel(flatRootNode);

    } else {
      DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) flatRootNode.clone();
      for (int i = 0; i < flatRootNode.getChildCount(); i++) {
        DefaultMutableTreeNode originalChildNode =
            (DefaultMutableTreeNode) flatRootNode.getChildAt(i);
        DefaultMutableTreeNode clonedChildNode = (DefaultMutableTreeNode) originalChildNode.clone();
        KeyInfo key = (KeyInfo) clonedChildNode.getUserObject();
        String[] explodedKey = StringUtils.split(key.getKey(), groupSymbol);
        if (explodedKey.length == 1) {
          addChildren(clonedChildNode, originalChildNode);
          rootNode.add(clonedChildNode);
        } else {
          updateTree(rootNode, originalChildNode, explodedKey, key);
        }
      }
      treeModel = new DefaultTreeModel(rootNode);
    }

    keyTree.setModel(treeModel);
    treeModel.reload();
  }

  public void flushDBHandle() {
    ConfirmDialog confirmDialog =
        new ConfirmDialog(
            project,
            "Confirm",
            "Are you sure you want to delete all the keys of the currently selected DB?",
            actionEvent -> {
              try (Jedis jedis = redisManager.getJedis(redisDatabase.getIndex())) {
                jedis.flushDB();
              }
              resetPageIndex();
              renderKeyTree(parent.getKeyFilter(), parent.getGroupSymbol());
            });
    confirmDialog.show();
  }

  public void addKeyHandle() {
    NewKeyDialog newKeyDialog = new NewKeyDialog(project);
    newKeyDialog.setCustomOkAction(
        actionEvent -> {
          try {
            String key = newKeyDialog.getKeyTextField().getText();
            if (StringUtils.isEmpty(key)) {
              ErrorDialog.show("Key can not be empty");
              return;
            }

            String valueString;
            switch (newKeyDialog.getSelectedType()) {
              case String:
                valueString = newKeyDialog.getStringValueTextArea().getText();
                if (StringUtils.isEmpty(valueString)) {
                  ErrorDialog.show("Value can not be empty");
                } else {
                  redisManager.set(key, valueString, 0, redisDatabase.getIndex());
                  newKeyDialog.close(OK_EXIT_CODE);
                  if (newKeyDialog.isReloadSelected()) {
                    renderKeyTree(parent.getKeyFilter(), parent.getGroupSymbol());
                  }
                }
                break;

              case List:
                valueString = newKeyDialog.getListValueTextArea().getText();
                if (StringUtils.isEmpty(valueString)) {
                  ErrorDialog.show("Value can not be empty");
                } else {
                  try {
                    List<String> strings = JSON.parseArray(valueString, String.class);
                    redisManager.lpush(
                        key, strings.toArray(new String[] {}), redisDatabase.getIndex());
                  } catch (Exception exception) {
                    redisManager.lpush(key, new String[] {valueString}, redisDatabase.getIndex());
                  }
                  newKeyDialog.close(OK_EXIT_CODE);
                  if (newKeyDialog.isReloadSelected()) {
                    renderKeyTree(parent.getKeyFilter(), parent.getGroupSymbol());
                  }
                }
                break;

              case Set:
                valueString = newKeyDialog.getSetValueTextArea().getText();
                if (StringUtils.isEmpty(valueString)) {
                  ErrorDialog.show("Value can not be empty");
                } else {
                  try {
                    List<String> strings = JSON.parseArray(valueString, String.class);
                    redisManager.sadd(
                        key, redisDatabase.getIndex(), strings.toArray(new String[] {}));
                  } catch (Exception exception) {
                    redisManager.sadd(key, redisDatabase.getIndex(), valueString);
                  }
                  newKeyDialog.close(OK_EXIT_CODE);
                  if (newKeyDialog.isReloadSelected()) {
                    renderKeyTree(parent.getKeyFilter(), parent.getGroupSymbol());
                  }
                }
                break;

              case Zset:
                valueString = newKeyDialog.getZsetValueTextArea().getText();
                String score = newKeyDialog.getScoreTextField().getText();
                if (StringUtils.isEmpty(valueString)) {
                  ErrorDialog.show("Value can not be empty");
                } else if (StringUtils.isEmpty(score)) {
                  ErrorDialog.show("Score can not be empty");
                } else {
                  try (Jedis jedis = redisManager.getJedis(redisDatabase.getIndex())) {
                    jedis.zadd(key, Double.parseDouble(score), valueString);
                  }
                  newKeyDialog.close(OK_EXIT_CODE);
                  if (newKeyDialog.isReloadSelected()) {
                    renderKeyTree(parent.getKeyFilter(), parent.getGroupSymbol());
                  }
                }
                break;

              default:
                valueString = newKeyDialog.getHashValueTextArea().getText();
                String field = newKeyDialog.getFieldTextField().getText();
                if (StringUtils.isEmpty(valueString)) {
                  ErrorDialog.show("Value can not be empty");
                } else if (StringUtils.isEmpty(field)) {
                  ErrorDialog.show("Field can not be empty");
                } else {
                  redisManager.hset(key, field, valueString, redisDatabase.getIndex());
                  newKeyDialog.close(OK_EXIT_CODE);
                  if (newKeyDialog.isReloadSelected()) {
                    renderKeyTree(parent.getKeyFilter(), parent.getGroupSymbol());
                  }
                }
            }
          } catch (Exception exp) {
            ErrorDialog.show(exp.getMessage() + "");
          }
        });
    newKeyDialog.show();
  }

  public void deleteSelectedKeyHandle() {
    if (selectionPaths != null
        && selectionPaths.length == 1
        && selectionPaths[0].getPathCount() == 1) {
      return;
    }

    final ValueDisplayPanel valueDisplayPanel = parent.getValueDisplayPanel();
    ConfirmDialog confirmDialog =
        new ConfirmDialog(
            project,
            "Confirm",
            "Are you sure you want to delete this key?",
            actionEvent -> {
              if (selectionPaths != null) {
                for (TreePath selectionPath : selectionPaths) {
                  if (selectionPath.getPathCount() > 1) {
                    DefaultMutableTreeNode selectNode =
                        (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
                    List<String> keys = new ArrayList<>();
                    findDeleteKeys(selectNode, keys, valueDisplayPanel);
                    if (CollectionUtils.isNotEmpty(keys)) {
                      try (Jedis jedis = redisManager.getJedis(redisDatabase.getIndex())) {
                        jedis.del(keys.toArray(new String[] {}));
                        redisDatabase.setKeyCount(redisDatabase.getKeyCount() - keys.size());
                      }
                      if (redisDatabase.getKeyCount() % pageSize == 0
                          && pageIndex == getPageCount()) {
                        pageIndex = pageIndex - 1;
                      }
                      renderKeyTree(parent.getKeyFilter(), parent.getGroupSymbol());
                    }
                  }
                }
              }
            });
    confirmDialog.show();
  }

  private void findDeleteKeys(
      DefaultMutableTreeNode treeNode, List<String> keys, ValueDisplayPanel valueDisplayPanel) {
    if (treeNode.isLeaf()) {
      KeyInfo keyInfo = (KeyInfo) treeNode.getUserObject();
      if (!keyInfo.isDel()) {
        keyInfo.setDel(true);
        treeNode.setUserObject(keyInfo);
        keys.add(keyInfo.getKey());

        if (valueDisplayPanel != null) {
          final String key = valueDisplayPanel.getKey();
          if (keyInfo.getKey().equals(key)) {
            parent.removeValueDisplayPanel();
          }
        }

        treeModel.reload(treeNode);
      }
    } else {
      for (int i = 0; i < treeNode.getChildCount(); i++) {
        DefaultMutableTreeNode child = (DefaultMutableTreeNode) treeNode.getChildAt(i);
        findDeleteKeys(child, keys, valueDisplayPanel);
      }
    }
  }

  public void refreshKeyHandle() {
    renderKeyTree(parent.getKeyFilter(), parent.getGroupSymbol());
  }

  public int getPageCount() {
    int total = allKeys.size();
    int result = total / pageSize;
    int mod = total % pageSize;
    return mod > 0 ? result + 1 : result;
  }

  public void previousPageHandle() {
    pageIndex--;
    renderKeyTree(parent.getKeyFilter(), parent.getGroupSymbol());
  }

  public void nextPageHandle() {
    pageIndex++;
    renderKeyTree(parent.getKeyFilter(), parent.getGroupSymbol());
  }

  public void firstsPageHandle() {
    pageIndex = 1;
    renderKeyTree(parent.getKeyFilter(), parent.getGroupSymbol());
  }

  public void lastPageHandle() {
    pageIndex = getPageCount();
    renderKeyTree(parent.getKeyFilter(), parent.getGroupSymbol());
  }

  public void resetPageIndex() {
    pageIndex = 1;
  }

  public void changePageSizeHandle(Integer pageSize) {
    this.pageSize = pageSize;
    resetPageIndex();
    renderKeyTree(parent.getKeyFilter(), parent.getGroupSymbol());
  }

  private String buildTextPageLabel() {
    if (allKeys.size() <= getPageSize()) {
      return allKeys.size() + " rows";
    }
    return String.format(
        "%s-%s of %s rows", (pageIndex - 1) * pageSize + 1, pageIndex * pageSize, allKeys.size());
  }

  public Tree getKeyTree() {
    return this.keyTree;
  }

  public LoadingDecorator getKeyDisplayLoadingDecorator() {
    return this.keyDisplayLoadingDecorator;
  }

  public RedisDatabase getRedisDatabase() {
    return this.redisDatabase;
  }

  public RedisManager getRedisManager() {
    return this.redisManager;
  }

  public Project getProject() {
    return this.project;
  }

  public KeyValueDisplayPanel getParent() {
    return this.parent;
  }

  public int getPageSize() {
    return this.pageSize;
  }

  public DefaultTreeModel getTreeModel() {
    return this.treeModel;
  }

  public JPanel getKeyDisplayPanel() {
    return this.keyDisplayPanel;
  }

  public DefaultMutableTreeNode getFlatRootNode() {
    return this.flatRootNode;
  }

  public int getPageIndex() {
    return this.pageIndex;
  }

  public List<String> getAllKeys() {
    return this.allKeys;
  }

  public List<String> getCurrentPageKeys() {
    return this.currentPageKeys;
  }

  public Map<Integer, String> getPageIndexScanPointerMap() {
    return this.pageIndexScanPointerMap;
  }

  public TreePath[] getSelectionPaths() {
    return selectionPaths;
  }

  public void setSelectionPaths(TreePath[] selectionPaths) {
    this.selectionPaths = selectionPaths;
  }
}
