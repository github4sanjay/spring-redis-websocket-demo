package com.example.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class OutboundMessageListener {

  private final ReactiveStringRedisTemplate reactiveStringRedisTemplate;
  private final ObjectStringConverter objectStringConverter;
  private final MessagingService messagingService;
  private final String patternTopic;

  public Mono<Void> subscribeMessageChannelAndPublishOnWebSocket() {
    return reactiveStringRedisTemplate
            .listenTo(new PatternTopic(patternTopic))
            .map(ReactiveSubscription.Message::getMessage)
            .flatMap(message -> objectStringConverter.stringToObject(message, WebSocketMessage.class))
            .flatMap(messagingService::onNext)
            .then();
  }
}
