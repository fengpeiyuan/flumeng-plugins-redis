package com.flumeng.plugins.source;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.PollableSource;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.source.AbstractSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;

public class RedisListPollableSource extends AbstractSource implements Configurable, PollableSource{

  private Logger logger = LoggerFactory.getLogger(RedisListPollableSource.class);
  private Jedis jedis;
  private String host;
  private int port;
  private String listName;
  private int timeout;
  private String password;
  private int database;
  private String charset;

  @Override
  public void configure(Context context) {
    host = context.getString("host", "localhost");
    port = context.getInteger("port", 6379);
    listName = context.getString("listName");
    timeout = context.getInteger("timeout", 2000);
    password = context.getString("password", "");
    database = context.getInteger("database", 0);
    charset = context.getString("charset", "utf-8");

    if (listName == null) { throw new RuntimeException("Redis list name must be set."); }
    logger.info("Flume Redis list source Configured");
  }

  @Override
  public synchronized void start() {
    jedis = new Jedis(host, port, timeout);
    if (!"".equals(password)) {
      jedis.auth(password);
    }
    if (database != 0) {
      jedis.select(database);
    }
    logger.info("Redis Connected. (host: " + host + ", port: " + String.valueOf(port) + ", timeout: " + String.valueOf(timeout) + ")");
    super.start();
  }

  @Override
  public synchronized void stop() {
    jedis.disconnect();
    super.stop();
  }

  @Override
  public Status process() throws EventDeliveryException {
    Status status = Status.READY;;
    ChannelProcessor channelProcessor = getChannelProcessor();
    try {
    	String listIndex = jedis.lindex(listName, -1);
    	if (null != listIndex) {
    		Event event = EventBuilder.withBody(listIndex.getBytes(charset));
    		channelProcessor.processEvent(event);
    		jedis.lrem(listName, 0, listIndex);
    		status = Status.READY;
    	} else {
    		throw new EventDeliveryException(
            "List index value is null,list name: " + listName);
    	}
    } catch (Throwable e) {
      status = Status.BACKOFF;
      if (e instanceof Error) {
        throw (Error) e;
      }
    } 

    return status;
  }
}
