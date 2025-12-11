/**
 * @file ChatMessageRepository.java
 @author 이재, 강승우
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 채팅 메시지 Repository 클래스입니다.
 */

package com.ureka.techpost.domain.chat.repository;

import com.ureka.techpost.domain.chat.entity.ChatMessage;
import com.ureka.techpost.domain.chat.entity.ChatRoom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomOrderByCreatedAtAsc(ChatRoom chatRoom); // 생성 시간 오름차순으로 정렬
}
