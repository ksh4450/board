package com.example.board.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;

    public List<Comment> findByUserId(Long userId) {
        return commentRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}