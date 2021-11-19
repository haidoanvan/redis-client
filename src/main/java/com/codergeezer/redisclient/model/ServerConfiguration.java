package com.codergeezer.redisclient.model;

import java.util.List;
import java.util.Objects;
import redis.clients.jedis.Protocol;

/**
 * @author haidv
 * @version 1.0
 */
public class ServerConfiguration {

  private static final String DEFAULT_LABEL = "@localhost";

  private String name;

  private String url;

  private int port;

  private String password;

  private String comment;

  private boolean isNew;

  public ServerConfiguration(
      String name, String url, int port, String password, String comment, boolean isNew) {
    this.name = name;
    this.url = url;
    this.port = port;
    this.password = password;
    this.comment = comment;
    this.isNew = isNew;
  }

  public ServerConfiguration() {}

  public static ServerConfiguration byDefault(List<ServerConfiguration> serverConfigurations) {
    ServerConfiguration serverConfiguration = new ServerConfiguration();
    serverConfiguration.setName(byDefaultLabel(serverConfigurations));
    serverConfiguration.setUrl(Protocol.DEFAULT_HOST);
    serverConfiguration.setPort(Protocol.DEFAULT_PORT);
    serverConfiguration.setNew(true);
    return serverConfiguration;
  }

  public static String byDefaultLabel(List<ServerConfiguration> serverConfigurations) {
    if (serverConfigurations == null || serverConfigurations.isEmpty()) {
      return DEFAULT_LABEL;
    }
    int max = 0;
    for (ServerConfiguration v : serverConfigurations) {
      if (!v.getName().startsWith(DEFAULT_LABEL)) {
        continue;
      }
      try {
        var tmp = Integer.parseInt(v.getName().substring(11).replace("[", "").replace("]", ""));
        if (tmp >= max) {
          max = tmp + 1;
        }
      } catch (Exception e) {
        if (v.getName().length() == 10) {
          max = 1;
        }
      }
    }
    return max == 0 ? DEFAULT_LABEL : DEFAULT_LABEL + " [" + max + "]";
  }

  public static String cloneLabel(String label) {
    if (!label.endsWith("]")) {
      return label + " [1]";
    }
    try {
      int tmp =
          Integer.parseInt(label.substring(label.lastIndexOf("[") + 1, label.length() - 1)) + 1;
      return label.substring(0, label.lastIndexOf("[") + 1) + tmp + "]";
    } catch (Exception e) {
      return label + " [1]";
    }
  }

  public static ServerConfigurationBuilder builder() {
    return new ServerConfigurationBuilder();
  }

  @Override
  public String toString() {
    return this.name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ServerConfiguration)) return false;
    ServerConfiguration that = (ServerConfiguration) o;
    return getPort() == that.getPort()
        && Objects.equals(getName(), that.getName())
        && Objects.equals(getUrl(), that.getUrl())
        && Objects.equals(getComment(), that.getComment())
        && Objects.equals(getPassword(), that.getPassword());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getName(), getUrl(), getPort(), getPassword(), getComment());
  }

  @Override
  public ServerConfiguration clone() {
    try {
      return (ServerConfiguration) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new AssertionError();
    }
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return this.url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public int getPort() {
    return this.port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public String getPassword() {
    return this.password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public boolean isNew() {
    return isNew;
  }

  public void setNew(boolean aNew) {
    isNew = aNew;
  }

  public static class ServerConfigurationBuilder {

    private String name;

    private String url;

    private int port;

    private String password;

    private String comment;

    private boolean isNew;

    ServerConfigurationBuilder() {}

    public ServerConfigurationBuilder name(String name) {
      this.name = name;
      return this;
    }

    public ServerConfigurationBuilder url(String url) {
      this.url = url;
      return this;
    }

    public ServerConfigurationBuilder port(int port) {
      this.port = port;
      return this;
    }

    public ServerConfigurationBuilder password(String password) {
      this.password = password;
      return this;
    }

    public ServerConfigurationBuilder comment(String comment) {
      this.comment = comment;
      return this;
    }

    public ServerConfigurationBuilder isNew(boolean isNew) {
      this.isNew = isNew;
      return this;
    }

    public ServerConfiguration build() {
      return new ServerConfiguration(name, url, port, password, comment, isNew);
    }
  }
}
