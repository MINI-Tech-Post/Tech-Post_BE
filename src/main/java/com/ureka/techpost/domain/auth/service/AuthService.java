package com.ureka.techpost.domain.auth.service;

import com.ureka.techpost.domain.auth.dto.LoginDto;
import com.ureka.techpost.domain.auth.dto.SignupDto;
import com.ureka.techpost.domain.auth.jwt.JwtUtil;
import com.ureka.techpost.domain.user.entity.User;
import com.ureka.techpost.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
            throw new RuntimeException("이미 가입되어 있는 회원입니다.");
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

		// 인증된 사용자 이름 추출
		String authenticatedUsername = authentication.getName();

		// 인증된 사용자의 권한(Role) 추출
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
		GrantedAuthority auth = iterator.next();
		String role = auth.getAuthority();

		// JWT 액세스 토큰 및 리프레시 토큰 생성
		String access = jwtUtil.generateAccessToken("access", authenticatedUsername, role);
		String refresh = jwtUtil.generateRefreshToken("refresh");

		// DB에서 사용자 정보 조회 (리프레시 토큰 저장을 위함)
		User user = userRepository.findByUsername(authenticatedUsername)
				.orElseThrow(() -> new UsernameNotFoundException("해당하는 회원을 찾을 수 없습니다."));

		// 새로 발급된 리프레시 토큰을 DB에 저장 (기존 토큰이 있다면 업데이트)
		tokenService.addRefreshToken(user, refresh);

		// 클라이언트 응답 헤더에 액세스 토큰 추가 (Bearer 타입)
		response.setHeader("Authorization", "Bearer " + access);
		// 클라이언트 응답 쿠키에 HttpOnly 리프레시 토큰 추가
		response.addCookie(tokenService.createCookie("refresh", refresh));
		// HTTP 응답 상태를 OK(200)로 설정
		response.setStatus(HttpStatus.OK.value());
	}


}