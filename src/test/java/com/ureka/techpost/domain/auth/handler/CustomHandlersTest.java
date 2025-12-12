package com.ureka.techpost.domain.auth.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import static org.junit.jupiter.api.Assertions.*;

class CustomHandlersTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    // AccessDeniedHandler: 권한 부족 시 403 코드, JSON 응답 형식(status/code/message)을 반환하는지 검증
    void accessDeniedHandler_returnsForbiddenJson() throws Exception {
        var handler = new CustomAccessDeniedHandler(new ObjectMapper());
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("denied"));

        // 기대: 권한 부족 시 HTTP 403
        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        // 기대: JSON UTF-8 응답 헤더
        assertEquals("application/json;charset=UTF-8", response.getContentType());
        // 기대: 바디의 status/code/message 필드가 존재하고 값이 비어 있지 않음
        JsonNode body = OBJECT_MAPPER.readTree(response.getContentAsString());
        assertEquals("FORBIDDEN", body.get("status").asText());
        assertEquals("ACCESS_DENIED", body.get("code").asText());
        assertFalse(body.get("message").asText().isBlank());
    }

    @Test
    // AuthenticationEntryPoint: 인증되지 않은 요청에 401 코드와 JSON 응답을 반환하는지 검증
    void entryPoint_returnsUnauthorizedJson() throws Exception {
        var entryPoint = new CustomAuthenticationEntryPoint();
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new AuthenticationException("auth fail") {});

        // 기대: 인증 실패 시 HTTP 401
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        // 기대: JSON UTF-8 응답 헤더
        assertEquals("application/json;charset=UTF-8", response.getContentType());
        // 기대: 바디의 status/code/message 필드가 존재하고 값이 비어 있지 않음
        JsonNode body = OBJECT_MAPPER.readTree(response.getContentAsString());
        assertEquals("UNAUTHORIZED", body.get("status").asText());
        assertEquals("AUTHENTICATION_FAILED", body.get("code").asText());
        assertFalse(body.get("message").asText().isBlank());
    }

    @Test
    // AuthenticationFailureHandler: 로그인 실패 시 401 코드와 JSON 응답을 반환하는지 검증
    void failureHandler_returnsUnauthorizedJson() throws Exception {
        var failureHandler = new CustomOAuth2FailureHandler();
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();

        failureHandler.onAuthenticationFailure(request, response, new AuthenticationException("login fail") {});

        // 기대: 로그인 실패 시 HTTP 401
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        // 기대: JSON UTF-8 응답 헤더
        assertEquals("application/json;charset=UTF-8", response.getContentType());
        // 기대: 바디의 status/code/message 필드가 존재하고 값이 비어 있지 않음
        JsonNode body = OBJECT_MAPPER.readTree(response.getContentAsString());
        assertEquals("UNAUTHORIZED", body.get("status").asText());
        assertEquals("OAUTH2_LOGIN_FAILED", body.get("code").asText());
        assertFalse(body.get("message").asText().isBlank());
    }
}
