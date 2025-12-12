package com.ureka.techpost.domain.auth.jwt;

import com.ureka.techpost.domain.auth.dto.CustomUserDetails;
import com.ureka.techpost.domain.user.entity.User;
import com.ureka.techpost.domain.user.repository.UserRepository;
import com.ureka.techpost.domain.auth.service.TokenService;
import com.ureka.techpost.global.exception.CustomException;
import com.ureka.techpost.global.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @author 김동혁, 구본문
 * @version 1.0
 * @file JwtAuthenticationFilter.java
 * @description 이 파일은 모든 API 요청이 올 때마다 가장 먼저 실행되어 토큰 검사하는 클래스입니다.
 * @since 2025-12-08
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        // reissue 요청은 헤더에 access 토큰이 아닌 refresh 토큰이 필요하기 때문에,
        // JwtAuthenticationFilter의 검증 로직을 건너뛰어야 함
        return requestURI.equals("/api/auth/reissue");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 토큰 추출
        String accessToken = resolveToken(request);

        // 토큰이 없으면 다음 필터로 진행
        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 유효성 검증 (실패 시 즉시 종료)
        if (!validateToken(response, accessToken)) {
            return;
        }

        // 인증 처리
        if (!authenticateUser(response, accessToken)) {
            return;
        }

        // 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.split(" ")[1];
        }
        return null;
    }

    private boolean validateToken(HttpServletResponse response, String accessToken) throws IOException {
        // 토큰 만료 여부 확인
        if (jwtUtil.isExpired(accessToken)) {
            log.warn("[JwtFilter] expired access token");
            sendError(response, 401, "액세스 토큰 만료");
            return false;
        }

        // 토큰 카테고리 확인
        String category = jwtUtil.getCategory(accessToken);
        if (!"access".equals(category)) {
            log.warn("[JwtFilter] invalid token category: {}", category);
            sendError(response, 401, "카테고리 까보니 액세스토큰이 아님");
            return false;
        }

        return true;
    }

    private boolean authenticateUser(HttpServletResponse response, String accessToken) throws IOException {
        // 토큰에서 username 추출
        String username;
        try {
            username = jwtUtil.getUsernameFromToken(accessToken);
        } catch (Exception e) {
            log.warn("[JwtFilter] token parsing failed", e);
            sendError(response, 401, "유효하지 않은 토큰입니다.");
            return false;
        }

        // DB에서 사용자 조회
        User foundUser = userRepository.findByUsername(username)
                .orElse(null);

        if (foundUser == null) {
            log.warn("[JwtFilter] user not found. username={}", username);
            sendError(response, 401, "[회원이 존재하지 않습니다.");
            return false;
        }

        // UserDetails 객체 생성
        CustomUserDetails customUserDetails = new CustomUserDetails(foundUser);

        // 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

        // SecurityContext에 설정
        SecurityContextHolder.getContext().setAuthentication(authToken);

        return true;

    }

    private void sendError(HttpServletResponse response, int status, String msg) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"" + msg + "\"}");
    }
}
