package com.ureka.techpost.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @file AppConfig.java
 @author 김동혁
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 비밀번호 암호화를 위한 클래스입니다.
 */
@Configuration
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
