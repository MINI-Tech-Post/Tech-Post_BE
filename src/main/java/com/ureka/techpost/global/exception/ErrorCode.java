package com.ureka.techpost.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 예시
    //Post
    POST_NOT_FOUND(HttpStatus.BAD_REQUEST, "해당 게시글을 찾을 수 없습니다."),
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND , "해당 댓글을 찾을 수 없습니다.");


    private final HttpStatus status;
    private final String message;
}
