package com.ureka.techpost.domain.post.repository;

import com.ureka.techpost.domain.post.dto.PostResponseDTO;
import com.ureka.techpost.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @file PostRepository.java
 * @author 최승언
 * @version 1.2
 * @since 2025-12-09
 * @description 게시글(Post) 엔티티의 기본적인 CRUD 및 JPQL 쿼리를 담당하는 JPA Repository 인터페이스입니다.
 */

@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    boolean existsByOriginalUrl(String originalUrl);

    @Query("SELECT new com.ureka.techpost.domain.post.dto.PostResponseDTO(" +
            "p.id, p.title, p.summary, p.originalUrl, p.thumbnailUrl, " +
            "p.publisher, p.publishedAt, p.sourceName, p.createdAt, " +
            "(SELECT count(l) FROM Likes l WHERE l.post.id = p.id), " +
            "(SELECT count(c) FROM Comment c WHERE c.post.id = p.id)) " +
            "FROM Post p")
    Page<PostResponseDTO> findPostList(Pageable pageable);

    @Query("SELECT new com.ureka.techpost.domain.post.dto.PostResponseDTO(" +
            "p.id, p.title, p.summary, p.originalUrl, p.thumbnailUrl, " +
            "p.publisher, p.publishedAt, p.sourceName, p.createdAt, " +
            "(SELECT count(l) FROM Likes l WHERE l.post.id = p.id), " +
            "(SELECT count(c) FROM Comment c WHERE c.post.id = p.id)) " +
            "FROM Post p " +
            "WHERE p.id = :postId")
    Optional<PostResponseDTO> findPostById(@Param("postId") Long postId);

}
