/**
 * @file ChatMessageRes.java
 @author 이재, 강승우
 @version 1.0
 @since 2025-12-09
 @description 이 파일은 채팅 내역 불러오기 시 사용되는 Response Dto 클래스입니다.
 */

package com.ureka.techpost.domain.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class ChatMessageRes {

    private String message;
    private String senderName;
    private String senderId;
}