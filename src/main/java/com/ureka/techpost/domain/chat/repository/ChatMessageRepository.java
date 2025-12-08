/**
 * @file ChatMessageRepository.java
 @author 이재, 강승우
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 채팅 메시지 Repository 클래스입니다.
 */

package com.ureka.techpost.domain.chat.repository;

import com.ureka.techpost.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

}
