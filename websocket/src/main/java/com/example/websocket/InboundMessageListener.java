package com.example.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class InboundMessageListener {

  public Mono<Void> handle(String userId, String message) {
    return Mono.fromCallable(
        () -> {
          log.info("websocket message received for user id {} and message {}", userId, message);
          return null;
        });
  }
}
