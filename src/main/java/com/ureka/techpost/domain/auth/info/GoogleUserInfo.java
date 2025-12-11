package com.ureka.techpost.domain.auth.info;

import java.util.Map;

/**
 * @file GoogleUserInfo.java
 @author 김동혁, 구본문
 @version 1.0
 @since 2025-12-09
 @description 이 파일은 구글 사용자 정보를 추출하기 위한 구현체 클래스입니다.
 */
public class GoogleUserInfo implements OAuth2UserInfo {

    private final Map<String, Object> attributes;

    public GoogleUserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }
}
