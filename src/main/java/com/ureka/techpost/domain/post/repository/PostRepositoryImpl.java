package com.ureka.techpost.domain.post.repository;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.ureka.techpost.domain.post.dto.PostResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.ureka.techpost.domain.post.entity.QPost.post;
import static com.ureka.techpost.domain.comment.entity.QComment.comment;
import static com.ureka.techpost.domain.likes.entity.QLikes.likes;

/**
 * @file PostRepositoryImpl.java
 * @author 최승언
 * @version 1.2
 * @since 2025-12-09
 * @description QueryDSL을 활용하여 게시글 검색, 필터링 등 복잡한 조회 로직을 실제로 구현한 클래스입니다.
 */

@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;


    @Override
    public Page<PostResponseDTO> search(String keyword, String sourceName, Pageable pageable) {

        List<PostResponseDTO> content = queryFactory
                .select(Projections.constructor(PostResponseDTO.class,
                        post.id,
                        post.title,
                        post.summary,
                        post.originalUrl,
                        post.thumbnailUrl,
                        post.publisher,
                        post.publishedAt,
                        post.sourceName,
                        post.createdAt,
                         ExpressionUtils.as(
                                JPAExpressions.select(likes.count())
                                        .from(likes)
                                        .where(likes.post.eq(post)),
                                "likeCount"),

                         ExpressionUtils.as(
                                JPAExpressions.select(comment.count())
                                        .from(comment)
                                        .where(comment.post.eq(post)),
                                "commentCount")
                ))
                .from(post)
                .where(
                        titleOrSummaryContains(keyword), // 제목 or 요약
                        sourceNameContains(sourceName)   // 출처
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(post.id.desc()) // 정렬
                .fetch();

        // 카운트 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(post.count())
                .from(post)
                .where(
                        titleOrSummaryContains(keyword),
                        sourceNameContains(sourceName)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    // 제목 or 요약 키워드 검색
    private BooleanExpression titleOrSummaryContains(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return post.title.contains(keyword)
                .or(post.summary.contains(keyword));
    }

    // 출처 검색
    private BooleanExpression sourceNameContains(String provider) {
        if (!StringUtils.hasText(provider)) {
            return null;
        }
        return post.sourceName.contains(provider);
    }
}
