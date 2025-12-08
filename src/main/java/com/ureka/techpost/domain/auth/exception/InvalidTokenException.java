package com.ureka.techpost.domain.auth.exception;

/**
 * @file InvalidTokenException.java
 @author 김동혁, 구본문
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 유효하지 않은 토큰과 관련된 오류 상황에서 발생하는 사용자 정의 런타임 예외(Custom Exception) 클래스입니다.
 */
public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
