package com.example.websocket;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;

@Slf4j
@TestConfiguration
public class MockRedis {

  private final RedisServer server;

  @SneakyThrows
  public MockRedis() {
    this.server = new RedisServer(6381);
  }

  @SneakyThrows
  @PostConstruct
  public void postConstruct() {
    try {
      server.start();
      log.info("embedded redis started on port 6381");
    } catch (Exception e) {
      if (!(e.getLocalizedMessage() != null
          && e.getLocalizedMessage().contains("Address already in use"))) {
        log.error("error in starting redis server", e);
      }
    }
  }

  @PreDestroy
  public void preDestroy() {
    try {
      server.stop();
    } catch (Exception e) {
      log.error("error in stopping redis server", e);
    }
  }
}
