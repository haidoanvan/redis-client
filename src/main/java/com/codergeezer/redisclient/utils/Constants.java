package com.codergeezer.redisclient.utils;

/**
 * @author haidv
 * @version 1.0
 */
public class Constants {

  public static final String CONNECT_ERROR_TITLE = "Connection Error";

  public static final String CONNECT_ERROR_LABEL_EMPTY_MSG = "Label connection not empty";

  public static final String CONNECT_ERROR_LABEL_ALREADY_EXISTS_MSG =
      "Object with name %s already exists. Please choose another name.";

  public static final String CONFIG_ERROR_TITLE = "Incomplete Configuration";

  public static final String CONFIG_ERROR_URL_MSG = "No database URL provided";

  public static final String CONFIG_DIALOG_TITLE = "Connection Setting";

  public static final String CONFIG_DIALOG_CHECKBOX_LABEL = "Show Password";

  public static final String CONFIG_DIALOG_TEST_BUTTON_LABEL = "Test Connection";

  public static final String CONFIG_DIALOG_NAME_LABEL = "Name:";

  public static final String CONFIG_DIALOG_COMMENT_LABEL = "Comment:";

  public static final String CONFIG_DIALOG_HOST_LABEL = "Host:";

  public static final String CONFIG_DIALOG_PORT_LABEL = "Port:";

  public static final String CONFIG_DIALOG_PASSWORD_LABEL = "Password:";

  public static final String OK_LABEL = "OK";

  public static final String CANCEL_LABEL = "Cancel";

  public static final String CONFIRM_LABEL = "Confirmation";

  public static final String CONFIRM_DELETE_SERVER_MSG =
      "The following datasource will be removed:\n %s";

  public static final String ADD_NEW_SERVER_SUCCESS_NOTY =
      "Add new configuration server %s succeeded!!!";

  public static final String UPDATE_SERVER_SUCCESS_NOTY =
      "Update configuration server %s succeeded!!!";

  public static final String REMOVE_SERVER_SUCCESS_NOTY = "Configuration server: %s removed";

  private Constants() {
    throw new IllegalStateException("Utility class");
  }
}
