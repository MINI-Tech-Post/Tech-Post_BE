package com.ureka.techpost.domain.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

/**
 * @file RefreshToken.java
 @author 김동혁, 구본문
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 리프레시 토큰 정보를 담는 Redis Entity 클래스입니다.
 */
@Getter
@Builder
@AllArgsConstructor
@RedisHash(value = "refreshToken", timeToLive = 60)
public class RefreshToken {

    @Id
    private String id; // Redis Key (일반적으로 username이나 userId 사용)

    @Indexed
    private String tokenValue; // 리프레시 토큰 값 (조회용 인덱스)

    private String username; // 사용자 식별자

    public void updateToken(String tokenValue) {
        this.tokenValue = tokenValue;
    }
}