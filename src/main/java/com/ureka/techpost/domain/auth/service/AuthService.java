package com.ureka.techpost.domain.auth.service;

import com.ureka.techpost.domain.auth.dto.CustomUserDetails;
import com.ureka.techpost.domain.auth.dto.LoginDto;
import com.ureka.techpost.domain.auth.dto.SignupDto;
import com.ureka.techpost.domain.auth.jwt.JwtUtil;
import com.ureka.techpost.domain.user.entity.User;
import com.ureka.techpost.domain.user.repository.UserRepository;
import com.ureka.techpost.global.exception.CustomException;
import com.ureka.techpost.global.exception.ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Iterator;

/**
 * @file AuthController.java
 @author 김동혁, 구본문
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 사용자 인증 관련 로직을 수행하는 클래스입니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	private final TokenService tokenService;
	private final AuthenticationManager authenticationManager;

    // 회원가입
    @Transactional
    public void signup(SignupDto signupDto) {
        // DB에 입력한 username이 존재하는지 확인
        if (userRepository.existsByUsername(signupDto.getUsername())) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
        }

        // 없으면 DB에 회원 저장
        User user = signupDto.toEntity(passwordEncoder.encode(signupDto.getPassword()));
        userRepository.save(user);
    }

	public void login(LoginDto loginDto, HttpServletResponse response) {
		// 입력 데이터에서 username, password 꺼냄
		String username = loginDto.getUsername();
		String password = loginDto.getPassword();

		// 로그인을 위한 Spring Security 인증 토큰 생성
		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);

		// AuthenticationManager를 통해 사용자 인증 시도
		// 인증 성공 시, 사용자 정보(Principal)와 권한(Authorities)을 포함한 Authentication 객체 반환
		Authentication authentication = authenticationManager.authenticate(authToken);

		// 사용자 추출
		CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

		// JWT 액세스 토큰 및 리프레시 토큰 생성
		String access = jwtUtil.generateAccessToken("access", user.getUsername(), user.getUser().getName(), user.getUser().getRoleName());
		String refresh = jwtUtil.generateRefreshToken("refresh");

		// 새로 발급된 리프레시 토큰을 DB에 저장 (기존 토큰이 있다면 업데이트)
		tokenService.addRefreshToken(user.getUser(), refresh);

		// 클라이언트 응답 헤더에 액세스 토큰 추가 (Bearer 타입)
		response.setHeader("Authorization", "Bearer " + access);
		// 클라이언트 응답 쿠키에 HttpOnly 리프레시 토큰 추가
		response.addCookie(tokenService.createCookie("refresh", refresh));
		// HTTP 응답 상태를 OK(200)로 설정
		response.setStatus(HttpStatus.OK.value());
	}

	// 토큰 재발급
	public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {

		String authorization = request.getHeader("Authorization");
		// Access Token 검증
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			throw new CustomException(ErrorCode.ACCESS_TOKEN_MISSING);
		}
		String accessToken = authorization.split(" ")[1];

		String refresh = getRefreshTokenFromCookie(request);

		tokenService.validateRefreshToken(refresh);

		// --- 검증 통과 --- //

		// 기존 토큰에서 username 꺼냄
		String username = jwtUtil.getUsernameFromExpirationToken(accessToken);

		User foundUser = userRepository.findByUsername(username)
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		// 새로운 access/refresh 토큰 생성
		String newAccess = jwtUtil.generateAccessToken("access", username, foundUser.getName(), foundUser.getRoleName());
		String newRefresh = jwtUtil.generateRefreshToken("refresh");

		// 기존 Refresh 토큰 DB에서 삭제 후 새 Refresh 토큰 저장
		tokenService.deleteByTokenValue(refresh);
		tokenService.addRefreshToken(foundUser, newRefresh);

		// 응답 설정
		response.setHeader("Authorization", "Bearer " + newAccess);
		response.addCookie(tokenService.createCookie("refresh", newRefresh));

		return new ResponseEntity<>(HttpStatus.OK);
	}

	// 로그아웃 처리
	public void logout(HttpServletRequest request, HttpServletResponse response) {
		String refresh = getRefreshTokenFromCookie(request);

		// 토큰이 존재하면 검증 및 DB 삭제 시도
		if (refresh != null) {
			try {
				// 토큰 검증 (만료, 위조, DB 존재 여부 확인)
				tokenService.validateRefreshToken(refresh);
				// DB에서 Refresh 토큰 제거
				tokenService.deleteByTokenValue(refresh);
			} catch (CustomException e) {
				// 토큰이 유효하지 않거나(만료 등), 이미 DB에 없는 경우
				// 로그아웃 과정이므로 무시하고 쿠키 삭제로 넘어감
			}
		}

		// response에서 쿠키 제거 (항상 수행하여 클라이언트 상태 정리)
		Cookie cookie = new Cookie("refresh", null);
		cookie.setMaxAge(0);
		cookie.setPath("/");
		response.addCookie(cookie);
	}

	private static String getRefreshTokenFromCookie(HttpServletRequest request) {
		// Refresh 토큰 검증
		String refresh = null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("refresh")) {
					refresh = cookie.getValue();
					break;
				}
			}
		}
		return refresh;
	}
}