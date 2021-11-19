package com.codergeezer.redisclient.view;

import com.codergeezer.redisclient.logic.RedisConfiguration;
import com.codergeezer.redisclient.logic.RedisManager;
import com.codergeezer.redisclient.model.KeyInfo;
import com.codergeezer.redisclient.model.RedisDatabase;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LoadingDecorator;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.SearchTextField;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI.Borders;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ScrollPaneConstants;
import org.apache.commons.lang.StringUtils;

/**
 * @author haidv
 * @version 1.0
 */
public class KeyValueDisplayPanel extends JPanel implements Disposable {

  public static final String DEFAULT_FILTER = "*";

  public static final String DEFAULT_GROUP_SYMBOL = ":";

  private final RedisDatabase redisDatabase;

  private final RedisManager redisManager;

  private final Project project;

  private final RedisConfiguration redisConfiguration;

  private JPanel formPanel;

  private JBSplitter splitterContainer;

  private JPanel keyToolBarPanel;

  private String groupSymbol;

  private String keyFilter = DEFAULT_FILTER;

  private SearchTextField searchTextField;

  private KeyTreeDisplayPanel keyTreeDisplayPanel;

  private ValueDisplayPanel valueDisplayPanel;

  public KeyValueDisplayPanel(
      Project project, RedisDatabase redisDatabase, RedisManager redisManager) {
    this.project = project;
    this.redisConfiguration = RedisConfiguration.getInstance(project);
    this.redisDatabase = redisDatabase;
    this.redisManager = redisManager;

    initPanel();
  }

  @Override
  public void dispose() {
    redisManager.dispose();
  }

  public void removeValueDisplayPanel() {
    JPanel emptyPanel = new JPanel();
    emptyPanel.setMinimumSize(new Dimension(100, 100));
    splitterContainer.setSecondComponent(emptyPanel);
    this.valueDisplayPanel = null;
  }

  private void initPanel() {
    ApplicationManager.getApplication().invokeLater(this::initKeyToolBarPanel);
    ApplicationManager.getApplication().invokeLater(this::initKeyTreePanel);
  }

  private void initKeyToolBarPanel() {
    JPanel searchTextField = createSearchBox();
    JPanel groupTextField = createGroupByPanel();

    keyToolBarPanel.add(searchTextField);
    keyToolBarPanel.add(groupTextField);
  }

  private void initKeyTreePanel() {
    try {
      this.keyTreeDisplayPanel =
          new KeyTreeDisplayPanel(
              project,
              this,
              splitterContainer,
              redisDatabase,
              redisManager,
              this::renderValueDisplayPanel);
      keyTreeDisplayPanel.renderKeyTree(this.getKeyFilter(), this.getGroupSymbol());
    } catch (RuntimeException e) {
      if ("exception occurred".equals(e.getMessage())) {
        return;
      }
      throw e;
    }
  }

  private JPanel createSearchBox() {
    searchTextField = new SearchTextField();
    searchTextField.setText(DEFAULT_FILTER);
    searchTextField.addKeyboardListener(
        new KeyListener() {
          @Override
          public void keyTyped(KeyEvent e) {}

          @Override
          public void keyPressed(KeyEvent e) {}

          @Override
          public void keyReleased(KeyEvent e) {
            keyFilter = searchTextField.getText();
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
              // 根据输入的filter, 重新渲染keyTree
              if (StringUtils.isEmpty(keyFilter)) {
                keyFilter = DEFAULT_FILTER;
                searchTextField.setText(keyFilter);
              } else {
                searchTextField.addCurrentTextToHistory();
              }
              keyTreeDisplayPanel.resetPageIndex();
              keyTreeDisplayPanel.renderKeyTree(getKeyFilter(), getGroupSymbol());
            }
          }
        });

    JPanel searchBoxPanel = new JPanel();
    searchBoxPanel.add(new JLabel("Filter:"));
    searchBoxPanel.add(searchTextField);
    return searchBoxPanel;
  }

  private JPanel createGroupByPanel() {
    JPanel groupByPanel = new JPanel();
    groupByPanel.setBorder(Borders.empty());

    JBTextField groupText = new JBTextField(getGroupSymbol());
    groupText.addKeyListener(
        new KeyListener() {
          @Override
          public void keyTyped(KeyEvent e) {}

          @Override
          public void keyPressed(KeyEvent e) {}

          @Override
          public void keyReleased(KeyEvent e) {
            groupSymbol = groupText.getText();
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
              keyTreeDisplayPanel.updateKeyTree(getGroupSymbol());
            }
            redisConfiguration.saveGroupSymbol(redisDatabase, getGroupSymbol());
          }
        });

    groupByPanel.add(new JLabel("Group by:"));
    groupByPanel.add(groupText);
    return groupByPanel;
  }

  private void renderValueDisplayPanel(KeyInfo keyInfo) {
    String key = keyInfo.getKey();

    valueDisplayPanel = ValueDisplayPanel.getInstance();
    valueDisplayPanel.setMinimumSize(new Dimension(100, 100));
    JBScrollPane valueDisplayScrollPanel = new JBScrollPane(valueDisplayPanel);
    valueDisplayScrollPanel.setHorizontalScrollBarPolicy(
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    valueDisplayScrollPanel.setVerticalScrollBarPolicy(
        ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

    LoadingDecorator loadingDecorator = new LoadingDecorator(valueDisplayScrollPanel, this, 0);
    splitterContainer.setSecondComponent(loadingDecorator.getComponent());
    valueDisplayPanel.init(
        project, this, keyTreeDisplayPanel, key, redisManager, redisDatabase, loadingDecorator);
  }

  private void createUIComponents() {
    keyToolBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

    splitterContainer = new JBSplitter(false, 0.35f);
    removeValueDisplayPanel();
    splitterContainer.setDividerWidth(2);
    splitterContainer.setDividerPositionStrategy(Splitter.DividerPositionStrategy.KEEP_FIRST_SIZE);
    splitterContainer.setShowDividerControls(true);
    splitterContainer.setSplitterProportionKey("aRedis.keyValue.splitter");

    formPanel = new JPanel(new BorderLayout());
    formPanel.add(keyToolBarPanel, BorderLayout.NORTH);
    formPanel.add(splitterContainer, BorderLayout.CENTER);

    this.setLayout(new BorderLayout());
    this.add(formPanel);
  }

  public String getGroupSymbol() {
    if (groupSymbol == null) {
      groupSymbol = redisConfiguration.getGroupSymbol(redisDatabase);
    }
    return groupSymbol;
  }

  public String getKeyFilter() {
    return keyFilter;
  }

  public ValueDisplayPanel getValueDisplayPanel() {
    return valueDisplayPanel;
  }
}
