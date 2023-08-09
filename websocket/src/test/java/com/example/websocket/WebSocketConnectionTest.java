package com.example.websocket;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakeException;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.test.StepVerifier;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = MockRedis.class)
class WebSocketConnectionTest {
  @Autowired private ObjectStringConverter objectMapper;
  @Autowired private WebSocketTokenService webSocketTokenService;
  @Autowired private WebTestClient webTestClient;
  @LocalServerPort private int port;

  @Test
  @DisplayName(
      "test websocket connection when valid token is provided should have successful connection")
  void testWebsocketConnectionWhenValidTokenIsProvidedShouldHaveSuccessfulConnection() {
    var userId = "593b45a0-eeeb-4688-9507-42f70dd93a47";

    var response =
        webTestClient
            .get()
            .uri(WebSocketAPI.WEBSOCKET_TOKEN_API)
            .accept(MediaType.APPLICATION_JSON)
            .header("X-AUTHORIZATION-ID", userId)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(WebSocketAPI.WebSocketTokenResponse.class)
            .returnResult();

    var webSocketTokenResponse = response.getResponseBody();
    Assertions.assertNotNull(webSocketTokenResponse);
    Assertions.assertNotNull(webSocketTokenResponse.getToken());

    var firstTime = new AtomicBoolean(true);
    var test =
        webSocketTokenService
            .getToken(userId)
            .flatMap(
                token -> {
                  String webSocketConnectionUrl =
                      "http://localhost:"
                          + port
                          + "/v1/subscriptions?token="
                          + webSocketTokenResponse.getToken();
                  WebSocketClient webSocketClient = new ReactorNettyWebSocketClient();
                  return webSocketClient
                      .execute(
                          URI.create(webSocketConnectionUrl),
                          session ->
                              session
                                  .receive()
                                  .doOnNext(
                                      webSocketMessage -> {
                                        if (firstTime.compareAndSet(true, false)) {
                                          assertThat(
                                              webSocketMessage
                                                  .getPayloadAsText()
                                                  .equals(
                                                      """
                                                  {
                                                    "event": "LIFE_CYCLE",
                                                    "data": {
                                                      "message": "connected"
                                                    }
                                                  }
                                                  """));
                                        } else {
                                          assertThat(
                                              webSocketMessage
                                                  .getPayloadAsText()
                                                  .equals(
                                                      """
                                                      {
                                                         "event": "DEVICE_REGISTERED",
                                                         "data": {
                                                           "accountId": "ac19db1c-d128-4845-994e-d8f6de9b03ea",
                                                           "factorType": "email"
                                                         }
                                                       }
                                                    """));
                                        }
                                      })
                                  .then(session.close()))
                      .timeout(Duration.ofSeconds(5));
                });

    StepVerifier.create(test)
        .then(this::postMessages)
        .expectNextCount(0)
        .expectError(TimeoutException.class)
        .verify();
  }

  @Test
  @DisplayName(
      "test websocket connection when invalid token is provided should have handshake error")
  void testWebsocketConnectionWhenValidTokenIsProvidedShouldHaveHandshakeError() {

    var webSocketConnectionUrl =
        "http://localhost:" + port + "/v1/subscriptions?token=593b45a0-eeeb-4688-9507-42f70dd93a47";
    WebSocketClient webSocketClient = new ReactorNettyWebSocketClient();
    var test =
        webSocketClient
            .execute(
                URI.create(webSocketConnectionUrl),
                session -> session.receive().then(session.close()))
            .timeout(Duration.ofSeconds(5));

    StepVerifier.create(test).expectError(WebSocketClientHandshakeException.class).verify();
  }

  private void postMessages() {
    IntStream.range(0, 5)
        .forEach(
            ignored ->
                webTestClient
                    .post()
                    .uri(WebSocketAPI.WEBSOCKET_MESSAGE_API)
                    .bodyValue(
                        objectMapper.stringToObjectNonReactive(
                            """
                                {
                                   "message": {
                                     "id": "cd4a11fb-7413-4933-b6aa-b1e62754e361",
                                     "userId": "593b45a0-eeeb-4688-9507-42f70dd93a47",
                                     "data": {
                                       "event": "DEVICE_REGISTERED",
                                       "data": {
                                         "accountId": "ac19db1c-d128-4845-994e-d8f6de9b03ea",
                                         "factorType": "email"
                                       }
                                     }
                                   }
                                 }
                                """,
                            WebSocketAPI.WebSocketMessageRequest.class))
                    .exchange()
                    .expectStatus()
                    .is2xxSuccessful());
  }
}
