package com.ureka.techpost.domain.auth.handler;


import com.ureka.techpost.domain.auth.dto.CustomUserDetails;
import com.ureka.techpost.domain.auth.jwt.JwtUtil;
import com.ureka.techpost.domain.auth.service.TokenService;
import com.ureka.techpost.domain.auth.utils.CookieUtil;
import com.ureka.techpost.domain.user.entity.User;
import com.ureka.techpost.domain.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 * @file OAuth2LoginSuccessHandler.java
 @author 김동혁, 구본문
 @version 1.0
 @since 2025-12-09
 @description 이 파일은 소셜 로그인 성공 로직을 수행하는 클래스입니다.
 */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        CustomUserDetails oAuth2User = (CustomUserDetails) authentication.getPrincipal();

        // 우리 시스템의 JWT 토큰 생성
        String access = jwtUtil.generateAccessToken("access", oAuth2User.getUsername(), oAuth2User.getUser().getName(), oAuth2User.getUser().getRoleName());
        String refresh = jwtUtil.generateRefreshToken("refresh");

        tokenService.addRefreshToken(oAuth2User.getUser(), refresh);
        response.addCookie(CookieUtil.createCookie("refresh", refresh, 1209600));
        
        // 액세스 토큰을 쿼리 파라미터에 담아 프론트엔드 URL로 리다이렉트
		// vue.js 에서 지원하는 포트 번호로 변경해야 함
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/post/list")
                .queryParam("accessToken", access)
                .build().toUriString();
                
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}