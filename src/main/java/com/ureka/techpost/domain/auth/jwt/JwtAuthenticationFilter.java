package com.ureka.techpost.domain.auth.jwt;

import com.ureka.techpost.domain.auth.dto.CustomUserDetails;
import com.ureka.techpost.domain.user.entity.User;
import com.ureka.techpost.domain.user.repository.UserRepository;
//import com.ureka.techpost.domain.auth.service.TokenService;
import com.ureka.techpost.global.exception.CustomException;
import com.ureka.techpost.global.exception.ErrorCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
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

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        // reissue 요청은 헤더에 access 토큰이 아닌 refresh 토큰이 필요하기 때문에,
        // JwtAuthenticationFilter의 검증 로직을 건너뛰어야 함
        return requestURI.equals("/api/auth/reissue");
    }



    @Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		// 토큰 추출
		String accessToken = resolveToken(request);

		// 토큰이 없으면 다음 필터로 진행
		if (accessToken == null) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			validateToken(accessToken);
			authenticateUser(accessToken);
		} catch (CustomException e) {
			request.setAttribute("exception", e.getErrorCode());
		} catch (SignatureException | MalformedJwtException e) {
			request.setAttribute("exception", ErrorCode.INVALID_TOKEN);
		} catch (ExpiredJwtException e) {
			request.setAttribute("exception", ErrorCode.ACCESS_TOKEN_EXPIRED);
		} catch (Exception e) {
			log.error("Unhandled exception in JwtFilter", e);
			request.setAttribute("exception", ErrorCode.INVALID_TOKEN);
		}

		// 다음 필터로 진행
		filterChain.doFilter(request, response);
	}

	private String resolveToken(HttpServletRequest request) {
		String authorization = request.getHeader("Authorization");
		if (authorization != null && authorization.startsWith("Bearer ")) {
			return authorization.split(" ")[1];
		}
		return null;
	}

	private void validateToken(String accessToken) throws IOException {
		// 토큰 만료 여부 확인
		if (jwtUtil.isExpired(accessToken)) {
			throw new CustomException(ErrorCode.ACCESS_TOKEN_MISSING);
		}

		// 토큰 카테고리 확인
		String category = jwtUtil.getCategory(accessToken);
		if (!"access".equals(category)) {
			throw new CustomException(ErrorCode.INVALID_TOKEN_CATEGORY);
		}
	}

	private void authenticateUser(String accessToken) {
		// 토큰에서 username 추출
		String username = jwtUtil.getUsernameFromToken(accessToken);

		// DB에서 사용자 조회
		User foundUser = userRepository.findByUsername(username)
				.orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

		// UserDetails 객체 생성
		CustomUserDetails customUserDetails = new CustomUserDetails(foundUser);

		// 인증 토큰 생성
		Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());

		// SecurityContext에 설정
		SecurityContextHolder.getContext().setAuthentication(authToken);

	}
}
