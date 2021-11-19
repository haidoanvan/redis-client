package com.codergeezer.redisclient.view.editor;

import com.codergeezer.redisclient.logic.RedisManager;
import com.codergeezer.redisclient.model.RedisDatabase;
import com.codergeezer.redisclient.model.ServerConfiguration;
import com.codergeezer.redisclient.view.ConfigurationHelper;
import com.codergeezer.redisclient.view.KeyValueDisplayPanel;
import com.google.common.base.Objects;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author haidv
 * @version 1.0
 */
public class KeyValueDisplayVirtualFile extends VirtualFile {

  private final String name;

  private final Project project;

  private final ServerConfiguration serverConfiguration;

  private final RedisDatabase redisDatabase;

  private final KeyValueDisplayPanel keyValueDisplayPanel;

  private final ConfigurationHelper configurationHelper;

  public KeyValueDisplayVirtualFile(
      String name,
      Project project,
      ServerConfiguration serverConfiguration,
      RedisDatabase redisDatabase,
      RedisManager redisManager,
      ConfigurationHelper configurationHelper) {
    this.project = project;
    this.name = name;
    this.serverConfiguration = serverConfiguration;
    this.redisDatabase = redisDatabase;
    this.keyValueDisplayPanel = new KeyValueDisplayPanel(project, redisDatabase, redisManager);
    this.configurationHelper = configurationHelper;
  }

  @Override
  public @NotNull FileType getFileType() {
    return new KeyValueDisplayFileType();
  }

  @Override
  public @NotNull String getName() {
    return name;
  }

  @Override
  public @NotNull VirtualFileSystem getFileSystem() {
    return KeyValueDisplayFileSystem.getInstance(project);
  }

  @Override
  public @NonNls @NotNull String getPath() {
    return name;
  }

  @Override
  public boolean isWritable() {
    return true;
  }

  @Override
  public boolean isDirectory() {
    return false;
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public VirtualFile getParent() {
    return null;
  }

  @Override
  public VirtualFile[] getChildren() {
    return new VirtualFile[0];
  }

  @Override
  public @NotNull OutputStream getOutputStream(Object o, long l, long l1) throws IOException {
    throw new UnsupportedOperationException("Unsupported Operation");
  }

  @Override
  public byte[] contentsToByteArray() throws IOException {
    return new byte[0];
  }

  @Override
  public long getTimeStamp() {
    return 0;
  }

  @Override
  public long getLength() {
    return 0;
  }

  @Override
  public void refresh(boolean b, boolean b1, @Nullable Runnable runnable) {}

  @Override
  public @NotNull InputStream getInputStream() throws IOException {
    return null;
  }

  @Override
  public long getModificationStamp() {
    return 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KeyValueDisplayVirtualFile that = (KeyValueDisplayVirtualFile) o;
    return Objects.equal(serverConfiguration, that.serverConfiguration)
        && Objects.equal(redisDatabase, that.redisDatabase);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(serverConfiguration, redisDatabase);
  }

  public Project getProject() {
    return project;
  }

  public ServerConfiguration getServerConfiguration() {
    return serverConfiguration;
  }

  public RedisDatabase getRedisDatabase() {
    return redisDatabase;
  }

  public KeyValueDisplayPanel getKeyValueDisplayPanel() {
    return keyValueDisplayPanel;
  }

  public ConfigurationHelper getConnectionManager() {
    return configurationHelper;
  }
}
