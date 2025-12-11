/**
 * @file ChatRoomRepository.java
 @author 이재, 강승우
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 채팅방 Repository 클래스입니다.
 */

package com.ureka.techpost.domain.chat.repository;

import com.ureka.techpost.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
}
