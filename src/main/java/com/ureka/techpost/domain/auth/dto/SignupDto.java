package com.ureka.techpost.domain.auth.dto;

import com.ureka.techpost.domain.user.entity.User;
import com.ureka.techpost.domain.user.enums.Role;
import lombok.Data;

/**
 * @file SignupDto.java
 @author 김동혁, 구본문
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 회원가입 요청에 사용되는 DTO 클래스입니다.
 */
@Data
public class SignupDto {

	private String username;
	private String password;
	private String name;

	public User toEntity(String encodedPassword) {
		return User.builder()
				.username(username)
				.password(encodedPassword)
				.name(name)
				.provider("NONE")
				.role(Role.ROLE_USER)
				.build();
	}
}
