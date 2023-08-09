package com.example.websocket;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class ReactiveWebSocketConfig {

  @Bean
  public SubscriptionHandler webSocketHandler(MessagingService messagingService) {
    return new SubscriptionHandler(messagingService);
  }

  @Bean
  public HandlerMapping webSocketHandlerMapping(SubscriptionHandler webSocketHandler) {
    Map<String, WebSocketHandler> map = new HashMap<>();
    map.put("/v1/subscriptions/**", webSocketHandler);
    SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
    handlerMapping.setCorsConfigurations(
        Collections.singletonMap("*", new CorsConfiguration().applyPermitDefaultValues()));
    handlerMapping.setOrder(1);
    handlerMapping.setUrlMap(map);
    return handlerMapping;
  }

  @Bean
  public WebSocketHandlerAdapter handlerAdapter(WebSocketService webSocketService) {
    return new WebSocketHandlerAdapter(webSocketService);
  }

  @Bean
  public WebSocketService getWebSocketService(WebSocketTokenService webSocketTokenService) {
    return new HandshakeWebSocketService(
        new PrincipalRequestUpgradeStrategy(webSocketTokenService));
  }
}
