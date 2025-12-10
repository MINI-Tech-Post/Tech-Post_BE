package com.ureka.techpost.domain.auth.jwt;

import com.ureka.techpost.domain.auth.dto.CustomUserDetails;
import com.ureka.techpost.domain.user.entity.User;
import com.ureka.techpost.domain.user.repository.UserRepository;
import com.ureka.techpost.domain.auth.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * @file JwtAuthenticationFilter.java
 @author 김동혁, 구본문
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 모든 API 요청이 올 때마다 가장 먼저 실행되어 토큰 검사하는 클래스입니다.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        // reissue 요청은 헤더에 access 토큰이 아닌 refresh 토큰이 필요하기 때문에,
        // JwtAuthenticationFilter의 검증 로직을 건너뛰어야 함
        return requestURI.equals("/api/auth/reissue");
    }

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("[JwtAuthFilter] doFilterInternal");
        
        // 요청 헤더에서 Authorization 키의 값(토큰) 추출
		String authorization = request.getHeader("Authorization");

        // 토큰이 없거나, Bearer 타입이 아니면 필터 통과 (인증 실패 처리됨)
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.warn("JWT 토큰 없음");
            filterChain.doFilter(request, response);
            return;
        }

        // "Bearer " 접두사를 제거하고 순수 토큰 값만 추출
        String accessToken = authorization.split(" ")[1];

        // 토큰 유효성 검증 (만료 여부, 위조 여부 등 확인)
        // 유효하지 않으면 예외가 발생하여 GlobalExceptionHandler가 처리
        tokenService.validateAccessToken(accessToken);

        // 토큰에서 사용자 이름(username) 추출
        String username = jwtUtil.getUsernameFromToken(accessToken);

        // 추출한 username으로 DB에서 실제 사용자 정보 조회
        // (토큰에는 비밀번호 같은 민감한 정보가 없으므로 DB 조회가 필요할 수 있음)
        User foundUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("회원이 존재하지 않습니다."));

        // 인증 객체(Authentication) 생성을 위한 임시 User 객체 생성
        // 비밀번호는 이미 토큰 검증을 통과했으므로 임의의 값으로 설정
        User user = User.builder()
				.userId(foundUser.getUserId())
                .username(username)
                .password("temppassword")
                .name(foundUser.getName())
                .role(foundUser.getRole())
                .provider("NONE")
                .providerId(null)
                .build();

        // UserDetails 객체 생성 (Spring Security가 사용하는 사용자 정보 객체)
        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        // 스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

        // 세션(Security Context)에 인증 정보 등록
        // 이 요청이 끝날 때까지만 인증된 상태로 유지됨 (Stateless)
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 다음 필터로 진행
        filterChain.doFilter(request, response);
	}
}
