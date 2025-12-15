package com.ureka.techpost.domain.likes.service;

import com.ureka.techpost.domain.auth.dto.CustomUserDetails;
import com.ureka.techpost.domain.likes.entity.Likes;
import com.ureka.techpost.domain.likes.repository.LikesRepository;
import com.ureka.techpost.domain.post.entity.Post;
import com.ureka.techpost.domain.post.repository.PostRepository;
import com.ureka.techpost.domain.post.service.PostRedisService;
import com.ureka.techpost.domain.user.entity.User;
import com.ureka.techpost.domain.user.repository.UserRepository;
import com.ureka.techpost.global.exception.CustomException;
import com.ureka.techpost.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikesService {

    private final LikesRepository likesRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostRedisService postRedisService;

    @Transactional
    @CacheEvict(value = "postLikes", key = "#postId")
    public void createLike(Long postId, CustomUserDetails userDetails) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if(likesRepository.existsByUserAndPost(user, post)){
            throw new CustomException(ErrorCode.ALREADY_LIKED);
        }

        Likes likes = Likes.builder()
                .user(user)
                .post(post)
                .build();

        likesRepository.save(likes);

        // 랭킹 점수 실시간 반영 (ZSet)
        postRedisService.incrementLikeRanking(postId);
    }

    @Transactional
    @CacheEvict(value = "postLikes", key = "#postId")
    public void deleteLike(Long postId, CustomUserDetails userDetails) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Likes likes = likesRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> new CustomException(ErrorCode.LIKE_NOT_FOUND));

        if(!likes.getUser().getUserId().equals(user.getUserId())){
            throw  new CustomException(ErrorCode.USER_NOT_MATCH);
        }

        likesRepository.delete(likes);

        // 랭킹 점수 차감 (ZSet)
        postRedisService.decrementLikeRanking(postId);
    }
}
