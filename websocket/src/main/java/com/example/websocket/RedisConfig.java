package com.example.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class RedisConfig {

  @Bean
  ReactiveRedisConnectionFactory reactiveRedisConnectionFactory(RedisProperties redisProperties) {
    RedisStandaloneConfiguration redisStandaloneConfiguration =
        new RedisStandaloneConfiguration(redisProperties.getHost(), redisProperties.getPort());
    redisStandaloneConfiguration.setPassword(redisProperties.getPassword());
    return new LettuceConnectionFactory(redisStandaloneConfiguration);
  }

  @Bean
  ReactiveStringRedisTemplate reactiveStringRedisTemplate(
      ReactiveRedisConnectionFactory reactiveRedisConnectionFactory) {
    return new ReactiveStringRedisTemplate(reactiveRedisConnectionFactory);
  }

  @Bean
  ApplicationRunner subscriptionTopicRunner(
          ReactiveStringRedisTemplate reactiveStringRedisTemplate,
          ObjectStringConverter objectStringConverter,
          MessagingService messagingService) {
    return args -> {
      new OutboundMessageListener(
              reactiveStringRedisTemplate,
              objectStringConverter,
              messagingService,
              WebSocketTopic.SUBSCRIPTION.getTopic())
              .subscribeMessageChannelAndPublishOnWebSocket()
              .doOnSubscribe(
                      subscription ->
                              log.info(
                                      "Redis Listener Started for topic " + WebSocketTopic.SUBSCRIPTION.getTopic()))
              .doOnError(throwable -> log.error("Error listening to Redis topic.", throwable))
              .doFinally(signalType -> log.info("Stopped Listener. Signal Type: {}", signalType))
              .subscribe();
    };
  }
}
