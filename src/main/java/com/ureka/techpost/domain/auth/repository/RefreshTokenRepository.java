package com.ureka.techpost.domain.auth.repository;

import com.ureka.techpost.domain.auth.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @file RefreshTokenRepository.java
 @author 김동혁, 구본문
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 RefreshToken Entity를 위한 Redis Repository 클래스 입니다.
 */
@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

    // @Indexed로 지정된 필드는 findBy 구문으로 조회 가능
    Optional<RefreshToken> findByTokenValue(String tokenValue);

    // CrudRepository는 기본적으로 Key(@Id) 기반 조회만 빠르고, Indexed 필드 조회는 보조 인덱스를 사용함.
    
    Optional<RefreshToken> findByUsername(String username);

    void deleteByTokenValue(String tokenValue);
}