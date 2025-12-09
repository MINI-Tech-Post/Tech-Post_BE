package com.ureka.techpost.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "아이디는 필수입니다.")
	private String username;
    @NotBlank(message = "비밀번호는 필수입니다.")
	private String password;
}
