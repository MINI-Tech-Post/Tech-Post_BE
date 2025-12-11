package com.ureka.techpost.domain.post.repository;

import com.ureka.techpost.domain.post.dto.PostResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @file PostRepositoryCustom.java
 * @author 최승언
 * @version 1.0
 * @since 2025-12-09
 * @description QueryDSL을 사용한 동적 쿼리 및 검색 기능을 정의하기 위한 커스텀 Repository 인터페이스입니다.
 */

public interface PostRepositoryCustom {
    Page<PostResponseDTO> search(String keyword, String publisher, Pageable pageable);
}
