package com.ureka.techpost.domain.chat.service;

import com.ureka.techpost.domain.chat.dto.response.ChatRoomRes;
import com.ureka.techpost.domain.chat.entity.ChatRoom;
import com.ureka.techpost.domain.chat.repository.ChatRoomRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;

    public List<ChatRoomRes> getChatRoomList() {
      List<ChatRoom> chatRoomList = chatRoomRepository.findAll();
      List<ChatRoomRes> chatRoomResList = new ArrayList<>();

      for (ChatRoom chatRoom : chatRoomList)
        chatRoomResList.add(ChatRoomRes.from(chatRoom));

      return chatRoomResList;
    }
}