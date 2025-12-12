package com.ureka.techpost.domain.post.service;

import com.ureka.techpost.domain.post.dto.PostResponseDTO;
import com.ureka.techpost.domain.post.repository.PostRepository;
import com.ureka.techpost.global.exception.CustomException;
import com.ureka.techpost.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostCacheService {
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "posts", key = "#postId", cacheManager = "redisCacheManager")
    public PostResponseDTO getPostBaseDto(Long postId) {
        return postRepository.findPostById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }
}
