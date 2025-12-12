package com.ureka.techpost.domain.chat.repository;

import com.ureka.techpost.domain.chat.entity.ChatParticipant;
import com.ureka.techpost.domain.chat.entity.ChatRoom;
import com.ureka.techpost.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
    boolean existsByChatRoom_IdAndUser_UserId(Long chatRoomId, Long userId);

    @Query("SELECT cp FROM ChatParticipant cp " +
            "JOIN FETCH cp.chatRoom " +
            "WHERE cp.user.userId = :userId")
    List<ChatParticipant> findAllWithChatRoomByUserId(@Param("userId") Long userId);

    Optional<ChatParticipant> findByUserAndChatRoom(User user, ChatRoom chatRoom);

    boolean existsByUserAndChatRoom(User user, ChatRoom chatRoom);

    long countByChatRoomId(Long roomId);
}
