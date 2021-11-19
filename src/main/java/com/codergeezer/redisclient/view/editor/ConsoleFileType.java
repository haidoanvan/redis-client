package com.codergeezer.redisclient.view.editor;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import javax.swing.Icon;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author haidv
 * @version 1.0
 */
public class ConsoleFileType implements FileType {

  @Override
  public @NonNls @NotNull String getName() {
    return "Redis Console";
  }

  @Override
  public @NotNull String getDescription() {
    return "A-Redis console";
  }

  @Override
  public @NotNull String getDefaultExtension() {
    return "";
  }

  @Override
  public @Nullable Icon getIcon() {
    return AllIcons.Nodes.DataColumn;
  }

  @Override
  public boolean isBinary() {
    return false;
  }

  @Override
  public boolean isReadOnly() {
    return true;
  }

  @Override
  public @NonNls @Nullable String getCharset(@NotNull VirtualFile file, @NotNull byte[] content) {
    return null;
  }
}
