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

        RefreshToken refreshToken = RefreshToken.builder()
				.tokenValue(refresh) // Key: refresh token value
				.username(user.getUsername())
				.build();

        refreshTokenRepository.save(refreshToken);
    }

    // DB에서 Refresh 토큰을 삭제 (Redis)
    public void deleteByTokenValue(String tokenValue) {
        refreshTokenRepository.deleteById(tokenValue);
    }

    // 리프레시 토큰 검증
    public void validateRefreshToken(String token) {
        if (token == null) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_MISSING);
        }

		if (jwtUtil.isExpired(token)) {
			throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);
		}

        String category = jwtUtil.getCategory(token);
        if (!category.equals("refresh")) {
            throw new CustomException(ErrorCode.INVALID_TOKEN_CATEGORY);
        }

        if (!refreshTokenRepository.existsById(token)) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }
    }
}