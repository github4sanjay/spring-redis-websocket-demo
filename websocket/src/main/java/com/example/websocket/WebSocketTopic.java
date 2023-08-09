package com.example.websocket;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WebSocketTopic {
  SUBSCRIPTION("subscription");

  private final String topic;
}
