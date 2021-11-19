package com.codergeezer.redisclient.logic;

import com.codergeezer.redisclient.view.editor.KeyValueDisplayVirtualFile;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author haidv
 * @version 1.0
 */
public class ConnectionManager {

  private static final ConnectionManager instance = new ConnectionManager();
  private final Map<String, RedisManager> connectionRedisMap = new HashMap<>();
  private final Map<String, CopyOnWriteArraySet<KeyValueDisplayVirtualFile>> connectionDbEditorMap =
      new HashMap<>();

  private ConnectionManager() {}

  public static ConnectionManager getInstance() {
    return instance;
  }

  public void addConfig(String name, RedisManager redisManager) {
    connectionRedisMap.put(name, redisManager);
  }

  public void removeConfig(String name) {
    connectionRedisMap.remove(name);
  }

  public RedisManager getConfig(String name) {
    return connectionRedisMap.get(name);
  }

  public void addConnectionDbEditor(
      String name, CopyOnWriteArraySet<KeyValueDisplayVirtualFile> copyOnWriteArraySet) {
    connectionDbEditorMap.put(name, copyOnWriteArraySet);
  }

  public void removeConnectionDbEditor(String name) {
    connectionDbEditorMap.remove(name);
  }

  public CopyOnWriteArraySet<KeyValueDisplayVirtualFile> getConnectionDbEditor(String name) {
    return connectionDbEditorMap.get(name);
  }

  public Collection<RedisManager> getAllConfig() {
    return connectionRedisMap.values();
  }

  public void removeAll() {
    connectionRedisMap.clear();
    connectionDbEditorMap.clear();
  }

  @Override
  public String toString() {
    return "ConnectionManager{" + "connectionRedisMap=" + connectionRedisMap + '}';
  }
}
