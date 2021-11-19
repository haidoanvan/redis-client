package com.codergeezer.redisclient.view.action.paging;

import com.codergeezer.redisclient.view.KeyTreeDisplayPanel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.util.ui.JBUI.Borders;
import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

/**
 * @author haidv
 * @version 1.0
 */
public class PageSizeAction extends ComboBoxAction {

  private static final Integer[] OPTION_PAGE_SIZE = {5, 10, 100, 250, 1000, 2000};

  private final KeyTreeDisplayPanel keyTreeDisplayPanel;
  private String myChartOrientation = null;

  public PageSizeAction(KeyTreeDisplayPanel keyTreeDisplayPanel) {
    this.keyTreeDisplayPanel = keyTreeDisplayPanel;
    setPopupTitle("Page Size");
  }

  @Override
  protected @NotNull ComboBoxButton createComboBoxButton(@NotNull Presentation presentation) {
    ComboBoxButton comboBoxButton = super.createComboBoxButton(presentation);
    comboBoxButton.setBorder(Borders.empty());
    comboBoxButton.setText("Page Size: 5");
    return comboBoxButton;
  }

  @NotNull
  @Override
  protected DefaultActionGroup createPopupActionGroup(JComponent button) {
    DefaultActionGroup group = new DefaultActionGroup();
    for (Integer integer : OPTION_PAGE_SIZE) {
      group.add(
          new AnAction(integer.toString()) {

            @Override
            public void actionPerformed(AnActionEvent e) {
              keyTreeDisplayPanel.changePageSizeHandle(integer);
              myChartOrientation = "Page Size: " + integer;
            }
          });
    }
    return group;
  }

  @Override
  public void update(AnActionEvent e) {
    super.update(e);
    getTemplatePresentation().setText(myChartOrientation);
    e.getPresentation().setText(myChartOrientation);
  }

  @Override
  public boolean isDumbAware() {
    return true;
  }
}
