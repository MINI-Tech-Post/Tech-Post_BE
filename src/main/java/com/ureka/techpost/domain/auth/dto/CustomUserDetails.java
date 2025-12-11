package com.ureka.techpost.domain.auth.dto;

import com.ureka.techpost.domain.user.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * @file CustomUserDetails.java
 @author 김동혁, 구본문
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 Spring Security의 UserDetails와 OAuth2User 인터페이스를 구현한 커스텀 클래스입니다.
 */
@Getter
public class CustomUserDetails implements UserDetails, OAuth2User {

	private final User user;
	private Map<String, Object> attributes;

	// 일반 로그인 생성자
	public CustomUserDetails(User user) {
		this.user = user;
	}

	// OAuth2 로그인 생성자
	public CustomUserDetails(User user, Map<String, Object> attributes) {
		this.user = user;
		this.attributes = attributes;
	}


	// === UserDetails 구현 ===
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {

		Collection<GrantedAuthority> collection = new ArrayList<>();
		collection.add(new GrantedAuthority() {
			@Override
			public String getAuthority() {
				return user.getRoleName();
			}
		});

		return collection;
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}

	// === OAuth2User 구현 ===
	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public String getName() {
		return user.getProviderId();
	}
}