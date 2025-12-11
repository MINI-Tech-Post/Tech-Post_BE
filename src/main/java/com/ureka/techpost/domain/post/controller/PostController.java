package com.ureka.techpost.domain.post.controller;

import com.ureka.techpost.domain.auth.dto.CustomUserDetails;
import com.ureka.techpost.domain.post.dto.PostResponseDTO;
import com.ureka.techpost.domain.post.dto.PostRequestDTO;
import com.ureka.techpost.domain.post.service.PostService;
import com.ureka.techpost.global.apiPayload.ApiResponse;
import com.ureka.techpost.global.apiPayload.code.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * @file PostController.java
 @author 최승언
 @version 1.0
 @since 2025-12-09
 @description 게시글 관련 API 요청(생성, 조회, 검색, 삭제)을 받아 서비스 계층으로 전달하고 응답을 반환하는 컨트롤러 클래스입니다.
 */

@Tag(name = "게시글(Post) API", description = "게시글 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 등록", description = "제목, 내용, 링크 등을 받아 게시글을 등록합니다.")
    @PostMapping("")
    public ApiResponse<Void> createPost(@RequestBody PostRequestDTO postRequestDTO,
                                          @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails){

        postService.save(postRequestDTO, userDetails);

        return ApiResponse.of(SuccessStatus._CREATED, null);
    }

    @Operation(summary = "게시글 목록 조회/검색", description = "키워드와 출처로 검색하거나 전체 목록을 페이징하여 조회합니다.")
    @GetMapping("")
    public  ApiResponse<Page<PostResponseDTO>> searchPosts(
            @Parameter(description = "검색할 키워드 (제목/요약)") @RequestParam(required = false) String keyword,
            @Parameter(description = "출처 필터링 (예: Velog)") @RequestParam(required = false) String publisher,
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)Pageable pageable
    ){
        return ApiResponse.onSuccess(postService.search(keyword, publisher, pageable));
    }

    @Operation(summary = "게시글 상세 조회", description = "게시글 ID(PK)를 이용하여 특정 게시글의 상세 정보를 조회합니다.")
    @GetMapping("/{postId}")
    public ApiResponse<PostResponseDTO> getPost(@Parameter(description = "조회할 게시글의 ID") @PathVariable Long postId){

        return ApiResponse.onSuccess(postService.findById(postId));
    }

    @Operation(summary = "게시글 삭제", description = "게시글 ID와 로그인한 유저 정보를 비교하여 본인의 게시글을 삭제합니다.")
    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(@Parameter(description = "삭제할 게시글의 ID") @PathVariable Long postId,
                                          @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails){

        postService.deletePost(postId, userDetails);

        return ApiResponse.of(SuccessStatus._NO_CONTENT, null);
    }

}