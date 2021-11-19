package com.codergeezer.redisclient.view.dialog;

import static com.codergeezer.redisclient.view.textfield.EditorTextFieldManager.createEditorTextField;
import static com.codergeezer.redisclient.view.textfield.EditorTextFieldManager.formatValue;

import com.codergeezer.redisclient.model.RedisValueTypeEnum;
import com.codergeezer.redisclient.model.ValueFormatEnum;
import com.codergeezer.redisclient.utils.Constants;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBLabel;
import java.awt.BorderLayout;
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
public class AddRowDialog extends DialogWrapper {

  private final RedisValueTypeEnum valueTypeEnum;

  private final Project project;

  private Consumer<ActionEvent> customOkAction;

  private JTextField scoreOrFieldTextField;

  private EditorTextField valueTextArea;

  public AddRowDialog(@Nullable Project project, RedisValueTypeEnum valueTypeEnum) {
    super(project);
    this.project = project;
    this.valueTypeEnum = valueTypeEnum;

    this.init();
  }

  @Override
  protected void init() {
    super.init();
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    JPanel valuePanel = createValuePanel();
    JPanel container = new JPanel(new BorderLayout());
    container.setMinimumSize(new Dimension(500, 250));
    container.add(valuePanel, BorderLayout.CENTER);
    return container;
  }

  @Override
  public @Nullable JComponent getPreferredFocusedComponent() {
    return scoreOrFieldTextField == null ? valueTextArea : scoreOrFieldTextField;
  }

  @Override
  protected Action @NotNull [] createActions() {
    DialogWrapperExitAction exitAction =
        new DialogWrapperExitAction(Constants.CANCEL_LABEL, CANCEL_EXIT_CODE);
    CustomOKAction okAction = new CustomOKAction();
    okAction.putValue(DialogWrapper.DEFAULT_ACTION, true);
    return new Action[] {exitAction, okAction};
  }

  private JPanel createValuePanel() {
    switch (valueTypeEnum) {
      case String:
      case List:
      case Set:
        return createSimpleValuePanel();

      case Zset:
        return createZSetValuePanel();

      case Hash:
        return createHashValuePanel();

      default:
        return new JPanel();
    }
  }

  @NotNull
  private JPanel createZSetValuePanel() {
    JPanel scorePanel = new JPanel(new BorderLayout());
    JBLabel scoreLabel = new JBLabel("Score:");
    scoreLabel.setPreferredSize(new Dimension(50, 25));
    scorePanel.add(scoreLabel, BorderLayout.WEST);
    scoreOrFieldTextField = new JTextField();
    scoreOrFieldTextField.setDocument(new DoubleDocument());
    scorePanel.add(scoreOrFieldTextField, BorderLayout.CENTER);

    JPanel zsetValuePanel = createSimpleValuePanel();

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
    scoreOrFieldTextField = new JTextField();
    scorePanel.add(scoreOrFieldTextField, BorderLayout.CENTER);

    JPanel hashValuePanel = createSimpleValuePanel();

    JPanel zsetTypePanel = new JPanel(new BorderLayout());
    zsetTypePanel.add(scorePanel, BorderLayout.NORTH);
    zsetTypePanel.add(hashValuePanel, BorderLayout.CENTER);
    return zsetTypePanel;
  }

  @NotNull
  private JPanel createSimpleValuePanel() {
    valueTextArea = createEditorTextField(project, PlainTextLanguage.INSTANCE, "");
    JPanel stringTypePanel = new JPanel(new BorderLayout());
    JComboBox<ValueFormatEnum> newKeyValueFormatEnumJComboBox =
        new JComboBox<>(ValueFormatEnum.values());
    newKeyValueFormatEnumJComboBox.addItemListener(
        e -> {
          if (ItemEvent.SELECTED == e.getStateChange()) {
            ValueFormatEnum formatEnum = (ValueFormatEnum) e.getItem();
            valueTextArea = formatValue(project, stringTypePanel, formatEnum, valueTextArea);
          }
        });

    JPanel viewAsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    viewAsPanel.add(new JBLabel("View as:"));
    viewAsPanel.add(newKeyValueFormatEnumJComboBox);

    JPanel valueLabelPanel = new JPanel(new BorderLayout());
    valueLabelPanel.add(new JBLabel("Value:"), BorderLayout.WEST);
    valueLabelPanel.add(viewAsPanel, BorderLayout.EAST);

    stringTypePanel.add(valueLabelPanel, BorderLayout.NORTH);
    stringTypePanel.add(valueTextArea, BorderLayout.CENTER);
    return stringTypePanel;
  }

  public String getValue() {
    return valueTextArea.getText();
  }

  public String getScoreOrField() {
    return scoreOrFieldTextField.getText();
  }

  public void setCustomOkAction(Consumer<ActionEvent> customOkAction) {
    this.customOkAction = customOkAction;
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
