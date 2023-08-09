package com.example.websocket;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Validated
@RestController
@Tag(name = "WebSocket", description = "Set of endpoints for accessing websocket")
public interface WebSocketAPI {

  String WEBSOCKET_TOKEN_API = "/api/v1/websocket/token";
  String WEBSOCKET_MESSAGE_API = "/api/v1/websocket/messages";

  @Operation(
      summary = "Get a token to connect to websocket",
      description =
          "Get a token to connect to websocket. During websocket connection token can be sent in param.")
  @ApiResponse(responseCode = "200")
  @GetMapping(WEBSOCKET_TOKEN_API)
  Mono<WebSocketTokenResponse> getWebSocketToken(
      @RequestHeader(value = "X-AUTHORIZATION-ID") UUID userId);

  @Operation(
      hidden = true,
      summary = "Send message through websocket",
      description =
          "Send a message through websocket. Delivering depends on at that point user is connected or not.")
  @ApiResponse(responseCode = "200")
  @PostMapping(WEBSOCKET_MESSAGE_API)
  Mono<Void> sendWebSocketMessage(@RequestBody WebSocketMessageRequest<?> webSocketMessage);

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(name = "WebSocketTokenResponse", description = "Sample websocket token response object")
  class WebSocketTokenResponse {

    @Schema(
        name = "token",
        description = "WebSocket token",
        example = "00d79e72-de48-411b-94db-b0e824e11d9d")
    String token;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(name = "WebSocketMessageRequest", description = "Sample websocket message request object")
  class WebSocketMessageRequest<T> {

    @Schema(name = "message", description = "WebSocket message")
    WebSocketMessage<T> message;
  }
}
