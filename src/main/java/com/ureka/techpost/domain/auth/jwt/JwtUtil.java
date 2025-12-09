package com.ureka.techpost.domain.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * @file JwtUtil.java
 @author 김동혁, 구본문
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 JWT의 생성, 검증, 만료 확인, 정보 추출(파싱) 등을 담당하는 유틸리티 컴포넌트입니다.
 */
@Slf4j
@Component
public class JwtUtil {

	@Value("${jwt.secret}")
	private String secretKey;

	@Value("${jwt.access-token-expiration-time}")
	private long ACCESS_TOKEN_EXPIRE_TIME;

	@Value("${jwt.refresh-token-expiration-time}")
	private long REFRESH_TOKEN_EXPIRE_TIME;

	// 토큰 암호화 키
	private SecretKey key;

    // application.yml에서 jwt.secret 값을 가져와서 비밀 키로 세팅
	@PostConstruct
	public void init() {
		this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
	}

    // Access 토큰 생성 메소드
    // username, role, category 담겨있음
    public String generateAccessToken(String category, String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .claim("category", category)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE_TIME))
                .signWith(key)
                .compact();
    }

    // Refresh 토큰 생성 메소드
    // category만 담겨있음
    public String generateRefreshToken(String category) {
        return Jwts.builder()
                .claim("category", category)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(key)
                .compact();
    }

    // JWT로부터 subject를 꺼내서 username 확인
	public String getUsernameFromToken(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
	}

    // JWT로부터 role claim 추출
	public String getRoleFromToken(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().get("role", String.class);
	}

    // JWT로부터 category 추출 (access, refresh 구분)
	public String getCategory(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().get("category", String.class);
	}

    // 토큰이 만료되었으면 true, 아니면 false
	public Boolean isExpired(String token) {
		return Jwts.parser().verifyWith(key).build().parseSignedClaims(token)
				.getPayload().getExpiration().before(new Date());
	}

	// 만료된 토큰에서 username 추출
	public String getUsernameFromExpirationToken(String token) {
		try {
			return Jwts.parser()
					.verifyWith(key)
					.build()
					.parseSignedClaims(token)
					.getPayload()
					.getSubject();
		} catch (ExpiredJwtException e) {
			// 만료된 토큰이어도 일단 내부 정보 반환(재발급 시 사용자 정보가 필요할 수 있음)
			return e.getClaims().getSubject();
		}
	}
}
