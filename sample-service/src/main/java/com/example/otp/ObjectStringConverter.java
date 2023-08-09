package com.example.otp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration(proxyBeanMethods = false)
public class ObjectStringConverter {

  private final ObjectMapper objectMapper;

  public ObjectStringConverter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public <T> String objectToStringNonReactive(T object) {
    try {
      return objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      log.error("Error converting [{}] to String.", object);
    }
    return null;
  }
}
