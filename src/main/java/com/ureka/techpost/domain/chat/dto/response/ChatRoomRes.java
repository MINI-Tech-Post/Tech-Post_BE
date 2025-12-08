package com.ureka.techpost.domain.chat.dto.response;

import com.ureka.techpost.domain.chat.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class ChatRoomRes {

  private Long roomId;

  private String roomName;

  public static ChatRoomRes from(ChatRoom chatRoom) {
    return ChatRoomRes.builder()
        .roomId(chatRoom.getId())
        .roomName(chatRoom.getRoomName())
        .build();
  }
}
