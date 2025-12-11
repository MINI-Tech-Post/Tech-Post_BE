package com.ureka.techpost.domain.auth.dto;

import com.ureka.techpost.domain.user.entity.User;
import com.ureka.techpost.domain.user.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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


    @NotBlank(message = "아이디는 필수입니다.")
    @Size(min = 4, max = 20, message = "아이디는 4~20자여야 합니다.")
    @Pattern( regexp = "^[a-zA-Z0-9]+$", message = "아이디는 영문과 숫자만 사용할 수 있습니다." )
	private String username;

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8~20자여야 합니다.")
    @Pattern( regexp = "^(?=.*[A-Za-z])(?=.*\\d).*$", message = "비밀번호는 영문과 숫자를 포함해야 합니다." )
	private String password;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 30, message = "이름은 30자를 초과할 수 없습니다.")
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
