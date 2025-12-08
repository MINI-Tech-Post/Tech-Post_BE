/**
 * @file ChatParticipant.java
 @author 이재, 강승우
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 채팅 - 회원 중간 테이블 Jpa Entity 클래스입니다.
 */

package com.ureka.techpost.domain.chat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
public class ChatParticipant {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "member_id", nullable = false)
//    private Member member;
}
