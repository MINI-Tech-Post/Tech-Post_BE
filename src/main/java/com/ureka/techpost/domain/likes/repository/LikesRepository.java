package com.ureka.techpost.domain.likes.repository;


import com.ureka.techpost.domain.likes.entity.Likes;
import com.ureka.techpost.domain.post.entity.Post;
import com.ureka.techpost.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikesRepository extends JpaRepository<Likes, Long> {
    boolean existsByUserAndPost(User user, Post post);
    boolean existsByUserUserIdAndPostId(Long userId, Long postId);
    Optional<Likes> findByUserAndPost(User user, Post post);
}
