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
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostRedisService {

    private final PostRepository postRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LikesRepository likesRepository;
    private final CommentRepository commentRepository;
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

    // 좋아요 수 가져오기
    public Long getLikeCount(Long postId) {

        Cache cache = cacheManager.getCache(CACHE_LIKES);

        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(postId);

            if (wrapper != null) {
                Object value = wrapper.get();

                if (value instanceof Number) {
                    return ((Number) value).longValue();
                }
            }
        }

        Long dbCount = likesRepository.countByPostId(postId);

        if (cache != null) {
            cache.put(postId, dbCount);
        }

        return dbCount;
    }

    // 댓글 수 가져오기
    public Long getCommentCount(Long postId) {

        Cache cache = cacheManager.getCache(CACHE_COMMENTS);

        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(postId);

            if (wrapper != null) {
                Object value = wrapper.get();

                if (value instanceof Number) {
                    return ((Number) value).longValue();
                }
            }
        }

        Long dbCount = commentRepository.countByPostId(postId);

        if (cache != null) {
            cache.put(postId, dbCount);
        }

        return dbCount;
    }

    public void clearCommentCount(Long postId) {
        Cache cache = cacheManager.getCache("postComments");
        if (cache != null) {
            cache.evict(postId); // 해당 게시물의 댓글 수 캐시 삭제
        }
    }

    // 좋아요 점수 올리기 (+1)
    public void incrementLikeRanking(Long postId) {
        // ZSet에 해당 postId의 점수를 1 증가시킴 (없으면 자동 생성)
        redisTemplate.opsForZSet().incrementScore(RANKING_KEY, postId.toString(), 1);
    }

    // 좋아요 점수 내리기 (-1)
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
