package com.ureka.techpost.domain.auth.service;

import com.ureka.techpost.domain.auth.dto.CustomUserDetails;
import com.ureka.techpost.domain.auth.info.GoogleUserInfo;
import com.ureka.techpost.domain.auth.info.KakaoUserInfo;
import com.ureka.techpost.domain.auth.info.NaverUserInfo;
import com.ureka.techpost.domain.auth.info.OAuth2UserInfo;
import com.ureka.techpost.domain.user.entity.User;
import com.ureka.techpost.domain.user.enums.Role;
import com.ureka.techpost.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @file CustomOAuth2UserService.java
 @author 김동혁, 구본문
 @version 1.0
 @since 2025-12-09
 @description 이 파일은 소셜 사용자 정보를 DB에 위한 클래스입니다.
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 기본 OAuth2UserService를 통해 사용자 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        // 사용자의 소셜 서비스 제공자(provider) 이름 가져오기 (google, naver, kakao 등)
        String provider = userRequest.getClientRegistration().getRegistrationId();

        // 제공자에 따라 적절한 OAuth2UserInfo 구현체 선택
		OAuth2UserInfo oAuth2UserInfo = getOAuth2UserInfo(provider, oAuth2User);

		// DB에서 소셜 로그인 정보로 해당 사용자를 조회
		User existingUser = userRepository.findByProviderAndProviderId(provider, oAuth2UserInfo.getProviderId()).orElse(null);

        User user;
        if (existingUser != null) {
            // 이미 가입된 사용자인 경우, 기존 정보를 그대로 사용
            user = existingUser;
        } else {
			// 소셜 정보로 가입된 적이 없는 경우 -> 신규 회원가입 진행
			String email = oAuth2UserInfo.getEmail();
			// username을 "provider_이메일" 형식으로 생성하여 유니크성 보장 (이메일 없으면 providerId 사용)
			String username = (email != null && !email.isEmpty()) ? provider + "_" + email : provider + "_" + oAuth2UserInfo.getProviderId();

			// 신규 사용자인 경우, 자동 회원가입 진행
            String randomPassword = passwordEncoder.encode(UUID.randomUUID().toString());
            user = User.builder()
                    .username(username)
                    .name(oAuth2UserInfo.getName())
                    .password(randomPassword) // 소셜 로그인이므로 비밀번호는 임의의 값으로 설정
                    .role(Role.ROLE_USER)
                    .provider(oAuth2UserInfo.getProvider())
                    .providerId(oAuth2UserInfo.getProviderId())
                    .build();
            userRepository.save(user);
        }

        // 우리 시스템에서 사용할 CustomUserDetails 객체로 변환하여 반환
        return new CustomUserDetails(user, oAuth2User.getAttributes());
    }

	private static OAuth2UserInfo getOAuth2UserInfo(String provider, OAuth2User oAuth2User) {
		OAuth2UserInfo oAuth2UserInfo;
		if (provider.equals("google")) {
			oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
		} else if (provider.equals("naver")) {
			oAuth2UserInfo = new NaverUserInfo(oAuth2User.getAttributes());
		} else if (provider.equals("kakao")) {
			oAuth2UserInfo = new KakaoUserInfo(oAuth2User.getAttributes());
		} else {
			// 지원하지 않는 제공자일 경우 예외 처리
			throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다.");
		}
		return oAuth2UserInfo;
	}
}
