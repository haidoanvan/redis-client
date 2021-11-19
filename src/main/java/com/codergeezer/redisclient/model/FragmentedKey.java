package com.codergeezer.redisclient.model;

/**
 * @author haidv
 * @version 1.0
 */
public class FragmentedKey {

  private String fragmentedKey;

  public FragmentedKey(String fragmentedKey) {
    this.fragmentedKey = fragmentedKey;
  }

  public FragmentedKey() {}

  public static FragmentedKeyBuilder builder() {
    return new FragmentedKeyBuilder();
  }

  @Override
  public String toString() {
    return fragmentedKey;
  }

  public String getFragmentedKey() {
    return this.fragmentedKey;
  }

  public void setFragmentedKey(String fragmentedKey) {
    this.fragmentedKey = fragmentedKey;
  }

  public static class FragmentedKeyBuilder {

    private String fragmentedKey;

    FragmentedKeyBuilder() {}

    public FragmentedKeyBuilder fragmentedKey(String fragmentedKey) {
      this.fragmentedKey = fragmentedKey;
      return this;
    }

    public FragmentedKey build() {
      return new FragmentedKey(fragmentedKey);
    }

    public String toString() {
      return "FragmentedKey.FragmentedKeyBuilder(fragmentedKey=" + this.fragmentedKey + ")";
    }
  }
}
