package com.ureka.techpost.domain.post.dto;

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
}
