package com.example.websocket;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Slf4j
public class SubscriptionHandler implements WebSocketHandler {

  @Nonnull
  @Override
  public Mono<Void> handle(WebSocketSession webSocketSession) {
    return webSocketSession
        .receive()
        .doOnNext(webSocketMessage -> log.info("Message received {}", webSocketMessage))
        .doOnSubscribe(
            subscription -> {
              log.info("User '{}' connected.", webSocketSession.getId());
            })
        .doOnError(
            throwable -> log.error("Error Occurred while sending message to Redis.", throwable))
        .doFinally(
            signalType -> {
              log.info("User '{}' disconnected.", webSocketSession.getId());
            })
        .then();
  }
}
