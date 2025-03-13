package com.example.board.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findPageByOrderByCreatedAtDesc(Pageable pageable);

    Page<Post> findByTitleContainingOrderByCreatedAtDesc(String keyword, Pageable pageable);
    Page<Post> findByContentContainingOrderByCreatedAtDesc(String keyword, Pageable pageable);
    Page<Post> findByUser_NicknameContainingOrderByCreatedAtDesc(String keyword, Pageable pageable);
    Page<Post> findByTitleContainingOrContentContainingOrderByCreatedAtDesc(
            String titleKeyword, String contentKeyword, Pageable pageable);
    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);
}
