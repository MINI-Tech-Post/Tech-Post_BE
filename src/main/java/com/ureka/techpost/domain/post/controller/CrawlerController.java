package com.ureka.techpost.domain.post.controller;

import com.ureka.techpost.domain.post.service.CrawlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @file CrawlerController.java
 @author 유효주
 @version 1.0
 @since 2025-12-11
 @description 이 파일은 크롤러를 실행하는 컨트롤러 클래스입니다. 스케쥴링 클래스가 실행되지 않는다면 이 컨트롤러를 통해 수동으로 크롤러를 실행할 수 있습니다.
 */

@Slf4j
@RestController
@RequestMapping("/crawl")
@RequiredArgsConstructor
public class CrawlerController {

    private final CrawlService crawlService;

    /**
     * 모든 블로그 크롤링 수동 실행
     * GET /api/crawl/all
     */
    @PostMapping("/all")
    public ResponseEntity<String> crawlAll() {
        log.info("수동 크롤링 요청 - 전체");
        try {
            crawlService.crawlAll();
            return ResponseEntity.ok("전체 크롤링이 완료되었습니다.");
        } catch (Exception e) {
            log.error("크롤링 실패", e);
            return ResponseEntity.internalServerError()
                    .body("크롤링 실패: " + e.getMessage());
        }
    }

    /**
     * 특정 출처만 크롤링
     * POST /api/crawl/source?name=카카오 기술 블로그
     */
    @PostMapping("/source")
    public ResponseEntity<String> crawlBySource(@RequestParam String name) {
        log.info("수동 크롤링 요청 - 출처: {}", name);
        try {
            crawlService.crawlBySource(name);
            return ResponseEntity.ok(name + " 크롤링이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("크롤링 실패", e);
            return ResponseEntity.internalServerError()
                    .body("크롤링 실패: " + e.getMessage());
        }
    }

    /**
     * 사용 가능한 크롤러 목록 조회
     * GET /api/crawl/sources
     */
    @GetMapping("/sources")
    public ResponseEntity<List<String>> getAvailableSources() {
        List<String> sources = crawlService.getAvailableSources();
        return ResponseEntity.ok(sources);
    }
}