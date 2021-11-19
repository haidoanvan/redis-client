package com.codergeezer.redisclient.model;

/**
 * @author haidv
 * @version 1.0
 */
public class KeyInfo {

  private String key;

  private boolean del;

  public KeyInfo(String key, boolean del) {
    this.key = key;
    this.del = del;
  }

  public KeyInfo() {}

  public static KeyInfoBuilder builder() {
    return new KeyInfoBuilder();
  }

  @Override
  public String toString() {
    return del ? "(Removed) " + key : key;
  }

  public String getKey() {
    return this.key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public boolean isDel() {
    return this.del;
  }

  public void setDel(boolean del) {
    this.del = del;
  }

  public static class KeyInfoBuilder {

    private String key;

    private boolean del;

    KeyInfoBuilder() {}

    public KeyInfoBuilder key(String key) {
      this.key = key;
      return this;
    }

    public KeyInfoBuilder del(boolean del) {
      this.del = del;
      return this;
    }

    public KeyInfo build() {
      return new KeyInfo(key, del);
    }

    public String toString() {
      return "KeyInfo.KeyInfoBuilder(key=" + this.key + ", del=" + this.del + ")";
    }
  }
}
