package com.example.otp;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class MessagePublisher {

  private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;

  @Scheduled(fixedDelay = 5000)
  public void scheduleFixedDelayTask() {
    log.info("publishing message");
    reactiveStringRedisTemplate
        .convertAndSend(WebSocketTopic.SUBSCRIPTION.getTopic(), "Message " + UUID.randomUUID())
        .block();
  }
}
