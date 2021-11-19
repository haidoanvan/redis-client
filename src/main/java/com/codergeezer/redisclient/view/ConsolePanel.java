package com.codergeezer.redisclient.view;

import com.codergeezer.redisclient.logic.RedisManager;
import com.codergeezer.redisclient.model.ServerConfiguration;
import com.codergeezer.redisclient.view.textfield.ConsoleCommandTextArea;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.util.ui.JBUI;
import java.awt.BorderLayout;
import javax.swing.JPanel;
import org.jetbrains.annotations.NotNull;

/**
 * @author haidv
 * @version 1.0
 */
public class ConsolePanel extends JPanel {

  private final Project project;

  private final ServerConfiguration serverConfiguration;

  private final RedisManager redisManager;

  private JBSplitter container;

  private ConsoleCommandTextArea cmdTextArea;

  public ConsolePanel(
      Project project, ServerConfiguration serverConfiguration, RedisManager redisManager) {
    this.project = project;
    this.serverConfiguration = serverConfiguration;
    this.redisManager = redisManager;
    this.setLayout(new BorderLayout());
    init();
  }

  private void init() {
    // init result area
    JBTextArea resultArea = new JBTextArea();
    resultArea.setEditable(false);
    resultArea.setLineWrap(true);
    resultArea.setWrapStyleWord(true);
    resultArea.setMargin(JBUI.insetsLeft(10));
    JBScrollPane executeResultScrollPane = new JBScrollPane(resultArea);
    executeResultScrollPane.setHorizontalScrollBarPolicy(JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    // init console result toolbar
    DefaultActionGroup actions = new DefaultActionGroup();
    actions.add(createClearAction(resultArea));
    ActionToolbar actionToolbar =
        ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, actions, false);
    // init result area and result toolbar container
    JPanel executeResultPanel = new JPanel(new BorderLayout());
    executeResultPanel.add(executeResultScrollPane, BorderLayout.CENTER);
    executeResultPanel.add(actionToolbar.getComponent(), BorderLayout.EAST);

    // init console area
    cmdTextArea = new ConsoleCommandTextArea(resultArea, redisManager);
    JPanel cmdPanel = new JPanel(new BorderLayout());
    cmdPanel.add(cmdTextArea);
    JBScrollPane cmdScrollPane = new JBScrollPane(cmdPanel);

    container = new JBSplitter(true, 0.6F);
    container.setDividerPositionStrategy(Splitter.DividerPositionStrategy.KEEP_FIRST_SIZE);
    container.setFirstComponent(cmdScrollPane);
    container.setSecondComponent(executeResultPanel);

    this.add(container, BorderLayout.CENTER);
  }

  private AnAction createClearAction(JBTextArea resultArea) {
    return new AnAction("Action.CLEAR_RESULT") {
      @Override
      public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        resultArea.setText("");
      }
    };
  }

  public ConsoleCommandTextArea getCmdTextArea() {
    return this.cmdTextArea;
  }
}
