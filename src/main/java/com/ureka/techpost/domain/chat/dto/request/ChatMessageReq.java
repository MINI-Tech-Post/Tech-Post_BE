/**
 * @file ChatMessageReq.java
 @author 이재, 강승우
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 채팅 메시지 전송 시 사용되는 Request Dto 클래스입니다.
 */

package com.ureka.techpost.domain.chat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ChatMessageReq {

    private String message;
    private String senderName;
    private String senderId;
}