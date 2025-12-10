package com.ureka.techpost.domain.comment.service;


import com.ureka.techpost.domain.auth.dto.CustomUserDetails;
import com.ureka.techpost.domain.comment.dto.CommentRequestDTO;
import com.ureka.techpost.domain.comment.dto.CommentResponseDTO;
import com.ureka.techpost.domain.comment.entity.Comment;
import com.ureka.techpost.domain.comment.repository.CommentRepository;
import com.ureka.techpost.domain.post.entity.Post;
import com.ureka.techpost.domain.post.repository.PostRepository;
import com.ureka.techpost.domain.user.entity.User;
import com.ureka.techpost.domain.user.repository.UserRepository;
import com.ureka.techpost.global.exception.CustomException;
import com.ureka.techpost.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @file CommentService.java
 * @author 최승언
 * @version 1.0
 * @since 2025-12-10
 * @description 댓글 작성, 수정, 삭제, 조회 등 댓글 도메인의 핵심 비즈니스 로직을 처리하고 트랜잭션을 관리하는 서비스 클래스입니다.
 */

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    @Transactional
    public void createComment(CommentRequestDTO commentRequestDTO, CustomUserDetails userDetails, Long postId){

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        User user = userRepository.findByUsername(userDetails.getUsername())
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Comment comment = Comment.builder()
                .user(user)
                .post(post)
                .content(commentRequestDTO.getContent())
                .build();

        commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDTO> findByPostId(Long postId) {

        if(!postRepository.existsById(postId)){
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
        }

        return commentRepository.findAllByPostId(postId).stream()
                .map(comment -> new CommentResponseDTO(
                        comment.getId()
                        , comment.getUser().getUserId()
                        , comment.getUser().getName()
                        , comment.getContent()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void patchComment(Long commentId, CustomUserDetails userDetails, CommentRequestDTO commentRequestDTO) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if(!comment.getUser().getUserId().equals(user.getUserId())){
            throw new CustomException(ErrorCode.USER_NOT_MATCH);
        }

        comment.updateContent(commentRequestDTO.getContent());
    }

    @Transactional
    public void deleteComment(Long commentId, CustomUserDetails userDetails) {

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if(!comment.getUser().getUserId().equals(user.getUserId())){
            throw new CustomException(ErrorCode.USER_NOT_MATCH);
        }

        commentRepository.delete(comment);
    }
}
