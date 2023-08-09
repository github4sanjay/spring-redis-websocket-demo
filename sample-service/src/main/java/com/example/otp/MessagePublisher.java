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
  private final ObjectStringConverter objectStringConverter;

  @Scheduled(fixedDelay = 5000)
  public void scheduleFixedDelayTask() {
    log.info("publishing message");
    var message =
        WebSocketMessage.<String>builder()
            .id(UUID.randomUUID().toString())
            .userId("00d79e72-de48-411b-94db-b0e824e11d9d")
            .data(WebSocketMessage.Data.<String>builder().data("Sanjay").type("SOME_TYPE").build())
            .build();
    reactiveStringRedisTemplate
        .convertAndSend(
            WebSocketTopic.SUBSCRIPTION.getTopic(),
            objectStringConverter.objectToStringNonReactive(message))
        .block();
  }
}
