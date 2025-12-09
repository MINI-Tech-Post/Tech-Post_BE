/**
 * @file ChatService.java
 @author 이재, 강승우
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 채팅 관련 서비스 클래스입니다.
 */

package com.ureka.techpost.domain.chat.service;

import com.ureka.techpost.domain.auth.dto.CustomUserDetails;
import com.ureka.techpost.domain.chat.dto.request.ChatMessageReq;
import com.ureka.techpost.domain.chat.dto.response.ChatRoomRes;
import com.ureka.techpost.domain.chat.entity.ChatMessage;
import com.ureka.techpost.domain.chat.entity.ChatRoom;
import com.ureka.techpost.domain.chat.repository.ChatMessageRepository;
import com.ureka.techpost.domain.chat.repository.ChatRoomRepository;
import com.ureka.techpost.domain.user.entity.User;
import com.ureka.techpost.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public List<ChatRoomRes> getChatRoomList() {
      List<ChatRoom> chatRoomList = chatRoomRepository.findAll();
      List<ChatRoomRes> chatRoomResList = new ArrayList<>();

      for (ChatRoom chatRoom : chatRoomList)
        chatRoomResList.add(ChatRoomRes.from(chatRoom));

      return chatRoomResList;
    }

    @Transactional
    public void saveMessage(Long roomId, Long userId, ChatMessageReq chatMessageReq) {
      ChatRoom chatRoom = chatRoomRepository.findById(roomId)
          .orElseThrow(() -> new EntityNotFoundException("room cannot be found"));

      User sender = userRepository.findById(userId)
          .orElseThrow(() -> new EntityNotFoundException("member cannot be found"));

      ChatMessage chatMessage = ChatMessage.builder()
          .chatRoom(chatRoom)
          .user(sender)
          .content(chatMessageReq.getMessage())
          .build();

      chatMessageRepository.save(chatMessage);
    }
}