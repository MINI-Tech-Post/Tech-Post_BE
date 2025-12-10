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
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND , "해당 댓글을 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."),
    USER_NOT_MATCH(HttpStatus.FORBIDDEN, "수정 및 삭제 권한이 없습니다."),

    //Auth
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT,"이미 가입 되어있는 회원 입니다."),//409
    ACCESS_TOKEN_MISSING(HttpStatus.UNAUTHORIZED,"액세스 토큰이 없습니다."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 액세스 토큰입니다."),
    REFRESH_TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 없습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 리프레시 토큰입니다."),
    INVALID_TOKEN_CATEGORY(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰 타입입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "DB에 존재하지 않는 리프레시 토큰입니다.");//401



    private final HttpStatus status;
    private final String message;
}
