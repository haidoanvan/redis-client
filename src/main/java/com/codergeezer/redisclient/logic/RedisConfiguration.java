package com.codergeezer.redisclient.logic;

import static com.codergeezer.redisclient.view.KeyValueDisplayPanel.DEFAULT_GROUP_SYMBOL;

import com.codergeezer.redisclient.model.RedisDatabase;
import com.codergeezer.redisclient.model.ServerConfiguration;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author haidv
 * @version 1.0
 */
public class RedisConfiguration implements PersistentStateComponent<RedisConfiguration> {

  private static final String RELOAD_AFTER_ADDING_THE_KEY = "reloadAfterAddingTheKey";

  private static final String DB_COUNT_KEY = "dbCount:";

  private static final RedisConfiguration instance = new RedisConfiguration();

  private static PropertiesComponent properties;

  private List<ServerConfiguration> serverConfigurations = new ArrayList<>();

  private RedisConfiguration() {}

  public static RedisConfiguration getInstance(Project project) {
    properties = PropertiesComponent.getInstance(project);
    return instance;
  }

  public List<ServerConfiguration> getServerConfigurations() {
    return serverConfigurations;
  }

  public void setServerConfigurations(List<ServerConfiguration> serverConfigurations) {
    this.serverConfigurations = serverConfigurations;
  }

  public void addServerConfiguration(ServerConfiguration serverConfiguration) {
    serverConfigurations.add(serverConfiguration);
  }

  public void updateServerConfiguration(
      ServerConfiguration previousConfiguration, ServerConfiguration updatedConfiguration) {
    if (previousConfiguration.equals(updatedConfiguration)) {
      return;
    }

    int index = getServerConfigurationIndex(previousConfiguration);
    serverConfigurations.set(index, updatedConfiguration);
  }

  private int getServerConfigurationIndex(ServerConfiguration configuration) {
    int index = 0;
    for (ServerConfiguration serverConfiguration : serverConfigurations) {
      if (serverConfiguration.equals(configuration)) {
        return index;
      }
      index++;
    }

    throw new IllegalArgumentException("Unable to find the configuration to updated");
  }

  public void removeServerConfiguration(ServerConfiguration configuration) {
    serverConfigurations.remove(configuration);
  }

  public boolean getReloadAfterAddingTheKey() {
    return properties.getBoolean(RELOAD_AFTER_ADDING_THE_KEY, false);
  }

  public void setReloadAfterAddingTheKey(boolean reloadAfterAddingTheKey) {
    properties.setValue(RELOAD_AFTER_ADDING_THE_KEY, reloadAfterAddingTheKey);
  }

  public void saveGroupSymbol(RedisDatabase redisDatabase, String groupSymbol) {
    properties.setValue(getGroupSymbolKey(redisDatabase), groupSymbol);
  }

  public String getGroupSymbol(RedisDatabase redisDatabase) {
    return properties.getValue(getGroupSymbolKey(redisDatabase), DEFAULT_GROUP_SYMBOL);
  }

  public void removeGroupSymbol(RedisDatabase redisDatabase) {
    properties.unsetValue(getGroupSymbolKey(redisDatabase));
  }

  public void setDbCount(String connectionId, int dbCount) {
    properties.setValue(DB_COUNT_KEY + connectionId, dbCount + "");
  }

  public int getDbCount(String connectionId) {
    return properties.getInt(DB_COUNT_KEY + connectionId, 0);
  }

  private String getGroupSymbolKey(RedisDatabase redisDatabase) {
    return redisDatabase.getConfigName() + ":" + redisDatabase.getIndex();
  }

  @Override
  public @Nullable RedisConfiguration getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull RedisConfiguration state) {
    XmlSerializerUtil.copyBean(state, this);
  }
}
