package com.example.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class ObjectStringConverter {

  private final ObjectMapper objectMapper;

  public ObjectStringConverter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public <T> Mono<T> stringToObject(String data, Class<T> clazz) {
    return Mono.fromCallable(() -> objectMapper.readValue(data, clazz))
        .doOnError(
            throwable ->
                log.error("Error converting [{}] to class '{}'.", data, clazz.getSimpleName()));
  }

  public <T> T stringToObjectNonReactive(String data, Class<T> clazz) {
    try {
      return objectMapper.readValue(data, clazz);
    } catch (JsonProcessingException e) {
      log.error("Error converting [{}] to String.", clazz);
    }
    return null;
  }

  public <T> Mono<String> objectToString(T object) {
    return Mono.fromCallable(() -> objectMapper.writeValueAsString(object))
        .doOnError(throwable -> log.error("Error converting [{}] to String.", object));
  }

  public <T> String objectToStringNonReactive(T object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      log.error("Error converting [{}] to String.", object);
    }
    return null;
  }

  public <T> Mono<T> stringToObject(String data, TypeReference<T> clazz) {
    return Mono.fromCallable(() -> objectMapper.readValue(data, clazz))
        .doOnError(
            throwable ->
                log.error(
                    "Error converting [{}] to class '{}'.", data, clazz.getType().getTypeName()));
  }

  public <T> JsonNode toJsonNode(T object) {
    return objectMapper.convertValue(object, JsonNode.class);
  }
}
