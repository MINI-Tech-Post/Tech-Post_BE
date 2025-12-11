package com.ureka.techpost.domain.comment.controller;


import com.ureka.techpost.domain.auth.dto.CustomUserDetails;
import com.ureka.techpost.domain.comment.dto.CommentRequestDTO;
import com.ureka.techpost.domain.comment.dto.CommentResponseDTO;
import com.ureka.techpost.domain.comment.service.CommentService;
import com.ureka.techpost.global.apiPayload.ApiResponse;
import com.ureka.techpost.global.apiPayload.code.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @file CommentController.java
 * @author 최승언
 * @version 1.0
 * @since 2025-12-10
 * @description 댓글 관련 API 요청(추가, 조회, 수정, 삭제)을 받아 서비스 계층으로 전달하고, 처리 결과를 클라이언트에게 반환하는 컨트롤러 클래스입니다.
 */

@Tag(name = "댓글(Comment) API", description = "게시글 댓글 관련 API")
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 작성", description = "특정 게시글(postId)에 새로운 댓글을 작성합니다.")
    @PostMapping("/posts/{postId}/comments")
    public ApiResponse<Void> createComment(@Parameter(description = "댓글을 달 게시글의 ID") @PathVariable Long postId,
                                           @RequestBody CommentRequestDTO commentRequestDTO,
                                           @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails){

        commentService.createComment(commentRequestDTO, userDetails, postId);

        return ApiResponse.of(SuccessStatus._CREATED, null);
    }

    @Operation(summary = "댓글 목록 조회", description = "특정 게시글(postId)에 달린 모든 댓글 목록을 조회합니다.")
    @GetMapping("/posts/{postId}/comments")
    public ApiResponse<List<CommentResponseDTO>> getComments(@Parameter(description = "조회할 게시글의 ID") @PathVariable Long postId){

        List<CommentResponseDTO> commentResponseList = commentService.findByPostId(postId);

        return ApiResponse.onSuccess(commentResponseList);
    }

    @Operation(summary = "댓글 삭제", description = "댓글 ID를 이용하여 본인이 작성한 댓글을 삭제합니다.")
    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<Void> deleteComment(@Parameter(description = "삭제할 댓글의 ID") @PathVariable Long commentId,
                                                @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails){

        commentService.deleteComment(commentId, userDetails);

        return ApiResponse.of(SuccessStatus._NO_CONTENT, null);
    }

    @Operation(summary = "댓글 수정", description = "댓글 ID를 이용하여 본인이 작성한 댓글 내용을 수정합니다.")
    @PatchMapping("/comments/{commentId}")
    public ApiResponse<Void> patchComment(@Parameter(description = "수정할 댓글의 ID") @PathVariable Long commentId,
                                               @RequestBody CommentRequestDTO commentRequestDTO,
                                               @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails){

        commentService.patchComment(commentId, userDetails, commentRequestDTO);

        return ApiResponse.of(SuccessStatus._NO_CONTENT, null);
    }

}
