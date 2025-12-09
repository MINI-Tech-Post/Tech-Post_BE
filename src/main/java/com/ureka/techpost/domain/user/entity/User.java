package com.ureka.techpost.domain.user.entity;

import com.ureka.techpost.domain.user.enums.Role;
import com.ureka.techpost.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * @file User.java
 @author 김동혁, 구본문
 @version 1.0
 @since 2025-12-08
 @description 이 파일은 사용자 정보를 담는 Entity 클래스입니다.
 */
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private Long userId;

	@Column(unique = true)
	private String username;

	private String password; // 소셜 로그인은 null 가능

	@Column(nullable = false)
	private String name;

	// 일반 로그인은 provider="NONE", providerId=null 로 설정
	private String provider; // google, kakao, naver

	@Column(name = "provider_id")
	private String providerId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	public String getRoleName() {
		return this.role.name();
	}
}
