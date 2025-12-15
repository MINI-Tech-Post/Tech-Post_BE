package com.ureka.techpost.domain.post.service;

import com.ureka.techpost.domain.auth.dto.CustomUserDetails;
import com.ureka.techpost.domain.likes.entity.Likes;
import com.ureka.techpost.domain.likes.repository.LikesRepository;
import com.ureka.techpost.domain.post.dto.PostResponseDTO;
import com.ureka.techpost.domain.post.dto.PostRequestDTO;
import com.ureka.techpost.domain.post.entity.Post;
import com.ureka.techpost.domain.post.repository.PostRepository;
import com.ureka.techpost.global.exception.CustomException;
import com.ureka.techpost.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @file PostService.java
 * @author 최승언
 * @version 1.1
 * @since 2025-12-09
 * @description 게시글 등록, 조회, 검색, 삭제 등 게시글 도메인의 핵심 비즈니스 로직을 처리하는 서비스 클래스입니다.
 */

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostRedisService postRedisService;
    private final LikesRepository likesRepository;
    private final PostCacheService postCacheService;

    public PostResponseDTO findById(Long id, CustomUserDetails userDetails) {

        // 게시글 기본 정보 가져오기
        // 캐시 히트 -> redis에서 정보 반환
        // 캐시 미스 -> DB에서 정보 반환
        PostResponseDTO dto = postCacheService.getPostBaseDto(id);

        // Redis에 있으면 쓰고, 없으면 DB에서 가져와서 캐싱
        dto.setLikeCount(postRedisService.getLikeCount(id));
        dto.setCommentCount(postRedisService.getCommentCount(id));

        if (userDetails != null) {
            Long userId = userDetails.getUser().getUserId();
            boolean liked = likesRepository.existsByUserUserIdAndPostId(userId, id);
            dto.setIsLiked(liked);
        } else {
            dto.setIsLiked(false);
        }

        return dto;
    }

    public Page<PostResponseDTO> search(String keyword, String publisher, Pageable pageable){

        // DB에서 검색 조건에 맞는 ID 리스트만 조회
        Page<Long> idPage = postRepository.searchIds(keyword, publisher, pageable);
        List<Long> ids = idPage.getContent();

        if (ids.isEmpty()) {
            return Page.empty(pageable);
        }

        // redis에 개싱된 데이터 조회
        List<PostResponseDTO> cachedDtos = postRedisService.getPostDtoList(ids);

        // redis에 없는 ID만 선별
        List<Long> missingIds = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            if (cachedDtos.get(i) == null) {
                missingIds.add(ids.get(i));
            }
        }

        // 캐시에 없는 데이터만 DB에서 상세 조회
        if (!missingIds.isEmpty()) {
            List<Post> missingPosts = postRepository.findAllById(missingIds);
            // 조회 후 DTO 변환 및 Redis 저장
            missingPosts.forEach(post -> {
                PostResponseDTO dto = new PostResponseDTO(
                        post.getId(),
                        post.getTitle(),
                        post.getSummary(),
                        post.getOriginalUrl(),
                        post.getThumbnailUrl(),
                        post.getPublisher(),
                        post.getPublishedAt(),
                        post.getSourceName(),
                        post.getCreatedAt(),
                        0L,0L
                        );

                postRedisService.savePostDtoToRedis(dto); // Redis에 채워넣기

                // cachedDtos 리스트의 null이었던 곳에 채워넣기
                int index = ids.indexOf(post.getId());
                if (index != -1) {
                    cachedDtos.set(index, dto);
                }
            });
        }

        // 실시간 좋아요/댓글 최신화
        cachedDtos.forEach(dto -> {
            if (dto != null) {
                dto.setCommentCount(postRedisService.getCommentCount(dto.getId()));
                dto.setLikeCount(postRedisService.getLikeCount(dto.getId()));
            }
        });

        // Page 객체로 다시 포장해서 반환
        return new PageImpl<>(cachedDtos, pageable, idPage.getTotalElements());
    }

    public List<PostResponseDTO> getPopularPosts() {
        List<Long> topIds = postRedisService.getTopLikedPostIds(10);
        List<PostResponseDTO> result = new ArrayList<>();

        for (Long id : topIds) {
            try {
                result.add(this.findById(id, null));
            } catch (CustomException e) {
                postRedisService.removeRanking(id);
            }
        }

        return result;
    }

    public void save(PostRequestDTO postRequestDTO, CustomUserDetails userDetails) {

        if(!userDetails.getUser().getRoleName().equals("ROLE_ADMIN")){
            throw new CustomException(ErrorCode.USER_NOT_MATCH);
        }

        if(postRepository.existsByOriginalUrl(postRequestDTO.getOriginalUrl())){
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
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

    public void deletePost(Long postId, CustomUserDetails userDetails) {

        if(!userDetails.getUser().getRoleName().equals("ROLE_ADMIN")){
            throw new CustomException(ErrorCode.USER_NOT_MATCH);
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        postRepository.delete(post);
    }

}
