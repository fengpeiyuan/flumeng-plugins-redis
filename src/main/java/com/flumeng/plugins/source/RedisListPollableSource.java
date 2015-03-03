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

  private String redisHost;
  private int redisPort;
  private String redisListName;
  private int redisTimeout;
  private String redisPassword;
  private int redisDatabase;
  private String messageCharset;

  @Override
  public void configure(Context context) {
    redisHost = context.getString("redisHost", "localhost");
    redisPort = context.getInteger("redisPort", 6379);
    redisListName = context.getString("redisListName");
    redisTimeout = context.getInteger("redisTimeout", 2000);
    redisPassword = context.getString("redisPassword", "");
    redisDatabase = context.getInteger("redisDatabase", 0);
    messageCharset = context.getString("messageCharset", "utf-8");

    if (redisListName == null) { throw new RuntimeException("Redis list name must be set."); }

    logger.info("Flume Redis list source Configured");
  }

  @Override
  public synchronized void start() {
    jedis = new Jedis(redisHost, redisPort, redisTimeout);
    if (!"".equals(redisPassword)) {
      jedis.auth(redisPassword);
    }
    if (redisDatabase != 0) {
      jedis.select(redisDatabase);
    }

    logger.info("Redis Connected. (host: " + redisHost + ", port: " + String.valueOf(redisPort)
                + ", timeout: " + String.valueOf(redisTimeout) + ")");
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
    	String listIndex = jedis.lindex(redisListName, -1);
    	if (null != listIndex) {
    		Event event = EventBuilder.withBody(listIndex.getBytes(messageCharset));
    		channelProcessor.processEvent(event);
    		jedis.lrem(redisListName, 0, listIndex);
    		status = Status.READY;
    	} else {
    		throw new EventDeliveryException(
            "List index value is null,list name: " + redisListName);
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
