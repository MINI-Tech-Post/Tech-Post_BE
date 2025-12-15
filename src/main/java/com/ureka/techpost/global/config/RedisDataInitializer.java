package com.ureka.techpost.global.config;

import com.ureka.techpost.domain.post.service.PostRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisDataInitializer implements CommandLineRunner {

    private final PostRedisService postRedisService;

    @Override
    public void run(String... args) {
        log.info("Started Redis Ranking Initialization...");
        try {
            postRedisService.initRankingIfEmpty();
            log.info("Redis Ranking Initialized Successfully!");
        } catch (Exception e) {
            log.error("Redis Initialization Failed: {}", e.getMessage());
        }
    }
}
