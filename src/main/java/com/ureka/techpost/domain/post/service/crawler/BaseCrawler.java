package com.ureka.techpost.domain.post.service.crawler;


import com.ureka.techpost.domain.post.entity.Post;

import java.util.List;

/**
 * @file BaseCrawler.java
 @author 유효주
 @version 1.0
 @since 2025-12-11
 @description 이 파일은 크롤링하는 기능을 정의하는 인터페이스입니다.
 */

public interface BaseCrawler {

    /**
     * @return 크롤링된 게시글 리스트
     */
    List<Post> crawl();

    /**
     * 크롤러의 출처명 반환
     * @return 출처명 (예: "카카오 기술 블로그")
     */
    String getSourceName();
}