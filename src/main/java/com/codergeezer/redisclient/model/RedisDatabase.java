package com.codergeezer.redisclient.model;

import com.google.common.base.Objects;

public class RedisDatabase {

  private Integer index;

  private Long keyCount;

  private String configName;

  public RedisDatabase(Integer index, Long keyCount, String configName) {
    this.index = index;
    this.keyCount = keyCount;
    this.configName = configName;
  }

  public RedisDatabase() {}

  public static RedisDatabaseBuilder builder() {
    return new RedisDatabaseBuilder();
  }

  @Override
  public String toString() {
    return String.format("Db%s (%s)", index, keyCount);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RedisDatabase redisDatabase = (RedisDatabase) o;
    return Objects.equal(index, redisDatabase.index)
        && Objects.equal(configName, redisDatabase.configName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(index, configName);
  }

  public Integer getIndex() {
    return this.index;
  }

  public void setIndex(Integer index) {
    this.index = index;
  }

  public Long getKeyCount() {
    return this.keyCount;
  }

  public void setKeyCount(Long keyCount) {
    this.keyCount = keyCount;
  }

  public String getConfigName() {
    return this.configName;
  }

  public void setConfigName(String configName) {
    this.configName = configName;
  }

  public static class RedisDatabaseBuilder {

    private Integer index;

    private Long keyCount;

    private String configName;

    RedisDatabaseBuilder() {}

    public RedisDatabaseBuilder index(Integer index) {
      this.index = index;
      return this;
    }

    public RedisDatabaseBuilder keyCount(Long keyCount) {
      this.keyCount = keyCount;
      return this;
    }

    public RedisDatabaseBuilder configName(String configName) {
      this.configName = configName;
      return this;
    }

    public RedisDatabase build() {
      return new RedisDatabase(index, keyCount, configName);
    }
  }
}
