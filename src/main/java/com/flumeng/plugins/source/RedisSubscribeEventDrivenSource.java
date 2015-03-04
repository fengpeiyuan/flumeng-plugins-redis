package com.flumeng.plugins.source;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDrivenSource;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.source.AbstractSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.nio.charset.Charset;

public class RedisSubscribeEventDrivenSource extends AbstractSource implements Configurable, EventDrivenSource {

  private Logger logger = LoggerFactory.getLogger(RedisSubscribeEventDrivenSource.class);
  private ChannelProcessor channelProcessor;
  private Jedis jedis;
  private String host;
  private int port;
  private int timeout;
  private String password;
  private int database;
  private String charset;
  private String[] channels;
  private boolean running;

  @Override
  public void configure(Context context) {
    host = context.getString("host", "localhost");
    port = context.getInteger("port", 6379);
    timeout = context.getInteger("timeout", 2000);
    password = context.getString("password", "");
    database = context.getInteger("database", 0);
    charset = context.getString("charset", "utf-8");
    String channel = context.getString("channel");
    if (channel == null) { throw new RuntimeException("Redis Channel must be set."); }
    channels = channel.split(",");
    logger.info("Flume Redis Subscribe Source Configured");
  }

  @Override
  public synchronized void start() {
    super.start();

    channelProcessor = getChannelProcessor();
    jedis = new Jedis(host, port, timeout);
    if (!"".equals(password)) {
      jedis.auth(password);
    }
    if (database != 0) {
      jedis.select(database);
    }
    logger.info("Redis Connected. (host: " + host + ", port: " + String.valueOf(port) + ", timeout: " + String.valueOf(timeout) + ", database: " + String.valueOf(database) + ")");
    running = true;
    new Thread(new SubscribeManager()).start();
  }

  @Override
  public synchronized void stop() {
    super.stop();
    running = false;
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private class SubscribeManager implements Runnable {

    private Thread subscribeRunner;
    private JedisPubSub jedisPubSub;

    @Override
    public void run() {
      logger.info("Subscribe Manager Thread is started.");
      jedisPubSub = new JedisSubscribeListener();
      subscribeRunner = new Thread(new SubscribeRunner(jedisPubSub));
      subscribeRunner.start();
      while (running) {
        try {
          Thread.sleep(50);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      jedisPubSub.unsubscribe(channels);
    }
  }

  private class SubscribeRunner implements Runnable {

    private JedisPubSub jedisPubSub;
    public SubscribeRunner(JedisPubSub jedisPubSub) {
      logger.info("Subscribe Runner Thread is started.");
      this.jedisPubSub = jedisPubSub;
    }

    @Override
    public void run() {
      jedis.subscribe(jedisPubSub, channels);
    }
  }

  private class JedisSubscribeListener extends JedisPubSub {

    @Override
    public void onMessage(String channel, String message) {
      Event event = EventBuilder.withBody(message, Charset.forName(charset));
      channelProcessor.processEvent(event);
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
    }

    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
      logger.info("onSubscribe (Channel: " + channel + ")");
    }

    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
      logger.info("onUnsubscribe (Channel: " + channel + ")");
    }

    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {
    }

    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
    }
  }
}
