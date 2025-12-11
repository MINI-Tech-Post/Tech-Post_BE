package com.ureka.techpost.domain.auth.info;

import java.util.Map;

/**
 * @file NaverUserInfo.java
 @author 김동혁, 구본문
 @version 1.0
 @since 2025-12-09
 @description 이 파일은 네이버 사용자 정보를 추출하기 위한 구현체 클래스입니다.
 */
public class NaverUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;
    private final Map<String, Object> responseAttributes;

    @SuppressWarnings("unchecked")
    public NaverUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
        // Naver 응답의 실제 사용자 정보는 "response" 키 값에 Map 형태로 들어있음
        this.responseAttributes = (Map<String, Object>) attributes.get("response");
    }

    @Override
    public Map<String, Object> getAttributes() {
        return responseAttributes; // 실제 속성 맵 반환
    }

    @Override
    public String getProviderId() {
        return (String) responseAttributes.get("id");
    }

    @Override
    public String getProvider() {
        return "naver";
    }

    @Override
    public String getEmail() {
        return (String) responseAttributes.get("email");
    }

    @Override
    public String getName() {
        return (String) responseAttributes.get("name");
    }
}
