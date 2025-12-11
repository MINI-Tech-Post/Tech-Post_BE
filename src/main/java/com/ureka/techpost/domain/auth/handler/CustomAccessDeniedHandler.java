package com.ureka.techpost.domain.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureka.techpost.global.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
/**
 * @file CustomAuthenticationEntryPoint.java
 @author 김동혁, 구본문
 @version 1.0
 @since 2025-12-10
 @description 이 파일은 전역 예외 형식에 맞게 예외를 보내주는 파일입니다. error 403
 */
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN)
                .code("ACCESS_DENIED")
                .message("접근 권한이 없습니다.")
                .build();

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
