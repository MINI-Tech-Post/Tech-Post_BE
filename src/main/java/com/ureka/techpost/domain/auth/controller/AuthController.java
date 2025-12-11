package com.ureka.techpost.domain.auth.controller;

import com.ureka.techpost.domain.auth.dto.LoginDto;
import com.ureka.techpost.domain.auth.dto.SignupDto;
import com.ureka.techpost.domain.auth.service.AuthService;
import com.ureka.techpost.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Authentication", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
	@PostMapping("/signup")
	public ApiResponse<String> signup(
			@Parameter(description = "회원가입 요청 데이터 (아이디, 비밀번호, 이름)", required = true)
			@Valid @RequestBody SignupDto signupDto) {
		authService.signup(signupDto);
		return ApiResponse.onSuccess("회원가입 성공");
	}

	@Operation(summary = "토큰 재발급", description = "Refresh Token을 사용하여 만료된 Access Token을 재발급합니다. Refresh Token은 쿠키에서, 만료된 Access Token은 헤더에서 가져옵니다.")
	@PostMapping("/reissue")
	public ApiResponse<?> reissue(
			@Parameter(hidden = true) HttpServletRequest request,
			@Parameter(hidden = true) HttpServletResponse response) {
		return ApiResponse.onSuccess(authService.reissue(request, response));
	}

	@Operation(summary = "로그인", description = "사용자 이름과 비밀번호로 로그인하여 Access Token 및 Refresh Token을 발급받습니다.")
	@PostMapping("/login")
	public ApiResponse<String> login(
			@Parameter(description = "로그인 요청 데이터 (아이디, 비밀번호)", required = true)
			@Valid @RequestBody LoginDto loginDto,
			@Parameter(hidden = true) HttpServletResponse response) {
		authService.login(loginDto, response);
		return ApiResponse.onSuccess("로그인 성공");
	}

	@Operation(summary = "로그아웃", description = "Refresh Token을 삭제하고 로그아웃 처리합니다.")
	@PostMapping("/logout")
	public ResponseEntity<String> logout(
			@Parameter(hidden = true) HttpServletRequest request,
			@Parameter(hidden = true) HttpServletResponse response) {
		authService.logout(request, response);
		return ResponseEntity.ok("로그아웃 성공");
	}
}
