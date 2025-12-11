/**
 * @file StompController.java
 @author 이재, 강승우
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 메시지 전송을 위한 컨트롤러 클래스입니다.
 */

package com.ureka.techpost.domain.chat.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ureka.techpost.domain.auth.dto.CustomUserDetails;
import com.ureka.techpost.domain.chat.dto.request.ChatMessageReq;
import com.ureka.techpost.domain.chat.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class StompController {

    private final SimpMessageSendingOperations messageTemplate;
    private final ChatService chatService;

    @MessageMapping("/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, SimpMessageHeaderAccessor accessor, @Payload @Valid ChatMessageReq chatMessageReq) {
      CustomUserDetails customUserDetails = (CustomUserDetails) accessor.getSessionAttributes().get("user");
      Long userId = customUserDetails.getUser().getUserId();

      chatService.saveMessage(roomId, userId, chatMessageReq);
      messageTemplate.convertAndSend("/topic/" + roomId, chatMessageReq);
    }
}