package com.ureka.techpost.domain.auth.service;

import com.ureka.techpost.domain.auth.entity.RefreshToken;
import com.ureka.techpost.domain.auth.exception.InvalidTokenException;
import com.ureka.techpost.domain.auth.jwt.JwtUtil;
import com.ureka.techpost.domain.auth.repository.RefreshTokenRepository;
import com.ureka.techpost.domain.user.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @file TokenService.java
 @author 김동혁, 구본문
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 Refresh 토큰의 저장·삭제(Redis) 및 로그아웃, 토큰 유효성 검증 등 토큰의 전반적인 생명주기를 관리하는 서비스 클래스입니다.
 */
@Service
@RequiredArgsConstructor
public class TokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    // 로그아웃 처리
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("refresh")) {
                    refresh = cookie.getValue();
                    break;
                }
            }
        }

        // 토큰이 존재하면 검증 및 DB 삭제 시도
        if (refresh != null) {
            try {
                // 토큰 검증 (만료, 위조, DB 존재 여부 확인)
                validateRefreshToken(refresh);
                // DB에서 Refresh 토큰 제거
                deleteByTokenValue(refresh);
            } catch (InvalidTokenException e) {
                // 토큰이 유효하지 않거나(만료 등), 이미 DB에 없는 경우
                // 로그아웃 과정이므로 무시하고 쿠키 삭제로 넘어감
            }
        }

        // response에서 쿠키 제거 (항상 수행하여 클라이언트 상태 정리)
        Cookie cookie = new Cookie("refresh", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    // DB에 Refresh 토큰 저장 (Redis)
    public void addRefreshToken(User user, String refresh) {
        // Redis에 저장할 객체 생성
        // @Id 필드(id)에 user.getUsername()을 사용하여, 사용자별로 하나의 리프레시 토큰만 유지하도록 할 수 있음
        // 또는 refresh 값을 id로 사용하여 다중 로그인을 허용할 수도 있음. 여기서는 username을 키로 사용.
        RefreshToken refreshToken = RefreshToken.builder()
                .id(user.getUsername()) // Key: username
                .username(user.getUsername())
                .tokenValue(refresh)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    // 쿠키 생성
    public Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(24 * 60 * 60);
        cookie.setHttpOnly(true);
        return cookie;
    }

    // DB에 Refresh 토큰이 존재하는지 확인 (Redis)
    public Boolean existsByTokenValue(String tokenValue) {
        // @Indexed 된 필드로 조회
        return refreshTokenRepository.findByTokenValue(tokenValue).isPresent();
    }

    // DB에서 Refresh 토큰을 삭제 (Redis)
    public void deleteByTokenValue(String tokenValue) {
        refreshTokenRepository.deleteByTokenValue(tokenValue);
    }

    // 리프레시 토큰 검증
    public void validateRefreshToken(String token) {
        if (token == null) {
            throw new InvalidTokenException("리프레시 토큰이 없습니다.");
        }

        try {
            jwtUtil.isExpired(token);
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("만료된 리프레시 토큰입니다.");
        }

        String category = jwtUtil.getCategory(token);
        if (!category.equals("refresh")) {
            throw new InvalidTokenException("유효하지 않은 카테고리의 토큰입니다.");
        }

        if (!existsByTokenValue(token)) {
            throw new InvalidTokenException("DB에 존재하지 않는 리프레시 토큰입니다.");
        }
    }

    // 액세스 토큰 검증
    public void validateAccessToken(String token) {
        if (token == null) {
            throw new InvalidTokenException("액세스 토큰이 없습니다.");
        }

        try {
            jwtUtil.isExpired(token);
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("만료된 액세스 토큰입니다.");
        }

        String category = jwtUtil.getCategory(token);
        if (!category.equals("access")) {
            throw new InvalidTokenException("유효하지 않은 카테고리의 토큰입니다.");
        }

    }

}