package com.codergeezer.redisclient.logic;

import com.alibaba.fastjson.JSON;
import com.codergeezer.redisclient.model.ServerConfiguration;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.intellij.openapi.Disposable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.reflect.MethodUtils;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.util.Pool;

/**
 * @author haidv
 * @version 1.0
 */
public class RedisManager implements Disposable {

  private static final JedisPoolConfig jedisPoolConfig;

  static {
    jedisPoolConfig = new JedisPoolConfig();
    jedisPoolConfig.setBlockWhenExhausted(false);
    jedisPoolConfig.setMaxIdle(10);
    jedisPoolConfig.setMinIdle(0);
    jedisPoolConfig.setMaxTotal(100);
    jedisPoolConfig.setSoftMinEvictableIdleTime(Duration.ofSeconds(60));
    jedisPoolConfig.setTestOnBorrow(true);
  }

  private final String host;

  private final Integer port;

  private final String password;

  private final Integer db;

  private final Notifier notifier;

  private JedisPool pool = null;

  public RedisManager(ServerConfiguration serverConfiguration, Notifier notifier) {
    this.host = serverConfiguration.getUrl();
    this.port = serverConfiguration.getPort();
    this.password = serverConfiguration.getPassword();
    this.notifier = notifier;
    this.db = Protocol.DEFAULT_DATABASE;
  }

  public static TestConnectionResult getTestConnectionResult(
      String host, Integer port, String password) {
    try (Pool<Jedis> pool =
            new JedisPool(jedisPoolConfig, host, port, Protocol.DEFAULT_TIMEOUT, password);
        Jedis jedis = pool.getResource()) {
      String pong = jedis.ping();
      if ("PONG".equalsIgnoreCase(pong)) {
        return new TestConnectionResult(true, "Succeeded!!!");
      }
      return new TestConnectionResult(false, pong);
    } catch (Exception e) {
      return new TestConnectionResult(false, e.getMessage());
    }
  }

  public boolean isValidate() {
    return pool != null;
  }

  public void invalidate() {
    if (isValidate()) {
      this.pool.close();
      this.pool = null;
    }
  }

  private synchronized JedisPool getJedisPool() {
    if (pool == null) {
      initPool();
    }
    return pool;
  }

  @Override
  public void dispose() {
    this.invalidate();
  }

  public List<String> execRedisCommand(int db, String command, String... args) {
    try (Jedis jedis = getJedis(db)) {
      Protocol.Command cmd = Protocol.Command.valueOf(command.toUpperCase());
      if (jedis == null) {
        return Lists.newArrayList();
      }

      Client client = jedis.getClient();
      Method method =
          MethodUtils.getMatchingMethod(
              Client.class, "sendCommand", Protocol.Command.class, String[].class);
      method.setAccessible(true);
      method.invoke(client, cmd, args);
      try {
        List<String> respList = new ArrayList<>();
        Object response = client.getOne();
        if (response == null) {
          return Collections.singletonList("null");
        }
        if (response instanceof List) {
          for (Object itemResp : ((List) response)) {
            if (itemResp == null) {
              respList.add("null");
            } else {
              if (itemResp instanceof List) {
                List<byte[]> itemList = (List<byte[]>) itemResp;
                List<String> strings =
                    itemList.stream().map(String::new).collect(Collectors.toList());
                respList.add(String.join("\n", strings));
              } else if (itemResp instanceof byte[]) {
                respList.add(new String((byte[]) itemResp));
              } else {
                respList.add(JSON.toJSONString(itemResp));
              }
            }
          }
          return respList;
        }

        if (response instanceof Long) {
          return Collections.singletonList(response + "");
        }

        if (cmd == Protocol.Command.DUMP) {
          return Collections.singletonList(getPrintableString((byte[]) response));
        }

        return Collections.singletonList(new String((byte[]) response));

      } catch (JedisException e) {
        return Collections.singletonList(e.getMessage());
      }
    } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
      return Collections.singletonList(e.getMessage());
    }
  }

  private void processArgs(Protocol.Command cmd, String[] args) {
    if (cmd == Protocol.Command.RESTORE) {
      for (int i = 0; i < args.length; i++) {
        String arg = args[i];
        if (arg.startsWith("\\x")) {
          byte[] processedArg = getRestoreBytes(arg);
          args[i] = new String(processedArg);
        }
      }
    }
  }

  private String getPrintableString(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      // printable ascii characters
      if (b > 31 && b < 127) {
        sb.append((char) b);
      } else {
        sb.append(String.format("\\x%02x", (int) b & 0xff));
      }
    }
    return sb.toString();
  }

  private byte[] getRestoreBytes(String dump) {
    List<Byte> result = Lists.newArrayList();

    while (dump.length() >= 1) {
      if (dump.startsWith("\\x")) {
        result.add((byte) (0xff & Byte.parseByte(dump.substring(2, 4), 16)));
        dump = dump.substring(4);
      } else {
        result.add((byte) dump.charAt(0));
        if (dump.length() == 1) {
          break;
        }
        dump = dump.substring(1);
      }
    }
    byte[] bytes = new byte[result.size()];
    for (int i = 0; i < result.size(); i++) {
      bytes[i] = result.get(i);
    }
    return bytes;
  }

  private void initPool() {
    try {
      pool = new JedisPool(jedisPoolConfig, host, port, Protocol.DEFAULT_TIMEOUT, password);
    } catch (Throwable e) {
      notifier.notifyError("Failed to initialize the Redis pool. \n" + e.getMessage());
    }
  }

  public Jedis getJedis(int db) {
    try {
      Jedis resource = getJedisPool().getResource();
      if (db != Protocol.DEFAULT_DATABASE) {
        resource.select(db);
      }
      return resource;
    } catch (Exception e) {
      notifier.notifyError(Throwables.getStackTraceAsString(e));
    }
    return null;
  }

  public int getDbCount() {
    try (Jedis jedis = getJedis(db)) {
      if (jedis == null) {
        return 0;
      }

      int count = 0;
      while (true) {
        try {
          jedis.select(count++);
        } catch (JedisDataException jedisDataException) {
          // reset
          jedis.select(Protocol.DEFAULT_DATABASE);
          return count - 1;
        }
      }
    } catch (NullPointerException e) {
      notifier.notifyError(Throwables.getStackTraceAsString(e));
      return 0;
    }
  }

  public Long dbSize(int db) {
    try (Jedis jedis = getJedis(db)) {
      if (jedis == null) {
        return 0L;
      }
      return jedis.dbSize();
    }
  }

  public void set(String key, String val, long expire, int db) {
    try (Jedis jedis = getJedis(db)) {
      if (jedis == null) {
        return;
      }
      if (expire != 0) {
        jedis.setex(key, expire, val);
      } else {
        jedis.set(key, val);
      }
    }
  }

  public void del(String key, int db) {
    try (Jedis jedis = getJedis(db)) {
      if (jedis == null) {
        return;
      }
      jedis.del(key);
    } catch (Exception e) {
      notifier.notifyError(Throwables.getStackTraceAsString(e));
      throw new IllegalArgumentException(e);
    }
  }

  public List<String> scan(String cursor, String pattern, int count, int db) {
    List<String> list = new ArrayList<>();
    try (Jedis jedis = getJedis(db)) {
      if (jedis == null) {
        return null;
      }
      ScanParams scanParams = new ScanParams();
      scanParams.count(count);
      scanParams.match(pattern);
      do {
        ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
        list.addAll(scanResult.getResult());
        cursor = scanResult.getCursor();
      } while (!"0".equals(cursor));
    } catch (Exception e) {
      notifier.notifyError(Throwables.getStackTraceAsString(e));
      throw new IllegalArgumentException(e);
    }
    return list;
  }

  public Long lpush(String key, String[] values, int db) {
    try (Jedis jedis = getJedis(db)) {
      if (jedis == null) {
        return -1L;
      }
      return jedis.lpush(key, values);
    } catch (Exception e) {
      notifier.notifyError(Throwables.getStackTraceAsString(e));
      throw new IllegalArgumentException(e);
    }
  }

  public Long hset(String key, String field, String value, int db) {
    try (Jedis jedis = getJedis(db)) {
      if (jedis == null) {
        return -1L;
      }
      return jedis.hset(key, field, value);
    } catch (Exception e) {
      notifier.notifyError(Throwables.getStackTraceAsString(e));
      throw new IllegalArgumentException(e);
    }
  }

  public Long sadd(String key, int db, String... value) {
    try (Jedis jedis = getJedis(db)) {
      if (jedis == null) {
        return -1L;
      }
      return jedis.sadd(key, value);
    } catch (Exception e) {
      notifier.notifyError(Throwables.getStackTraceAsString(e));
      throw new IllegalArgumentException(e);
    }
  }

  public static class TestConnectionResult {

    private boolean success;

    private String msg;

    public TestConnectionResult(boolean success, String msg) {
      this.success = success;
      this.msg = msg;
    }

    public boolean isSuccess() {
      return success;
    }

    public void setSuccess(boolean success) {
      this.success = success;
    }

    public String getMsg() {
      return msg;
    }

    public void setMsg(String msg) {
      this.msg = msg;
    }
  }
}
