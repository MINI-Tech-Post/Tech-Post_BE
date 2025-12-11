package com.ureka.techpost.domain.comment.repository;


import com.ureka.techpost.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @file CommentRepository.java
 * @author 최승언
 * @version 1.0
 * @since 2025-12-09
 * @description 댓글(Comment) 엔티티의 데이터베이스 CRUD 작업을 담당하며, N+1 문제 해결을 위한 Fetch Join 쿼리를 포함하는 리포지토리 인터페이스입니다.
 */

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("select c from Comment c join fetch c.user where c.post.id = :postId")
    List<Comment> findAllByPostId(@Param("postId") Long postId);
}
