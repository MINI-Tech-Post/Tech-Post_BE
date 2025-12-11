package com.ureka.techpost.domain.auth.service;

import com.ureka.techpost.domain.auth.entity.RefreshToken;
import com.ureka.techpost.domain.auth.jwt.JwtUtil;
import com.ureka.techpost.domain.auth.repository.RefreshTokenRepository;
import com.ureka.techpost.domain.user.entity.User;
import com.ureka.techpost.global.exception.CustomException;
import com.ureka.techpost.global.exception.ErrorCode;
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
            throw new CustomException(ErrorCode.REFRESH_TOKEN_MISSING);
        }

        try {
            jwtUtil.isExpired(token);
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }

        String category = jwtUtil.getCategory(token);
        if (!category.equals("refresh")) {
            throw new CustomException(ErrorCode.INVALID_TOKEN_CATEGORY);
        }

        if (!existsByTokenValue(token)) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }
    }

    // 액세스 토큰 검증
    public void validateAccessToken(String token) {
        if (token == null) {
            throw new CustomException(ErrorCode.ACCESS_TOKEN_MISSING);
        }

        try {
            jwtUtil.isExpired(token);
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.ACCESS_TOKEN_EXPIRED);
        }

        String category = jwtUtil.getCategory(token);
        if (!category.equals("access")) {
            throw new CustomException(ErrorCode.INVALID_TOKEN_CATEGORY);
        }

    }

}