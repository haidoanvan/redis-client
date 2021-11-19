package com.codergeezer.redisclient.model;

/**
 * @author haidv
 * @version 1.0
 */
public enum ValueFormatEnum {
  PLAIN("Plain text"),
  JSON("JSON"),
  XML("XML"),
  HTML("HTML");

  private final String description;

  ValueFormatEnum(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  @Override
  public String toString() {
    return this.description;
  }
}
