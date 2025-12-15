package com.ureka.techpost.domain.likes.controller;

import com.ureka.techpost.domain.auth.dto.CustomUserDetails;
import com.ureka.techpost.domain.likes.service.LikesService;
import com.ureka.techpost.global.apiPayload.ApiResponse;
import com.ureka.techpost.global.apiPayload.code.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LikesController {

    private final LikesService likesService;

    @PostMapping("/posts/{postId}/likes")
    public ApiResponse<Void> addLike(@PathVariable Long postId,
                                       @AuthenticationPrincipal CustomUserDetails userDetails){

        likesService.createLike(postId, userDetails);

        return ApiResponse.of(SuccessStatus._CREATED, null);
    }

    @DeleteMapping("/posts/{postId}/likes")
    public ApiResponse<Void> deleteLike(@PathVariable Long postId,
                                             @AuthenticationPrincipal CustomUserDetails userDetails){

        likesService.deleteLike(postId, userDetails);

        return ApiResponse.of(SuccessStatus._NO_CONTENT, null);
    }

}
