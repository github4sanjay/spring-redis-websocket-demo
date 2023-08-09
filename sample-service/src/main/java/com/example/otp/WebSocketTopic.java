package com.example.otp;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WebSocketTopic {
  SUBSCRIPTION("subscription");

  private final String topic;
}
