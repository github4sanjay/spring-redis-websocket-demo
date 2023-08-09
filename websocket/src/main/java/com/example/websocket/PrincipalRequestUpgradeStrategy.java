package com.example.websocket;

import com.sun.security.auth.UserPrincipal;
import jakarta.annotation.Nullable;
import java.util.StringTokenizer;
import java.util.function.Supplier;
import javax.naming.AuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.AbstractServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.adapter.NettyWebSocketSessionSupport;
import org.springframework.web.reactive.socket.adapter.ReactorNettyWebSocketSession;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerResponse;

@Slf4j
@RequiredArgsConstructor
public class PrincipalRequestUpgradeStrategy extends ReactorNettyRequestUpgradeStrategy {

  private final WebSocketTokenService webSocketTokenService;

  @Override
  @NonNull
  public Mono<Void> upgrade(
      ServerWebExchange exchange,
      @NonNull WebSocketHandler handler,
      @Nullable String subProtocol,
      Supplier<HandshakeInfo> handshakeInfoFactory) {

    var response = exchange.getResponse();
    var reactorResponse = getNativeResponse(response);
    final var handShakeInfo = handshakeInfoFactory.get();
    final var token = extractValueFromQueryStringAndKey("token", handShakeInfo.getUri().getQuery());
    if (token == null) {
      log.warn("wrong token {} so disconnecting", token);
      response.setStatusCode(HttpStatus.UNAUTHORIZED);
      return response.writeWith(Flux.empty());
    } else {
      return webSocketTokenService
          .getUserId(token)
          .switchIfEmpty(Mono.error(new AuthenticationException("Not authenticated")))
          .flatMap(
              userId -> {
                return reactorResponse.sendWebsocket(
                    (in, out) -> {
                      var session =
                          new ReactorNettyWebSocketSession(
                              in,
                              out,
                              new HandshakeInfo(
                                  handShakeInfo.getUri(),
                                  handShakeInfo.getHeaders(),
                                  handShakeInfo.getCookies(),
                                  Mono.just(new UserPrincipal(userId)),
                                  handShakeInfo.getSubProtocol(),
                                  handShakeInfo.getRemoteAddress(),
                                  handShakeInfo.getAttributes(),
                                  handShakeInfo.getLogPrefix()),
                              (NettyDataBufferFactory) response.bufferFactory(),
                              NettyWebSocketSessionSupport.DEFAULT_FRAME_MAX_SIZE);
                      return handler.handle(session);
                    });
              })
          .doOnError(throwable -> log.warn("wrong token {} so disconnecting", token, throwable));
    }
  }

  private static HttpServerResponse getNativeResponse(ServerHttpResponse response) {
    if (response instanceof AbstractServerHttpResponse) {
      return ((AbstractServerHttpResponse) response).getNativeResponse();
    } else if (response instanceof ServerHttpResponseDecorator) {
      return getNativeResponse(((ServerHttpResponseDecorator) response).getDelegate());
    } else {
      throw new IllegalArgumentException(
          "Couldn't find native response in " + response.getClass().getName());
    }
  }

  public String extractValueFromQueryStringAndKey(String key, String queryString) {
    String foundValue = null;
    if (queryString == null) return null;
    var queryItems = new StringTokenizer(queryString, "&");
    while (queryItems.hasMoreTokens() && foundValue == null) {
      String queryParameter = queryItems.nextToken();
      StringTokenizer paramTokenizer = new StringTokenizer(queryParameter, "=");
      String currentKey = paramTokenizer.nextToken();
      String currentValue = paramTokenizer.nextToken();
      if (currentKey.equalsIgnoreCase(key)) {
        foundValue = currentValue;
      }
    }
    return foundValue;
  }
}
