package com.example.otp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

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
}
