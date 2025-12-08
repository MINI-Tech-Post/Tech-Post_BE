package com.ureka.techpost.domain.chat.controller;

import com.ureka.techpost.domain.chat.dto.response.ChatRoomRes;
import com.ureka.techpost.domain.chat.service.ChatService;
import com.ureka.techpost.global.apiPayload.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/chats")
public class ChatController {
    private final ChatService chatService;

    @GetMapping
    public ApiResponse<List<ChatRoomRes>> getChatRoomList() {
      return ApiResponse.onSuccess(chatService.getChatRoomList());
    }
}
