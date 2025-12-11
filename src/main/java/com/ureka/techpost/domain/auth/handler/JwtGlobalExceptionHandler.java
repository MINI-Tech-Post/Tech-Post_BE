package com.ureka.techpost.domain.auth.handler;

import com.ureka.techpost.domain.auth.dto.ErrorResponseDto;
import com.ureka.techpost.domain.auth.exception.InvalidTokenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @file JwtGlobalExceptionHandler.java
 @author 김동혁, 구본문
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 애플리케이션 전역에서 발생하는 예외(토큰 오류, 로그인 실패 등)를 감지하여 표준화된 에러 응답(JSON)으로 변환해주는 글로벌 예외 처리 핸들러입니다.
 */
@RestControllerAdvice
public class JwtGlobalExceptionHandler {

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidTokenException(InvalidTokenException ex) {
        return ErrorResponseDto.toResponseEntity(HttpStatus.UNAUTHORIZED.value(), "토큰 오류", ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDto> handleBadCredentialsException(BadCredentialsException ex) {
        return ErrorResponseDto.toResponseEntity(HttpStatus.UNAUTHORIZED.value(), "로그인 실패", "비밀번호가 일치하지 않습니다.");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseDto> handleRuntimeException(RuntimeException ex) {
        // "이미 가입되어 있는 회원입니다." 와 같은 회원가입 시의 예외를 처리
        if ("이미 가입되어 있는 회원입니다.".equals(ex.getMessage())) {
            return ErrorResponseDto.toResponseEntity(HttpStatus.CONFLICT.value(), "회원가입 오류", ex.getMessage());
        }
        // 그 외 다른 런타임 예외는 일반적인 서버 오류로 처리
        return ErrorResponseDto.toResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 오류", "알 수 없는 런타임 오류가 발생했습니다.");
    }
}