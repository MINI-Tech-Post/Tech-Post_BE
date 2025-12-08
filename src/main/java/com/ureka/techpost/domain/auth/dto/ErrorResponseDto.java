package com.ureka.techpost.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @file ErrorResponseDto.java
 @author 김동혁, 구본문
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 예외 발생 시 클라이언트에게 반환되는 표준 오류 응답 DTO 클래스입니다.
 */
@Getter
@Builder
public class ErrorResponseDto {

    private final String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    private final int status;
    private final String error;
    private final String message;

    public static ResponseEntity<ErrorResponseDto> toResponseEntity(int status, String error, String message) {
        return ResponseEntity
                .status(status)
                .body(ErrorResponseDto.builder()
                        .status(status)
                        .error(error)
                        .message(message)
                        .build());
    }
}
