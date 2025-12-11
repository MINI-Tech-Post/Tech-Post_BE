package com.ureka.techpost.domain.comment.entity;

import com.ureka.techpost.domain.post.entity.Post;
import com.ureka.techpost.domain.user.entity.User;
import com.ureka.techpost.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @file Comment.java
 * @author 최승언
 * @version 1.0
 * @since 2025-12-10
 * @description 게시글에 달리는 댓글 정보를 관리하며, 데이터베이스의 'Comment' 테이블과 매핑되는 엔티티 클래스입니다.
 */

@Entity
@Table(name = "Comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Column
    private String content;

    public void updateContent(String content){
        this.content = content;
    }

    @Builder
    public Comment(User user, Post post, String content){
        this.user = user;
        this.post = post;
        this.content = content;
    }
}
