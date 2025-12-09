package com.ureka.techpost.domain.auth.controller;

import com.ureka.techpost.domain.auth.dto.LoginDto;
import com.ureka.techpost.domain.auth.dto.SignupDto;
import com.ureka.techpost.domain.auth.service.AuthService;
import com.ureka.techpost.global.apiPayload.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @file AuthController.java
 @author 김동혁, 구본문
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 사용자 인증과 관련된 HTTP 요청을 받아 처리하는 REST 컨트롤러 클래스입니다.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/signup")
	public ApiResponse<String> signup(@Valid @RequestBody SignupDto signupDto) {
		authService.signup(signupDto);
		return ApiResponse.onSuccess("회원가입 성공");
	}

	@PostMapping("/reissue")
	public ApiResponse<?> reissue(HttpServletRequest request, HttpServletResponse response) {
		return ApiResponse.onSuccess(authService.reissue(request, response));
	}

	@PostMapping("/login")
	public ApiResponse<String> login(@Valid @RequestBody LoginDto loginDto, HttpServletResponse response) {
		authService.login(loginDto, response);
		return ApiResponse.onSuccess("로그인 성공");
	}

}
