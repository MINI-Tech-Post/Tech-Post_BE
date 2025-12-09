package com.ureka.techpost.domain.chat.repository;

import com.ureka.techpost.domain.chat.entity.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    boolean existsByChatRoom_IdAndUser_UserId(Long chatRoomId, Long userId);
}
