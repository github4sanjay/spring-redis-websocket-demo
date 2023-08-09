package com.example.websocket;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Objects;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebSocketMessage<T> {

  private String id;
  private String userId;
  private Data<T> data;

  /**
   * @param id Id of the web socket message. e.g., 023cd173-460f-4a49-99df-4e4776fa0123
   * @param data Custom data to be delivered in web socket.
   * @param userId User id of the destination user. e.g., 023cd173-460f-4a49-99df-4e4776fa0122
   */
  @Builder
  public WebSocketMessage(Data<T> data, String userId, String id) {
    Objects.requireNonNull(id, "id should not be null");
    Objects.requireNonNull(data, "data should not be null");
    Objects.requireNonNull(userId, "userId should not be null");
    this.data = data;
    this.id = id;
    this.userId = userId;
  }

  @lombok.Data
  @NoArgsConstructor
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
  public static class Data<T> {
    private String event;
    private T data;

    /**
     * @param data Custom data to be delivered in web socket. e.g., {"user": "example@singtel.com"}
     * @param type Type of the message. e.g., DEVICE_REGISTERED, PROFILE_PICTURE_AVAILABLE
     */
    @Builder
    public Data(T data, String type) {
      Objects.requireNonNull(type, "type should not be null");
      this.data = data;
      this.event = type;
    }
  }
}
