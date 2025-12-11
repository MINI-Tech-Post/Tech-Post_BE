package com.ureka.techpost.domain.comment.dto;

import lombok.*;

/**
 * @file CommentResponseDTO.java
 * @author 최승언
 * @version 1.0
 * @since 2025-12-10
 * @description 댓글 목록 조회 API 호출 시, 댓글 정보(ID, 작성자, 내용 등)를 클라이언트에게 반환하기 위해 사용하는 응답 DTO 클래스입니다.
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentResponseDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String content;
}
