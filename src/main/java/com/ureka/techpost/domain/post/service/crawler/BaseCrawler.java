package com.ureka.techpost.domain.post.service.crawler;


import com.ureka.techpost.domain.post.entity.Post;

import java.util.List;

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