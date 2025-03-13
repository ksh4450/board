package com.example.board.vote;

import com.example.board.post.Post;
import com.example.board.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {
    Optional<Vote> findByUserAndPost(User user, Post post);
    Optional<Vote> findByUserUsernameAndPostId(String username, Long postId);
    List<Vote> findByPost(Post post);
}
