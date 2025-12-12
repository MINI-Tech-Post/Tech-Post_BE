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
import com.ureka.techpost.domain.chat.dto.response.ChatMessageRes;
import com.ureka.techpost.domain.chat.dto.response.ChatRoomRes;
import com.ureka.techpost.domain.chat.entity.ChatMessage;
import com.ureka.techpost.domain.chat.entity.ChatParticipant;
import com.ureka.techpost.domain.chat.entity.ChatRoom;
import com.ureka.techpost.domain.chat.repository.ChatMessageRepository;
import com.ureka.techpost.domain.chat.repository.ChatParticipantRepository;
import com.ureka.techpost.domain.chat.repository.ChatRoomRepository;
import com.ureka.techpost.domain.user.entity.User;
import com.ureka.techpost.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
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
          .orElseThrow(() -> new EntityNotFoundException("user cannot be found"));

      ChatMessage chatMessage = ChatMessage.builder()
          .chatRoom(chatRoom)
          .user(sender)
          .content(chatMessageReq.getMessage())
          .build();

      chatMessageRepository.save(chatMessage);
    }

    @Transactional
    public void createGroupChatRoom(String roomName, CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("user cannot be found"));

        ChatRoom chatRoom = ChatRoom.builder()
            .roomName(roomName)
            .build();
        chatRoomRepository.save(chatRoom);

        ChatParticipant chatParticipant = ChatParticipant.builder()
            .chatRoom(chatRoom)
            .user(user)
            .build();
        chatParticipantRepository.save(chatParticipant);
    }

    public List<ChatMessageRes> getChatHistory(Long roomId, CustomUserDetails userDetails) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new EntityNotFoundException("room cannot be found"));

        Long userId = userDetails.getUser().getUserId();

        boolean isParticipant = chatParticipantRepository.existsByChatRoom_IdAndUser_UserId(roomId, userId);
        if(!isParticipant) throw new IllegalArgumentException("속하지 않은 채팅방입니다.");

        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomOrderByCreatedAtAsc(chatRoom);
        List<ChatMessageRes> chatMessageResList = new ArrayList<>();

        for (ChatMessage chatMessage : chatMessages) {
            ChatMessageRes chatMessageRes = ChatMessageRes.builder()
                .message(chatMessage.getContent())
                .senderId(chatMessage.getUser().getUsername())
                .senderName(chatMessage.getUser().getName())
                .build();
            chatMessageResList.add(chatMessageRes);
        }

        return chatMessageResList;
    }

    public List<ChatRoomRes> getMyChatRoomList(CustomUserDetails userDetails) {
        return chatParticipantRepository.findAllWithChatRoomByUserId(userDetails.getUser().getUserId())
                .stream()
                .map(chatParticipant -> ChatRoomRes.from(chatParticipant.getChatRoom()))
                .toList();
    }

    @Transactional
    public void joinChatRoom(CustomUserDetails userDetails, Long roomId) {
        User user = userRepository.findById(userDetails.getUser().getUserId())
            .orElseThrow(() -> new EntityNotFoundException("user cannot be found"));
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new EntityNotFoundException("room cannot be found"));

        if (!chatParticipantRepository.existsByUserAndChatRoom(user, chatRoom)){
            ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .user(user)
                .build();
            chatParticipantRepository.save(chatParticipant);
        }
    }

    @Transactional
    public void leaveChatRoom(CustomUserDetails userDetails, Long roomId) {
        User user = userRepository.findById(userDetails.getUser().getUserId())
                .orElseThrow(() -> new EntityNotFoundException("user cannot be found"));
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("room cannot be found"));
        ChatParticipant chatParticipant = chatParticipantRepository.findByUserAndChatRoom(user, chatRoom)
                .orElseThrow(() -> new EntityNotFoundException("chat participant cannot be found"));

        chatParticipantRepository.delete(chatParticipant);
    }
}