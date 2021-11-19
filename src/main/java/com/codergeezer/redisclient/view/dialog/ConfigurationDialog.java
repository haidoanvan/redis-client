package com.codergeezer.redisclient.view.dialog;

import com.codergeezer.redisclient.logic.ConnectionManager;
import com.codergeezer.redisclient.logic.Notifier;
import com.codergeezer.redisclient.logic.RedisConfiguration;
import com.codergeezer.redisclient.logic.RedisManager;
import com.codergeezer.redisclient.model.ServerConfiguration;
import com.codergeezer.redisclient.utils.Constants;
import com.codergeezer.redisclient.view.ConfigurationHelper;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LoadingDecorator;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import com.intellij.ui.NumberDocument;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.List;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author haidv
 * @version 1.0
 */
public class ConfigurationDialog extends DialogWrapper implements Disposable {

  private final List<ServerConfiguration> serverConfigurations;

  private final RedisConfiguration redisConfiguration;

  private final ServerConfiguration serverConfiguration;

  private final Tree configurationTree;

  private final ConfigurationHelper configurationHelper;
  private final Notifier notifier;
  private final ConnectionManager connectionManager;
  private JTextField nameTextField;
  private JTextField hostField;
  private JTextField portField;
  private JPasswordField passwordField;
  private JTextField commentField;
  private CustomOKAction okAction;
  private String preName;

  public ConfigurationDialog(
      Project project,
      ServerConfiguration serverConfiguration,
      Tree configurationTree,
      ConfigurationHelper configurationHelper,
      Notifier notifier,
      ConnectionManager connectionManager) {
    super(project);
    this.notifier = notifier;
    this.redisConfiguration = RedisConfiguration.getInstance(project);
    this.connectionManager = connectionManager;
    this.serverConfigurations = redisConfiguration.getServerConfigurations();
    this.serverConfiguration =
        serverConfiguration == null
            ? ServerConfiguration.byDefault(serverConfigurations)
            : serverConfiguration;
    this.configurationTree = configurationTree;
    this.configurationHelper = configurationHelper;
    this.setTitle(Constants.CONFIG_DIALOG_TITLE);
    this.setSize(600, 600);
    this.init();
  }

  @Override
  protected void init() {
    super.init();
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    nameTextField = new JTextField(serverConfiguration.getName());
    preName = serverConfiguration.getName();
    hostField = new JTextField(serverConfiguration.getUrl());
    portField = new JTextField();
    portField.setDocument(new NumberDocument());
    portField.setText(String.valueOf(serverConfiguration.getPort()));
    passwordField = new JPasswordField(serverConfiguration.getPassword());
    commentField = new JTextField(serverConfiguration.getComment());

    JTextPane testResult = new JTextPane();
    testResult.setMargin(JBUI.insetsLeft(10));
    testResult.setOpaque(false);
    testResult.setEditable(false);
    testResult.setFocusable(false);
    testResult.setAlignmentX(SwingConstants.LEFT);

    LoadingDecorator loadingDecorator = new LoadingDecorator(testResult, this, 0);

    JButton testButton = new JButton(Constants.CONFIG_DIALOG_TEST_BUTTON_LABEL);
    testButton.addActionListener(
        e -> {
          if (doValidate(true)) {
            String password;
            if (StringUtils.isNotBlank(new String(passwordField.getPassword()))) {
              password = new String(passwordField.getPassword());
            } else {
              password = null;
            }

            loadingDecorator.startLoading(false);
            ApplicationManager.getApplication()
                .invokeLater(
                    () -> {
                      RedisManager.TestConnectionResult testConnectionResult =
                          RedisManager.getTestConnectionResult(
                              hostField.getText(), Integer.parseInt(portField.getText()), password);
                      testResult.setText(testConnectionResult.getMsg());
                      if (testConnectionResult.isSuccess()) {
                        testResult.setForeground(JBColor.GREEN);
                      } else {
                        testResult.setForeground(JBColor.RED);
                      }
                    });
            loadingDecorator.stopLoading();
          }
        });

    GridBagLayout gridBagLayout = new GridBagLayout();
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.BOTH;
    JPanel connectionSettingsPanel = new JPanel();
    connectionSettingsPanel.setLayout(gridBagLayout);
    constraints.insets = JBUI.insets(10, 10, 0, 10);

    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.gridwidth = 1;
    constraints.gridheight = 1;
    constraints.weightx = 0.15;
    constraints.weighty = 0.33;
    JLabel connectionNameLabel = new JLabel(Constants.CONFIG_DIALOG_NAME_LABEL);
    gridBagLayout.setConstraints(connectionNameLabel, constraints);

    constraints.gridx = 1;
    constraints.gridy = 0;
    constraints.gridwidth = 3;
    constraints.gridheight = 1;
    constraints.weightx = 0.85;
    constraints.weighty = 0.33;
    gridBagLayout.setConstraints(nameTextField, constraints);

    connectionSettingsPanel.add(connectionNameLabel);
    connectionSettingsPanel.add(nameTextField);

    constraints.gridx = 0;
    constraints.gridy = 1;
    constraints.gridwidth = 1;
    constraints.gridheight = 1;
    constraints.weightx = 0.15;
    constraints.weighty = 0.33;
    JLabel commentLabel = new JLabel(Constants.CONFIG_DIALOG_COMMENT_LABEL);
    gridBagLayout.setConstraints(commentLabel, constraints);

    constraints.gridx = 1;
    constraints.gridy = 1;
    constraints.gridwidth = 3;
    constraints.gridheight = 1;
    constraints.weightx = 0.85;
    constraints.weighty = 0.33;
    gridBagLayout.setConstraints(commentField, constraints);

    connectionSettingsPanel.add(commentLabel);
    connectionSettingsPanel.add(commentField);

    constraints.gridx = 0;
    constraints.gridy = 2;
    constraints.gridwidth = 1;
    constraints.gridheight = 1;
    constraints.weightx = 0.15;
    constraints.weighty = 0.33;
    JLabel hostLabel = new JLabel(Constants.CONFIG_DIALOG_HOST_LABEL);
    gridBagLayout.setConstraints(hostLabel, constraints);

    constraints.gridx = 1;
    constraints.gridy = 2;
    constraints.gridwidth = 1;
    constraints.gridheight = 1;
    constraints.weightx = 0.55;
    constraints.weighty = 0.33;
    gridBagLayout.setConstraints(hostField, constraints);

    constraints.gridx = 2;
    constraints.gridy = 2;
    constraints.gridwidth = 1;
    constraints.gridheight = 1;
    constraints.weightx = 0.15;
    constraints.weighty = 0.33;
    JLabel portLabel = new JLabel(Constants.CONFIG_DIALOG_PORT_LABEL, SwingConstants.CENTER);
    gridBagLayout.setConstraints(portLabel, constraints);

    constraints.gridx = 3;
    constraints.gridy = 2;
    constraints.gridwidth = 1;
    constraints.gridheight = 1;
    constraints.weightx = 0.15;
    constraints.weighty = 0.33;
    gridBagLayout.setConstraints(portField, constraints);

    connectionSettingsPanel.add(hostLabel);
    connectionSettingsPanel.add(hostField);
    connectionSettingsPanel.add(portLabel);
    connectionSettingsPanel.add(portField);

    constraints.gridx = 0;
    constraints.gridy = 3;
    constraints.gridwidth = 1;
    constraints.gridheight = 1;
    constraints.weightx = 0.15;
    constraints.weighty = 0.33;
    JLabel passwordLabel = new JLabel(Constants.CONFIG_DIALOG_PASSWORD_LABEL);
    gridBagLayout.setConstraints(passwordLabel, constraints);

    constraints.gridx = 1;
    constraints.gridy = 3;
    constraints.gridwidth = 2;
    constraints.gridheight = 1;
    constraints.weightx = 0.7;
    constraints.weighty = 0.33;
    gridBagLayout.setConstraints(passwordField, constraints);

    JCheckBox checkBox = new JCheckBox(Constants.CONFIG_DIALOG_CHECKBOX_LABEL);
    checkBox.addItemListener(
        e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            passwordField.setEchoChar((char) 0);
          } else {
            passwordField.setEchoChar('*');
          }
        });
    checkBox.setBounds(300, 81, 135, 27);

    constraints.gridx = 3;
    constraints.gridy = 3;
    constraints.gridwidth = 1;
    constraints.gridheight = 1;
    constraints.weightx = 0.15;
    constraints.weighty = 0.33;
    gridBagLayout.setConstraints(checkBox, constraints);

    connectionSettingsPanel.add(passwordLabel);
    connectionSettingsPanel.add(passwordField);
    connectionSettingsPanel.add(checkBox);

    JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
    row.add(testButton);
    JPanel testConnectionSettingsPanel = new JPanel(new GridLayout(2, 1));
    testConnectionSettingsPanel.add(row);
    testConnectionSettingsPanel.add(loadingDecorator.getComponent());

    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.add(connectionSettingsPanel, BorderLayout.NORTH);
    centerPanel.add(testConnectionSettingsPanel, BorderLayout.SOUTH);
    return centerPanel;
  }

  @Override
  public @Nullable JComponent getPreferredFocusedComponent() {
    return nameTextField;
  }

  @Override
  protected Action @NotNull [] createActions() {
    DialogWrapperExitAction exitAction =
        new DialogWrapperExitAction(Constants.CANCEL_LABEL, CANCEL_EXIT_CODE);
    okAction = new CustomOKAction();

    okAction.putValue(DialogWrapper.DEFAULT_ACTION, true);
    return new Action[] {exitAction, okAction};
  }

  protected boolean doValidate(boolean isTest) {
    if (!isTest) {
      if (StringUtils.isBlank(nameTextField.getText())) {
        Messages.showErrorDialog(
            Constants.CONNECT_ERROR_LABEL_EMPTY_MSG, Constants.CONNECT_ERROR_TITLE);
        return false;
      } else {
        if (serverConfiguration.isNew()
            && serverConfigurations.stream()
                .anyMatch(v -> v.getName().equals(nameTextField.getText()))) {
          Messages.showErrorDialog(
              String.format(
                  Constants.CONNECT_ERROR_LABEL_ALREADY_EXISTS_MSG, nameTextField.getText()),
              Constants.CONNECT_ERROR_TITLE);
          return false;
        }
        if (!serverConfiguration.isNew()
            && !nameTextField.getText().equals(serverConfiguration.getName())
            && serverConfigurations.stream()
                .anyMatch(v -> v.getName().equals(nameTextField.getText()))) {
          Messages.showErrorDialog(
              String.format(
                  Constants.CONNECT_ERROR_LABEL_ALREADY_EXISTS_MSG, nameTextField.getText()),
              Constants.CONNECT_ERROR_TITLE);
          return false;
        }
      }
    }
    String port = portField.getText();
    if (StringUtils.isBlank(hostField.getText())
        || StringUtils.isBlank(port)
        || !StringUtils.isNumeric(port)) {
      Messages.showErrorDialog(Constants.CONFIG_ERROR_URL_MSG, Constants.CONFIG_ERROR_TITLE);
      return false;
    }
    return true;
  }

  @Override
  public void dispose() {
    super.dispose();
  }

  protected class CustomOKAction extends DialogWrapperAction {

    protected CustomOKAction() {
      super(Constants.OK_LABEL);
    }

    @Override
    protected void doAction(ActionEvent e) {
      if (doValidate(false)) {
        DefaultTreeModel configurationTreeModel = (DefaultTreeModel) configurationTree.getModel();
        if (serverConfiguration.isNew()) {
          String password = null;
          if (StringUtils.isNotBlank(new String(passwordField.getPassword()))) {
            password = new String(passwordField.getPassword());
          }
          ServerConfiguration serverConfiguration =
              ServerConfiguration.builder()
                  .name(nameTextField.getText().trim())
                  .url(hostField.getText())
                  .port(Integer.parseInt(portField.getText()))
                  .password(password)
                  .comment(commentField.getText())
                  .build();
          redisConfiguration.addServerConfiguration(serverConfiguration);
          configurationHelper.addConnectionToList(configurationTreeModel, serverConfiguration);
          close(CANCEL_EXIT_CODE);
          notifier.notifyInfo(
              String.format(Constants.ADD_NEW_SERVER_SUCCESS_NOTY, serverConfiguration.getName()));
        } else {
          String password = null;
          if (StringUtils.isNotBlank(new String(passwordField.getPassword()))) {
            password = new String(passwordField.getPassword());
          }
          ServerConfiguration newServerConfiguration =
              ServerConfiguration.builder()
                  .name(nameTextField.getText().trim())
                  .url(hostField.getText())
                  .port(Integer.parseInt(portField.getText()))
                  .password(password)
                  .comment(commentField.getText())
                  .build();
          configurationHelper.disconnectHandle(
              configurationTree, configurationHelper.getConfigurationTreeModel());
          redisConfiguration.updateServerConfiguration(serverConfiguration, newServerConfiguration);
          close(OK_EXIT_CODE);
          notifier.notifyInfo(
              String.format(Constants.UPDATE_SERVER_SUCCESS_NOTY, serverConfiguration.getName()));
        }
      }
    }
  }
}
