package com.codergeezer.redisclient.view.dialog;

import static com.codergeezer.redisclient.view.textfield.EditorTextFieldManager.createEditorTextField;
import static com.codergeezer.redisclient.view.textfield.EditorTextFieldManager.formatValue;

import com.codergeezer.redisclient.logic.RedisConfiguration;
import com.codergeezer.redisclient.model.RedisValueTypeEnum;
import com.codergeezer.redisclient.model.ValueFormatEnum;
import com.codergeezer.redisclient.utils.Constants;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.function.Consumer;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.batik.ext.swing.DoubleDocument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author haidv
 * @version 1.0
 */
public class NewKeyDialog extends DialogWrapper {

  private final RedisConfiguration redisConfiguration;

  private final Project project;

  private Consumer<ActionEvent> customOkAction;

  private CardLayout cardLayout;

  private RedisValueTypeEnum selectedType;

  private JTextField keyTextField;

  private JTextField scoreTextField;

  private JTextField fieldTextField;

  private boolean reloadSelected;

  private JPanel zsetValuePanel;

  private JPanel hashValuePanel;

  private EditorTextField stringValueTextArea;

  private EditorTextField listValueTextArea;

  private EditorTextField setValueTextArea;

  private EditorTextField zsetValueTextArea;

  private EditorTextField hashValueTextArea;

  public NewKeyDialog(@Nullable Project project) {
    super(project);
    redisConfiguration = RedisConfiguration.getInstance(project);
    reloadSelected = redisConfiguration.getReloadAfterAddingTheKey();
    this.project = project;
    this.init();
  }

  @Override
  protected void init() {
    super.init();
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {

    JPanel keyPanel = createKeyPanel();
    JPanel valuePanel = createValuePanel();
    JPanel typePanel = createTypePanel(valuePanel);

    JPanel keyAndTypePanel = new JPanel(new BorderLayout());
    keyAndTypePanel.add(keyPanel, BorderLayout.NORTH);
    keyAndTypePanel.add(typePanel, BorderLayout.SOUTH);

    JBCheckBox reloadCheckBox = new JBCheckBox("Reload after adding the key", reloadSelected);
    reloadCheckBox.addChangeListener(
        e -> {
          reloadSelected = reloadCheckBox.isSelected();
          redisConfiguration.setReloadAfterAddingTheKey(reloadSelected);
        });
    JPanel reloadPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    reloadPanel.add(reloadCheckBox);

    JPanel container = new JPanel(new BorderLayout());
    container.setMinimumSize(new Dimension(500, 250));
    container.add(keyAndTypePanel, BorderLayout.NORTH);
    container.add(valuePanel, BorderLayout.CENTER);
    container.add(reloadPanel, BorderLayout.AFTER_LAST_LINE);

    return container;
  }

  @Override
  public @Nullable JComponent getPreferredFocusedComponent() {
    return keyTextField;
  }

  @NotNull
  private JPanel createValuePanel() {
    JPanel stringValuePanel = createSimpleValuePanel(RedisValueTypeEnum.String);
    JPanel listValuePanel = createSimpleValuePanel(RedisValueTypeEnum.List);
    JPanel setValuePanel = createSimpleValuePanel(RedisValueTypeEnum.Set);
    zsetValuePanel = createSimpleValuePanel(RedisValueTypeEnum.Zset);
    hashValuePanel = createSimpleValuePanel(RedisValueTypeEnum.Hash);

    JPanel zsetTypePanel = createZSetValuePanel();
    JPanel hashTypePanel = createHashValuePanel();

    cardLayout = new CardLayout();
    JPanel valuePanel = new JPanel(cardLayout);
    valuePanel.add(RedisValueTypeEnum.String.name(), stringValuePanel);
    valuePanel.add(RedisValueTypeEnum.List.name(), listValuePanel);
    valuePanel.add(RedisValueTypeEnum.Set.name(), setValuePanel);
    valuePanel.add(RedisValueTypeEnum.Zset.name(), zsetTypePanel);
    valuePanel.add(RedisValueTypeEnum.Hash.name(), hashTypePanel);
    return valuePanel;
  }

  @NotNull
  private JPanel createTypePanel(JPanel valuePanel) {
    JBLabel typeLabel = new JBLabel("Type:");
    typeLabel.setPreferredSize(new Dimension(50, 25));

    JComboBox<RedisValueTypeEnum> redisValueTypeEnumJComboBox =
        new JComboBox<>(RedisValueTypeEnum.values());

    redisValueTypeEnumJComboBox.addItemListener(
        e -> {
          if (ItemEvent.SELECTED == e.getStateChange()) {
            selectedType = (RedisValueTypeEnum) e.getItem();
            cardLayout.show(valuePanel, selectedType.name());
          }
        });
    redisValueTypeEnumJComboBox.setSelectedIndex(0);
    selectedType = RedisValueTypeEnum.String;

    JPanel typePanel = new JPanel(new BorderLayout());
    typePanel.add(typeLabel, BorderLayout.WEST);
    typePanel.add(redisValueTypeEnumJComboBox, BorderLayout.CENTER);
    return typePanel;
  }

  @NotNull
  private JPanel createKeyPanel() {
    JPanel keyPanel = new JPanel(new BorderLayout());
    keyPanel.setMinimumSize(new Dimension(300, 10));
    JBLabel keyLabel = new JBLabel("Key:");
    keyLabel.setPreferredSize(new Dimension(50, 25));
    keyPanel.add(keyLabel, BorderLayout.WEST);
    keyTextField = new JTextField();
    keyPanel.add(keyTextField, BorderLayout.CENTER);
    return keyPanel;
  }

  @NotNull
  private JPanel createZSetValuePanel() {
    JPanel scorePanel = new JPanel(new BorderLayout());
    JBLabel scoreLabel = new JBLabel("Score:");
    scoreLabel.setPreferredSize(new Dimension(50, 25));
    scorePanel.add(scoreLabel, BorderLayout.WEST);
    scoreTextField = new JTextField();
    scoreTextField.setDocument(new DoubleDocument());
    scorePanel.add(scoreTextField, BorderLayout.CENTER);

    JPanel zsetTypePanel = new JPanel(new BorderLayout());
    zsetTypePanel.add(scorePanel, BorderLayout.NORTH);
    zsetTypePanel.add(zsetValuePanel, BorderLayout.CENTER);
    return zsetTypePanel;
  }

  @NotNull
  private JPanel createHashValuePanel() {
    JPanel scorePanel = new JPanel(new BorderLayout());
    JBLabel scoreLabel = new JBLabel("Field:");
    scoreLabel.setPreferredSize(new Dimension(50, 25));
    scorePanel.add(scoreLabel, BorderLayout.WEST);
    fieldTextField = new JTextField();
    scorePanel.add(fieldTextField, BorderLayout.CENTER);

    JPanel zsetTypePanel = new JPanel(new BorderLayout());
    zsetTypePanel.add(scorePanel, BorderLayout.NORTH);
    zsetTypePanel.add(hashValuePanel, BorderLayout.CENTER);
    return zsetTypePanel;
  }

  @NotNull
  private JPanel createSimpleValuePanel(RedisValueTypeEnum typeEnum) {
    JPanel stringTypePanel = new JPanel(new BorderLayout());
    ComboBox<ValueFormatEnum> newKeyValueFormatEnumJComboBox =
        new ComboBox<>(ValueFormatEnum.values());
    switch (typeEnum) {
      case String:
        stringValueTextArea = createEditorTextField(project, PlainTextLanguage.INSTANCE, "");
        newKeyValueFormatEnumJComboBox.addItemListener(
            e -> {
              if (ItemEvent.SELECTED == e.getStateChange()) {
                ValueFormatEnum formatEnum = (ValueFormatEnum) e.getItem();
                stringValueTextArea =
                    formatValue(project, stringTypePanel, formatEnum, stringValueTextArea);
              }
            });
        stringTypePanel.add(stringValueTextArea, BorderLayout.CENTER);
        break;
      case List:
        listValueTextArea = createEditorTextField(project, PlainTextLanguage.INSTANCE, "");
        newKeyValueFormatEnumJComboBox.addItemListener(
            e -> {
              if (ItemEvent.SELECTED == e.getStateChange()) {
                ValueFormatEnum formatEnum = (ValueFormatEnum) e.getItem();
                listValueTextArea =
                    formatValue(project, stringTypePanel, formatEnum, listValueTextArea);
              }
            });
        stringTypePanel.add(listValueTextArea, BorderLayout.CENTER);
        break;
      case Set:
        setValueTextArea = createEditorTextField(project, PlainTextLanguage.INSTANCE, "");
        newKeyValueFormatEnumJComboBox.addItemListener(
            e -> {
              if (ItemEvent.SELECTED == e.getStateChange()) {
                ValueFormatEnum formatEnum = (ValueFormatEnum) e.getItem();
                setValueTextArea =
                    formatValue(project, stringTypePanel, formatEnum, setValueTextArea);
              }
            });
        stringTypePanel.add(setValueTextArea, BorderLayout.CENTER);
        break;
      case Zset:
        zsetValueTextArea = createEditorTextField(project, PlainTextLanguage.INSTANCE, "");
        newKeyValueFormatEnumJComboBox.addItemListener(
            e -> {
              if (ItemEvent.SELECTED == e.getStateChange()) {
                ValueFormatEnum formatEnum = (ValueFormatEnum) e.getItem();
                zsetValueTextArea =
                    formatValue(project, stringTypePanel, formatEnum, zsetValueTextArea);
              }
            });
        stringTypePanel.add(zsetValueTextArea, BorderLayout.CENTER);
        break;
      default:
        hashValueTextArea = createEditorTextField(project, PlainTextLanguage.INSTANCE, "");
        newKeyValueFormatEnumJComboBox.addItemListener(
            e -> {
              if (ItemEvent.SELECTED == e.getStateChange()) {
                ValueFormatEnum formatEnum = (ValueFormatEnum) e.getItem();
                hashValueTextArea =
                    formatValue(project, stringTypePanel, formatEnum, hashValueTextArea);
              }
            });
        stringTypePanel.add(hashValueTextArea, BorderLayout.CENTER);
        break;
    }

    JPanel viewAsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    viewAsPanel.add(new JBLabel("View as:"));
    viewAsPanel.add(newKeyValueFormatEnumJComboBox);

    JPanel valueLabelPanel = new JPanel(new BorderLayout());
    valueLabelPanel.add(new JBLabel("Value:"), BorderLayout.WEST);
    valueLabelPanel.add(viewAsPanel, BorderLayout.EAST);

    stringTypePanel.add(valueLabelPanel, BorderLayout.NORTH);

    return stringTypePanel;
  }

  @Override
  protected Action @NotNull [] createActions() {
    DialogWrapperExitAction exitAction =
        new DialogWrapperExitAction(Constants.CANCEL_LABEL, CANCEL_EXIT_CODE);
    CustomOKAction okAction = new CustomOKAction();
    okAction.putValue(DialogWrapper.DEFAULT_ACTION, true);
    return new Action[] {exitAction, okAction};
  }

  public Consumer<ActionEvent> getCustomOkAction() {
    return this.customOkAction;
  }

  public void setCustomOkAction(Consumer<ActionEvent> customOkAction) {
    this.customOkAction = customOkAction;
  }

  public CardLayout getCardLayout() {
    return this.cardLayout;
  }

  public RedisValueTypeEnum getSelectedType() {
    return this.selectedType;
  }

  public JTextField getKeyTextField() {
    return this.keyTextField;
  }

  public JTextField getScoreTextField() {
    return this.scoreTextField;
  }

  public JTextField getFieldTextField() {
    return this.fieldTextField;
  }

  public boolean isReloadSelected() {
    return this.reloadSelected;
  }

  public RedisConfiguration getRedisConfiguration() {
    return this.redisConfiguration;
  }

  public Project getProject() {
    return this.project;
  }

  public JPanel getZsetValuePanel() {
    return this.zsetValuePanel;
  }

  public JPanel getHashValuePanel() {
    return this.hashValuePanel;
  }

  public EditorTextField getStringValueTextArea() {
    return this.stringValueTextArea;
  }

  public EditorTextField getListValueTextArea() {
    return this.listValueTextArea;
  }

  public EditorTextField getSetValueTextArea() {
    return this.setValueTextArea;
  }

  public EditorTextField getZsetValueTextArea() {
    return this.zsetValueTextArea;
  }

  public EditorTextField getHashValueTextArea() {
    return this.hashValueTextArea;
  }

  protected class CustomOKAction extends DialogWrapperAction {

    protected CustomOKAction() {
      super(Constants.OK_LABEL);
    }

    @Override
    protected void doAction(ActionEvent e) {
      if (customOkAction != null) {
        customOkAction.accept(e);
      }
    }
  }
}
