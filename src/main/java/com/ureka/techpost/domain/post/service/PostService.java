package com.ureka.techpost.domain.post.service;

import com.ureka.techpost.domain.auth.dto.CustomUserDetails;
import com.ureka.techpost.domain.likes.entity.Likes;
import com.ureka.techpost.domain.likes.repository.LikesRepository;
import com.ureka.techpost.domain.post.dto.PostResponseDTO;
import com.ureka.techpost.domain.post.dto.PostRequestDTO;
import com.ureka.techpost.domain.post.entity.Post;
import com.ureka.techpost.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * @file PostService.java
 * @author 최승언
 * @version 1.0
 * @since 2025-12-09
 * @description 게시글 등록, 조회, 검색, 삭제 등 게시글 도메인의 핵심 비즈니스 로직을 처리하는 서비스 클래스입니다.
 */

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final LikesRepository likesRepository;

    public PostResponseDTO findById(Long id, CustomUserDetails userDetails) {
        PostResponseDTO dto = postRepository.findPostById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글 없음"));

        if (userDetails != null) {
            Long userId = userDetails.getUser().getUserId();
            boolean liked = likesRepository.existsByUserUserIdAndPostId(userId, id);
            dto.setIsLiked(liked);
        } else {
            dto.setIsLiked(false);
        }

        return dto;
    }

    public void save(PostRequestDTO postRequestDTO, CustomUserDetails userDetails) {

        if(!userDetails.getUser().getRoleName().equals("ROLE_ADMIN")){
            throw new IllegalArgumentException("권한이 없음");
        }

        if(postRepository.existsByOriginalUrl(postRequestDTO.getOriginalUrl())){
            throw new IllegalArgumentException("이미 존재하는 게시글");
        }

        postRepository.save(Post.builder()
                        .title(postRequestDTO.getTitle())
                        .summary(postRequestDTO.getSummary())
                        .originalUrl(postRequestDTO.getOriginalUrl())
                        .publisher(postRequestDTO.getPublisher())
                        .publishedAt(postRequestDTO.getPublishedAt())
                        .sourceName(postRequestDTO.getSourceName())
                        .thumbnailUrl(postRequestDTO.getThumbnailUrl())
                        .build());
    }

    public Page<PostResponseDTO> search(String keyword, String publisher, Pageable pageable){
        return postRepository.search(keyword, publisher, pageable);
    }

    public void deletePost(Long postId, CustomUserDetails userDetails) {

        if(!userDetails.getUser().getRoleName().equals("ROLE_ADMIN")){
            throw new IllegalArgumentException("권한이 없음");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물"));

        postRepository.delete(post);
    }
}
