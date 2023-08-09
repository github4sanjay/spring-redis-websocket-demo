package com.example.websocket;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessagingService {

  private static final Map<String, Sinks.Many<String>> sinks = new ConcurrentHashMap<>();

  private final ObjectStringConverter objectStringConverter;

  public <T> Mono<Sinks.EmitResult> onNext(WebSocketMessage<T> next) {
    if (!sinks.containsKey(next.getUserId())) return Mono.just(Sinks.EmitResult.OK);
    return objectStringConverter
        .objectToString(getData(next.getData()))
        .map(s -> sinks.get(next.getUserId()).tryEmitNext(s))
        .doOnSuccess(
            emitResult -> {
              if (emitResult.isFailure()) {
                log.warn("failed to send message with id: {}", next.getId());
              }
            });
  }

  public <T> WebSocketMessage.Data<T> getData(WebSocketMessage.Data<T> data) {
    if (data.getData() instanceof LinkedHashMap<?, ?> map) {
      map.remove("@class");
    }
    return data;
  }

  public Flux<String> getMessages(Mono<Principal> principal) {
    return principal.flatMapMany(
        s -> {
          sinks.putIfAbsent(s.getName(), Sinks.many().multicast().directBestEffort());
          return sinks
              .get(s.getName())
              .asFlux()
              .startWith(
                  objectStringConverter.objectToString(
                      WebSocketMessage.Data.builder()
                          .type("LIFE_CYCLE")
                          .data(LifeCycleMessage.builder().message("connected").build())
                          .build()));
        });
  }

  public void remove(String userId) {
    sinks.remove(userId);
  }

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class LifeCycleMessage {
    private String message;
  }
}
