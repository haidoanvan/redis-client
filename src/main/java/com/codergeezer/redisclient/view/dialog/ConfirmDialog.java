package com.codergeezer.redisclient.view.dialog;

import com.codergeezer.redisclient.utils.Constants;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.util.ui.JBUI;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author haidv
 * @version 1.0
 */
public class ConfirmDialog extends DialogWrapper {

  private final String centerPanelText;

  private final Consumer<ActionEvent> customOkFunction;

  public ConfirmDialog(
      @NotNull Project project,
      String title,
      String centerPanelText,
      Consumer<ActionEvent> customOkFunction) {
    super(project);
    this.centerPanelText = centerPanelText;
    this.customOkFunction = customOkFunction;
    this.setTitle(title);
    this.setResizable(false);
    this.setAutoAdjustable(true);
    this.init();
  }

  @Override
  protected void init() {
    super.init();
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    JLabel jLabel = new JLabel(centerPanelText);
    jLabel.setBorder(JBUI.Borders.empty(10, 0));
    return jLabel;
  }

  @Override
  protected Action @NotNull [] createActions() {
    DialogWrapperExitAction exitAction =
        new DialogWrapperExitAction(Constants.CANCEL_LABEL, CANCEL_EXIT_CODE);
    CustomOKAction okAction = new CustomOKAction();
    okAction.putValue(DialogWrapper.DEFAULT_ACTION, true);
    return new Action[] {exitAction, okAction};
  }

  protected class CustomOKAction extends DialogWrapperAction {

    protected CustomOKAction() {
      super(Constants.OK_LABEL);
    }

    @Override
    protected void doAction(ActionEvent e) {
      if (customOkFunction != null) {
        customOkFunction.accept(e);
      }
      close(OK_EXIT_CODE);
    }
  }
}
