package com.ureka.techpost.domain.post.service;

import com.ureka.techpost.domain.comment.repository.CommentRepository;
import com.ureka.techpost.domain.likes.repository.LikesRepository;
import com.ureka.techpost.domain.post.dto.PostResponseDTO;
import com.ureka.techpost.domain.post.entity.Post;
import com.ureka.techpost.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @file PostRedisService.java
 * @author 최승언
 * @version 1.1
 * @since 2025-12-12
 * @description redis에 캐싱하는 비즈니스 로직을 구현한 서비스 클래스입니다.
 */

@Service
@RequiredArgsConstructor
public class PostRedisService {

    private final PostRepository postRepository;
    private final LikesRepository likesRepository;
    private final CommentRepository commentRepository;

    // 객체(DTO) 저장용
    private final RedisTemplate<String, Object> redisTemplate;
    // 숫자(Count) 연산용
    private final StringRedisTemplate stringRedisTemplate;

    private final CacheManager cacheManager;

    private static final String CACHE_POSTS = "posts";
    private static final String CACHE_LIKES = "postLikes";
    private static final String CACHE_COMMENTS = "postComments";
    private static final String RANKING_KEY = "ranking:likes";

    // redis에 검색한 게시물 정보가 없다면 저장
    public void savePostDtoToRedis(PostResponseDTO dbDto) {

        Cache cache = cacheManager.getCache(CACHE_POSTS);

        if (cache != null) {
            cache.put(dbDto.getId(), dbDto);
        }
    }

    // 여러 ID의 DTO를 한 번에 조회
    public List<PostResponseDTO> getPostDtoList(List<Long> postIds) {
        List<String> keys = postIds.stream()
                .map(id -> CACHE_POSTS + "::" + id)
                .toList();

        // MultiGet: 한 번의 통신으로 여러 키 조회
        List<Object> results = redisTemplate.opsForValue().multiGet(keys);

        // 결과를 DTO 리스트로 변환 (없으면 null이 들어있음)
        return results.stream()
                .map(obj -> (PostResponseDTO) obj)
                .collect(Collectors.toList());
    }

    // 키 생성 헬퍼 메서드
    private String getLikeKey(Long postId) {
        return CACHE_LIKES + "::" + postId;
    }
    private String getCommentKey(Long postId) {
        return CACHE_COMMENTS + "::" + postId;
    }

    // 좋아요 수 가져오기
    public Long getLikeCount(Long postId) {
        String key = getLikeKey(postId);
        String value = stringRedisTemplate.opsForValue().get(key);

        // 캐시 히트
        if (value != null) {
            return Long.parseLong(value);
        }

        // 캐시 미스
        Long dbCount = likesRepository.countByPostId(postId);
        stringRedisTemplate.opsForValue().set(key, dbCount.toString());
        return dbCount;
    }

    // 댓글 수 가져오기
    public Long getCommentCount(Long postId) {
        String key = getCommentKey(postId);
        String value = stringRedisTemplate.opsForValue().get(key);

        if (value != null) {
            return Long.parseLong(value);
        }

        Long dbCount = commentRepository.countByPostId(postId);
        stringRedisTemplate.opsForValue().set(key, dbCount.toString());
        return dbCount;
    }

    /**
     * redis 좋아요, 댓글 개수 증감
     */
    public void incrementLikeCount(Long postId) {
        stringRedisTemplate.opsForValue().increment(getLikeKey(postId));
    }
    public void decrementLikeCount(Long postId) {
        stringRedisTemplate.opsForValue().decrement(getLikeKey(postId));
    }
    public void incrementCommentCount(Long postId) {
        stringRedisTemplate.opsForValue().increment(getCommentKey(postId));
    }
    public void decrementCommentCount(Long postId) {
        stringRedisTemplate.opsForValue().decrement(getCommentKey(postId));
    }

    /**
     * 랭킹 관련 점수 증감
     */
    public void incrementLikeRanking(Long postId) {
        // ZSet에 해당 postId의 점수를 1 증가시킴 (없으면 자동 생성)
        redisTemplate.opsForZSet().incrementScore(RANKING_KEY, postId.toString(), 1);
    }
    public void decrementLikeRanking(Long postId) {
        redisTemplate.opsForZSet().incrementScore(RANKING_KEY, postId.toString(), -1);
    }

    // 랭킹에서 삭제
    public void removeRanking(Long postId) {
        redisTemplate.opsForZSet().remove(RANKING_KEY, postId.toString());
    }

    // 인기 게시물 ID 목록 가져오기 (Top N)
    public List<Long> getTopLikedPostIds(int limit) {
        // 점수가 높은 순으로 가져옴
        Set<Object> topPostIds = redisTemplate.opsForZSet().reverseRange(RANKING_KEY, 0, limit - 1);

        if (topPostIds == null || topPostIds.isEmpty()) {
            return Collections.emptyList();
        }

        // Set<Object> -> List<Long> 변환
        return topPostIds.stream()
                .map(id -> Long.parseLong(id.toString()))
                .collect(Collectors.toList());
    }

    // 실시간 크롤링된 게시물 랭킹에 반영
    public void addRankingBatch(List<Post> posts) {
        if (posts == null || posts.isEmpty()) {
            return;
        }
        for (Post post : posts) {
            redisTemplate.opsForZSet().add(RANKING_KEY, post.getId().toString(), 0.0);
        }

    }

    // 랭킹이 비어있을 때 DB 기반으로 초기화하는 메서드
    public void initRankingIfEmpty() {

        if (Boolean.TRUE.equals(redisTemplate.hasKey(RANKING_KEY))) {
            return;
        }

        List<Post> posts = postRepository.findAll();
        for (Post post : posts) {
            redisTemplate.opsForZSet().add(RANKING_KEY, post.getId().toString(), likesRepository.countByPostId(post.getId()));
        }
    }
}
