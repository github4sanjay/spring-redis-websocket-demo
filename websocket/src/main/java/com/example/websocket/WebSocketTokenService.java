package com.example.websocket;

import java.time.Duration;
import java.util.UUID;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class WebSocketTokenService {
  private final ReactiveValueOperations<String, String> reactiveValueOps;

  public WebSocketTokenService(ReactiveStringRedisTemplate redisTemplate) {
    this.reactiveValueOps = redisTemplate.opsForValue();
  }

  public Mono<String> getToken(String userId) {
    return Mono.fromSupplier(UUID::randomUUID)
        .flatMap(
            token ->
                reactiveValueOps
                    .set(token.toString(), userId, Duration.ofMinutes(1))
                    .thenReturn(token.toString()));
  }

  public Mono<String> getUserId(String token) {
    return reactiveValueOps.get(token);
  }
}
