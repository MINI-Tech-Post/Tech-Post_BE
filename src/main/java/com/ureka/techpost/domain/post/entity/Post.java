package com.ureka.techpost.domain.post.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * @file Post.java
 * @author 최승언
 * @version 1.0
 * @since 2025-12-09
 * @description 게시글의 핵심 데이터(제목, 내용, URL 등)를 관리하는 엔티티(Entity) 클래스입니다.
 */

@Entity
@Table(name = "post")
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String summary;

    @Column(name = "original_url", nullable = false, unique = true)
    private String originalUrl;

    @Column(nullable = false)
    private String publisher;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "source_name")
    private String sourceName;

    @Column(name = "thumbnail_url", nullable = false)
    private String thumbnailUrl;

    @Builder
    public Post(String title, String summary, String originalUrl, String publisher, LocalDateTime publishedAt, String sourceName, String thumbnailUrl) {
        this.title = title;
        this.summary = summary;
        this.originalUrl = originalUrl;
        this.publisher = publisher;
        this.publishedAt = publishedAt;
        this.sourceName = sourceName;
        this.thumbnailUrl = thumbnailUrl;
    }
}
