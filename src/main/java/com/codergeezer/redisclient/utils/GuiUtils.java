package com.codergeezer.redisclient.utils;

import com.intellij.openapi.util.IconLoader;
import javax.swing.Icon;
import javax.swing.UIManager;
import org.jetbrains.annotations.NotNull;

/**
 * @author haidv
 * @version 1.0
 */
public class GuiUtils {

  private static final String ICON_FOLDER = "/icons/";

  private static final String ICON_DARK = "_dark";

  private static final String SVG = ".svg";

  public static final @NotNull Icon ConsoleRun = loadIcon("consoleRun");

  public static final @NotNull Icon Stop = loadIcon("stop", false);

  public static final @NotNull Icon Properties = loadIcon("toolWindowSQLGenerator");

  public static final @NotNull Icon Redis = loadIcon("redis", false);

  public static final @NotNull Icon Database = loadIcon("toolWindowDatabase");

  public static Icon loadIcon(String iconFilename) {
    return loadIcon(iconFilename, true);
  }

  public static Icon loadIcon(String iconFilename, boolean darkIcon) {
    String iconPath = ICON_FOLDER + iconFilename;
    if (darkIcon && isUnderDarcula()) {
      iconPath += ICON_DARK;
    }
    return IconLoader.findIcon(iconPath + SVG);
  }

  private static boolean isUnderDarcula() {
    return UIManager.getLookAndFeel().getName().contains("Darcula");
  }
}
