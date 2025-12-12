package com.ureka.techpost.domain.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureka.techpost.global.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @file CustomAuthenticationFailureHandler.java
 @author 김동혁, 구본문
 @version 1.0
 @since 2025-12-10
 @description 이 파일은 전역 예외 형식에 맞게 예외를 보내주는 파일입니다. error 401
 */

@Component
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {

        // 예외 메시지 추출 (OAuth2AuthenticationException 또는 기타 AuthenticationException)
        String errorMessage = exception.getMessage() != null
                ? exception.getMessage()
                : "소셜 로그인에 실패했습니다.";

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(HttpStatus.UNAUTHORIZED)
                .code("OAUTH2_LOGIN_FAILED")
                .message(errorMessage)
                .build();

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");

        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
