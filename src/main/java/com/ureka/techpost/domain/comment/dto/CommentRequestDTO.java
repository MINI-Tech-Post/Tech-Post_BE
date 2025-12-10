package com.ureka.techpost.domain.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @file CommentRequestDTO.java
 * @author 최승언
 * @version 1.0
 * @since 2025-12-10
 * @description 클라이언트로부터 댓글 작성 및 수정 요청이 들어올 때, 해당 내용을 전달받기 위해 사용하는 요청 DTO 클래스입니다.
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentRequestDTO {
    private String content;
}
