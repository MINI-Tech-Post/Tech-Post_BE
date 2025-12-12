/**
 * @file ChatRoomRes.java
 @author 이재, 강승우
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 채팅방 목록 조회 시 사용되는 Response Dto 클래스입니다.
 */


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

  private Long participantCount;

  public static ChatRoomRes from(ChatRoom chatRoom, long participantCount) {
    return ChatRoomRes.builder()
        .roomId(chatRoom.getId())
        .roomName(chatRoom.getRoomName())
        .participantCount(participantCount)
        .build();
  }
}
