/**
 * @file ChatController.java
 @author 이재, 강승우
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 채팅 관련 컨트롤러 클래스입니다.
 */

package com.ureka.techpost.domain.chat.controller;

import com.ureka.techpost.domain.auth.dto.CustomUserDetails;
import com.ureka.techpost.domain.chat.dto.response.ChatMessageRes;
import com.ureka.techpost.domain.chat.dto.response.ChatRoomRes;
import com.ureka.techpost.domain.chat.service.ChatService;
import com.ureka.techpost.global.apiPayload.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @PostMapping
    public ApiResponse<Void> createGroupChatRoom(@RequestParam String roomName, @AuthenticationPrincipal CustomUserDetails userDetails){
        chatService.createGroupChatRoom(roomName, userDetails);
        return ApiResponse.onSuccess(null);
    }

    @GetMapping("/history/{roomId}")
    public ApiResponse<List<ChatMessageRes>> getChatHistory(@PathVariable Long roomId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.onSuccess(chatService.getChatHistory(roomId, userDetails));
    }

    @GetMapping("/my")
    public ApiResponse<List<ChatRoomRes>> getMyChatRoomList(@AuthenticationPrincipal CustomUserDetails UserDetail) {
        return ApiResponse.onSuccess(chatService.getMyChatRoomList(UserDetail));
    }
}
