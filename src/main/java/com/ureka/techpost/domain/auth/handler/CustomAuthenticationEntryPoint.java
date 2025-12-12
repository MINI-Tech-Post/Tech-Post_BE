package com.ureka.techpost.domain.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureka.techpost.global.exception.ErrorCode;
import com.ureka.techpost.global.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @file CustomAuthenticationEntryPoint.java
 * @author 김동혁, 구본문
 * @version 1.0
 * @since 2025-12-10
 * @description 인증 실패 시 호출되는 핸들러. Request 속성에 저장된 구체적인 에러 정보를 확인하여 응답합니다.
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        Object exception = request.getAttribute("exception");
        ErrorResponse errorResponse;

        if (exception instanceof ErrorCode errorCode) {
            errorResponse = ErrorResponse.builder()
                    .status(errorCode.getStatus())
                    .code(errorCode.name())
                    .message(errorCode.getMessage())
                    .build();
            response.setStatus(errorCode.getStatus().value());
        } else {
            errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.UNAUTHORIZED)
                    .code("AUTHENTICATION_FAILED")
                    .message("인증에 실패했습니다.")
                    .build();
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }

        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
