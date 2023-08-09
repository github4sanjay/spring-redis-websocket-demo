package com.example.websocket;

import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
public class SubscriptionHandler implements WebSocketHandler {

  private final MessagingService messagingService;

  @Nonnull
  @Override
  public Mono<Void> handle(WebSocketSession webSocketSession) {
    return webSocketSession
        .getHandshakeInfo()
        .getPrincipal()
        .flatMap(
            principal -> {
              var sendMessageFlux =
                  messagingService
                      .getMessages(webSocketSession.getHandshakeInfo().getPrincipal())
                      .map(webSocketSession::textMessage)
                      .doOnError(
                          throwable ->
                              log.error(
                                  "Error Occurred while sending message to WebSocket.", throwable));
              var outputMessage = webSocketSession.send(sendMessageFlux);
              var inputMessage =
                  webSocketSession
                      .receive()
                      .doOnNext(
                          webSocketMessage -> log.info("Message received {}", webSocketMessage))
                      .doOnSubscribe(
                          subscription -> {
                            log.info("User '{}' connected.", principal.getName());
                          })
                      .doOnError(
                          throwable ->
                              log.error(
                                  "Error Occurred while sending message to Redis.", throwable))
                      .doFinally(
                          signalType -> {
                            log.info("User '{}' disconnected.", principal.getName());
                            messagingService.remove(principal.getName());
                          })
                      .then();
              return Mono.zip(inputMessage, outputMessage).then();
            });
  }
}
