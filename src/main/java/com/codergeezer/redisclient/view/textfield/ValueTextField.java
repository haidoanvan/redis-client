package com.codergeezer.redisclient.view.textfield;

import com.intellij.lang.Language;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.ui.LanguageTextField;
import org.jetbrains.annotations.NotNull;

/**
 * @author haidv
 * @version 1.0
 */
public class ValueTextField extends LanguageTextField {

  public ValueTextField(Language language, Project project, String text, boolean b) {
    super(language, project, text, b);
  }

  @Override
  protected @NotNull EditorEx createEditor() {
    EditorEx editor = super.createEditor();
    editor.setVerticalScrollbarVisible(true);
    return editor;
  }
}
