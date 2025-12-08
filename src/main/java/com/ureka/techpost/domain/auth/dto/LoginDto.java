package com.ureka.techpost.domain.auth.dto;

import lombok.Data;

/**
 * @file LoginDto.java
 @author 김동혁, 구본문
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 로그인 요청에 사용되는 DTO 클래스입니다.
 */
@Data
public class LoginDto {

	private String username;
	private String password;
}
