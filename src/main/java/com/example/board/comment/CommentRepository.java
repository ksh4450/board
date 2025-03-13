package com.example.board.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostIdOrderByCreatedAtDesc(Long postId);
    List<Comment> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Modifying
    @Transactional
    void deleteByPostId(Long postId);
}