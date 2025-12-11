package com.ureka.techpost.domain.user.repository;

import com.ureka.techpost.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @file UserRepository.java
 @author 김동혁, 구본문
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 User Entity를 위한 Repository 클래스입니다.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByUsername(String username);

	boolean existsByUsername(String username);

	// 소셜 로그인 정보로 회원 찾기
	Optional<User> findByProviderAndProviderId(String provider, String providerId);
}
