package com.ureka.techpost.domain.post.scheduler;

import com.ureka.techpost.domain.post.service.CrawlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @file CrawlScheduler.java
 @author 유효주
 @version 1.0
 @since 2025-12-11
 @description 이 파일은 오전 9시마다 모든 사이트를 크롤링하는 스케쥴러 클래스입니다.
 */


@Slf4j
@Component
@RequiredArgsConstructor
public class CrawlScheduler {

    private final CrawlService crawlService;

    /**
     * 매일 오전 9시에 전체 크롤링 실행
     */
    @Scheduled(cron = "0 0 13 * * ?")
    public void dailyCrawl() {
        log.info("일일 스케줄링 크롤링 시작");
        try {
            crawlService.crawlAll();
            log.info("일일 스케줄링 크롤링 완료");
        } catch (Exception e) {
            log.error("일일 스케줄링 크롤링 실패", e);
        }
    }
}