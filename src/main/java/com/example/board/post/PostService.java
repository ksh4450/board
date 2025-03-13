package com.example.board.post;

import com.example.board.comment.CommentRepository;
import com.example.board.vote.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    public void deletePost(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        // 작성자 검증
        if (!post.getUser().getUsername().equals(username)) {
            throw new RuntimeException("게시글의 작성자만 삭제할 수 있습니다.");
        }

        postRepository.delete(post);
    }

    public List<Post> findByUserId(Long userId) {
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
