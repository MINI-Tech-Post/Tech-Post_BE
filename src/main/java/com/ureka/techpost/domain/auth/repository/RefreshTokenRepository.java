//package com.ureka.techpost.domain.auth.repository;
//
//import com.ureka.techpost.domain.auth.entity.RefreshToken;
//import org.springframework.data.repository.CrudRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.Optional;
//
///**
// * @file RefreshTokenRepository.java
// @author 김동혁, 구본문
// @version 1.0
// @since 2025-12-08
// @description 이 파일은 RefreshToken Entity를 위한 Redis Repository 클래스 입니다.
// */
//@Repository
//public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
//
//    Optional<RefreshToken> findByUsername(String username);
//}