package com.ureka.techpost.domain.auth.handler;

import com.ureka.techpost.domain.auth.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

/**
 * @file CustomLogoutHandler.java
 @author 김동혁, 구본문
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 실제 로그아웃 로직(토큰 삭제, 쿠키 정리)을 TokenService에 위임하여 실행하는 커스텀 로그아웃 핸들러 클래스입니다.
 */
@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {

    private final TokenService tokenService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // 로그아웃 로직을 TokenService에 위임
        tokenService.logout(request, response);
    }
}
