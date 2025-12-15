package com.ureka.techpost.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    private ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Java 8 날짜(LocalDateTime) 모듈 등록
        objectMapper.registerModule(new JavaTimeModule());

        // 날짜를 [2024, 12, 12, 10, 0, 0] 배열 대신 "2024-12-12T10:00:00" 문자열로 저장
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 클래스 타입 정보를 JSON에 포함
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        return objectMapper;
    }

    // Key는 String, Value는 JSON
    @Bean
    public RedisTemplate<String, Object> redisTemplate (RedisConnectionFactory redisConnectionFactory){
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(createObjectMapper());

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(serializer);
        return redisTemplate;
    }

    // RedisCacheManager 커스텀 설정
    // Key는 String, Value는 JSON
    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(createObjectMapper());

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .entryTtl(Duration.ofMinutes(10)); // 기본 캐시 유효시간 10분

        // 좋아요, 댓글은 1시간
        Map<String, RedisCacheConfiguration> customConfigs = new HashMap<>();
        customConfigs.put("postLikes", defaultConfig.entryTtl(Duration.ofHours(1)));
        customConfigs.put("postComments", defaultConfig.entryTtl(Duration.ofHours(1)));

        return RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(connectionFactory)
                .cacheDefaults(defaultConfig) // 기본 설정 적용
                .withInitialCacheConfigurations(customConfigs) // 커스텀 설정 적용
                .build();
    }
}
