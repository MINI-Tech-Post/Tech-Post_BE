package com.ureka.techpost.domain.auth.info;

import java.util.Map;

/**
 * @file OAuth2UserInfo.java
 @author 김동혁, 구본문
 @version 1.0
 @since 2025-12-09
 @description 이 파일은 소셜 사용자 정보를 공통된 형식으로 사용하기 위한 클래스입니다.
 */
public interface OAuth2UserInfo {
    // 제공자로부터 받은 원본 사용자 정보
    Map<String, Object> getAttributes();
    // 제공자의 고유 식별 ID
    String getProviderId();
    // 제공자 이름 (google, naver, kakao)
    String getProvider();
    // 사용자 이메일
    String getEmail();
    // 사용자 이름
    String getName();
}
