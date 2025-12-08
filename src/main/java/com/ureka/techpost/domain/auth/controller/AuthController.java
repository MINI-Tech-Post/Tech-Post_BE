package com.ureka.techpost.domain.auth.controller;

import com.ureka.techpost.domain.auth.dto.SignupDto;
import com.ureka.techpost.domain.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
	public ResponseEntity<String> signup(@RequestBody SignupDto signupDto) {
		authService.signup(signupDto);
		return ResponseEntity.ok("회원가입 성공");
	}

	@PostMapping("/login")
	public ResponseEntity<String> login(@RequestBody LoginDto loginDto, HttpServletResponse response) {
		authService.login(loginDto, response);
		return ResponseEntity.ok("로그인 성공");
	}

}
