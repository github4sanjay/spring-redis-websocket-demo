package com.example.websocket;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WebSocketController implements WebSocketAPI {

  private final WebSocketTokenService webSocketTokenService;
  private final MessagingService messagingService;

  @Override
  public Mono<WebSocketTokenResponse> getWebSocketToken(UUID userId) {
    return webSocketTokenService
        .getToken(userId.toString())
        .map(token -> WebSocketTokenResponse.builder().token(token).build());
  }

  @Override
  public Mono<Void> sendWebSocketMessage(WebSocketMessageRequest<?> webSocketMessage) {
    return messagingService.onNext(webSocketMessage.getMessage()).then();
  }
}
