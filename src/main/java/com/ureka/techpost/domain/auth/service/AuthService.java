package com.ureka.techpost.domain.auth.service;

import com.ureka.techpost.domain.auth.dto.CustomUserDetails;
import com.ureka.techpost.domain.auth.dto.LoginDto;
import com.ureka.techpost.domain.auth.dto.SignupDto;
import com.ureka.techpost.domain.auth.entity.TokenDto;
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
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
        }

        // 없으면 DB에 회원 저장
        User user = signupDto.toEntity(passwordEncoder.encode(signupDto.getPassword()));
        userRepository.save(user);
    }

    public TokenDto login(LoginDto loginDto) {

        // 입력 데이터에서 username, password 꺼냄
        String username = loginDto.getUsername();
        String password = loginDto.getPassword();
        log.info("🔐 [LOGIN] 로그인 요청 - username={}, passwordLength={}",
                username, password != null ? password.length() : 0);

// 로그인을 위한 Spring Security 인증 토큰 생성
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(username, password, null);
        log.debug("🔑 [LOGIN] 인증 토큰 생성 완료 - authToken={}", authToken);

// AuthenticationManager를 통해 사용자 인증 시도
// 인증 성공 시, 사용자 정보(Principal)와 권한(Authorities)을 포함한 Authentication 객체 반환
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(authToken);
            log.info("✅ [LOGIN] 인증 성공 - principal={}, authorities={}",
                    authentication.getPrincipal(), authentication.getAuthorities());
        } catch (Exception e) {
            log.error("❌ [LOGIN] 인증 실패 - username={}, error={}", username, e.getMessage(), e);
            throw e;
        }

// 사용자 추출
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        log.info("👤 [LOGIN] 사용자 정보 로드 완료 - userId={}, username={}, role={}",
                user.getUser().getUserId(),
                user.getUser().getUsername(),
                user.getUser().getRoleName());

// JWT 액세스 토큰 및 리프레시 토큰 생성
        String access = jwtUtil.generateAccessToken(
                "access",
                user.getUsername(),
                user.getUser().getName(),
                user.getUser().getRoleName()
        );
        log.info("🔐 [TOKEN] Access Token 생성 완료 - length={}", access.length());

        String refresh = jwtUtil.generateRefreshToken("refresh");
        log.info("🔄 [TOKEN] Refresh Token 생성 완료 - length={}", refresh.length());

// 새로 발급된 리프레시 토큰을 DB에 저장
        try {
            tokenService.addRefreshToken(user.getUser(), refresh);
            log.info("💾 [TOKEN] Refresh Token DB 저장 성공 - userId={}", user.getUser().getUserId());
        } catch (Exception e) {
            log.error("❌ [TOKEN] Refresh Token DB 저장 실패 - userId={}, error={}",
                    user.getUser().getUserId(), e.getMessage(), e);
            throw e;
        }

        log.info("📤 [LOGIN] TokenDto 반환 완료 - username={}", username);

        return TokenDto.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .build();
    }


    // 토큰 재발급
    public TokenDto reissue(String accessToken, String refreshToken) {

        // Access Token 검증 (형식 확인 등) - 이미 필터나 컨트롤러에서 Bearer 제거 후 넘어왔다고 가정
        if (accessToken == null) {
            throw new CustomException(ErrorCode.ACCESS_TOKEN_MISSING);
        }

        // Refresh 토큰 검증
        tokenService.validateRefreshToken(refreshToken);

        // --- 검증 통과 --- //

        // 기존 토큰에서 username 꺼냄
        String username = jwtUtil.getUsernameFromExpirationToken(accessToken);

        User foundUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 새로운 access/refresh 토큰 생성
        String newAccess = jwtUtil.generateAccessToken("access", username, foundUser.getName(), foundUser.getRoleName());
        String newRefresh = jwtUtil.generateRefreshToken("refresh");

        // 기존 Refresh 토큰 DB에서 삭제 후 새 Refresh 토큰 저장
        // Key가 tokenValue이므로 기존 토큰을 지우고 새 토큰을 저장해야 함
        tokenService.deleteByTokenValue(refreshToken);
        tokenService.addRefreshToken(foundUser, newRefresh);

        return TokenDto.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .build();
    }

    // 로그아웃 처리
    @Transactional
    public void logout(String refreshToken) {
        // 토큰이 존재하면 검증 및 DB 삭제 시도
        if (refreshToken != null) {
            try {
                // 토큰 검증 (만료, 위조, DB 존재 여부 확인)
                tokenService.validateRefreshToken(refreshToken);
                // DB에서 Refresh 토큰 제거
                tokenService.deleteByTokenValue(refreshToken);
            } catch (CustomException e) {
                // 토큰이 유효하지 않거나(만료 등), 이미 DB에 없는 경우
                // 로그아웃 과정이므로 무시
            }
        }
    }
}