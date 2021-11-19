package com.codergeezer.redisclient.view.dialog;

import com.intellij.openapi.ui.Messages;

/**
 * @author haidv
 * @version 1.0
 */
public class ErrorDialog {

  public static void show(String message) {
    Messages.showMessageDialog(message, "Error", Messages.getErrorIcon());
  }
}
