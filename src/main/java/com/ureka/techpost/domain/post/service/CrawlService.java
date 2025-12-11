package com.ureka.techpost.domain.post.service;

import com.ureka.techpost.domain.post.entity.Post;
import com.ureka.techpost.domain.post.repository.PostRepository;
import com.ureka.techpost.domain.post.service.crawler.BaseCrawler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @file CrawalService.java
 @author 유효주
 @version 1.0
 @since 2025-12-11
 @description 이 파일은 크롤링을 실행하는 서비스 클래스입니다.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlService {

    private final List<BaseCrawler> crawlers;
    private final PostRepository postRepository;

    /**
     * 모든 크롤러를 실행하여 새로운 게시글 수집
     */
    @Transactional
    public void crawlAll() {
        log.info("전체 크롤링 시작 - 크롤러 개수: {}", crawlers.size());

        int totalSaved = 0;

        for (BaseCrawler crawler : crawlers) {
            try {
                List<Post> posts = crawler.crawl();
                int saved = savePosts(posts);
                totalSaved += saved;

                log.info("{} 크롤링 완료 - 저장된 게시글: {}", crawler.getSourceName(), saved);

            } catch (Exception e) {
                log.error("{} 크롤링 중 오류 발생", crawler.getSourceName(), e);
            }
        }

        log.info("전체 크롤링 완료 - 총 저장된 게시글: {}", totalSaved);
    }

    /**
     * 특정 출처의 크롤링만 실행
     */
    @Transactional
    public void crawlBySource(String sourceName) {
        log.info("특정 출처 크롤링 시작: {}", sourceName);

        BaseCrawler targetCrawler = crawlers.stream()
                .filter(crawler -> crawler.getSourceName().equals(sourceName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 출처: " + sourceName));

        List<Post> posts = targetCrawler.crawl();
        int saved = savePosts(posts);

        log.info("{} 크롤링 완료 - 저장된 게시글: {}", sourceName, saved);
    }

    /**
     * 게시글 리스트를 DB에 저장 (중복 체크는 크롤러에서 이미 처리됨)
     */
    private int savePosts(List<Post> posts) {
        if (posts.isEmpty()) {
            return 0;
        }

        postRepository.saveAll(posts);
        return posts.size();
    }

    /**
     * 사용 가능한 모든 크롤러 출처명 반환
     */
    public List<String> getAvailableSources() {
        return crawlers.stream()
                .map(BaseCrawler::getSourceName)
                .toList();
    }
}