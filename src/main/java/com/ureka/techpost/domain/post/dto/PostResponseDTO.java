package com.ureka.techpost.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @file PostResponseDTO.java
 * @author 최승언
 * @version 1.0
 * @since 2025-12-09
 * @description 클라이언트에게 게시글 정보를 응답(Response)할 때 사용하는 데이터를 담는 DTO 클래스입니다.
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostResponseDTO {
    private Long id;
    private String title;
    private String summary;
    private String originalUrl;
    private String thumbnailUrl;
    private String publisher;
    private LocalDateTime publishedAt;
    private String sourceName;
    private LocalDateTime createdAt;

    private Long likeCount;
    private Long commentCount;
    private Boolean isLiked;

    public PostResponseDTO(
        Long id,
        String title,
        String summary,
        String originalUrl,
        String thumbnailUrl,
        String publisher,
        LocalDateTime publishedAt,
        String sourceName,
        LocalDateTime createdAt,
        Long likeCount,
        Long commentCount
    ) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.originalUrl = originalUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.publisher = publisher;
        this.publishedAt = publishedAt;
        this.sourceName = sourceName;
        this.createdAt = createdAt;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.isLiked = false;
    }
}
