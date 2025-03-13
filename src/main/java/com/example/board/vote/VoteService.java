package com.example.board.vote;

import com.example.board.post.Post;
import com.example.board.post.PostRepository;
import com.example.board.user.User;
import com.example.board.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class VoteService {

    private final VoteRepository voteRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public VoteService(VoteRepository voteRepository,
                       PostRepository postRepository,
                       UserRepository userRepository) {
        this.voteRepository = voteRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public void vote(Long postId, String username, Vote.VoteType voteType) {
        // 1. 사용자와 게시글 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        // 2. 이전 투표 확인
        Optional<Vote> existingVote = voteRepository.findByUserAndPost(user, post);

        if (existingVote.isPresent()) {
            // 3. 이전 투표가 있는 경우
            Vote vote = existingVote.get();

            // 같은 유형의 투표인 경우 - 투표 취소
            if (vote.getVoteType() == voteType) {
                cancelVote(vote, post);
            } else {
                // 다른 유형의 투표인 경우 - 투표 변경
                updateVote(vote, voteType, post);
            }
        } else {
            // 4. 새로운 투표
            createNewVote(user, post, voteType);
        }

        // 5. 변경사항 저장
        postRepository.save(post);
    }

    private void cancelVote(Vote vote, Post post) {
        // 투표 취소 시 카운트 감소
        if (vote.getVoteType() == Vote.VoteType.UP_VOTE) {
            post.setUpVoteCount(post.getUpVoteCount() - 1);
        } else {
            post.setDownVoteCount(post.getDownVoteCount() - 1);
        }
        voteRepository.delete(vote);
    }

    private void updateVote(Vote vote, Vote.VoteType newVoteType, Post post) {
        // 이전 투표 카운트 감소
        if (vote.getVoteType() == Vote.VoteType.UP_VOTE) {
            post.setUpVoteCount(post.getUpVoteCount() - 1);
        } else {
            post.setDownVoteCount(post.getDownVoteCount() - 1);
        }

        // 새로운 투표 카운트 증가
        if (newVoteType == Vote.VoteType.UP_VOTE) {
            post.setUpVoteCount(post.getUpVoteCount() + 1);
        } else {
            post.setDownVoteCount(post.getDownVoteCount() + 1);
        }

        vote.setVoteType(newVoteType);
        voteRepository.save(vote);
    }

    private void createNewVote(User user, Post post, Vote.VoteType voteType) {
        Vote vote = new Vote();
        vote.setUser(user);
        vote.setPost(post);
        vote.setVoteType(voteType);

        // 투표 카운트 증가
        if (voteType == Vote.VoteType.UP_VOTE) {
            post.setUpVoteCount(post.getUpVoteCount() + 1);
        } else {
            post.setDownVoteCount(post.getDownVoteCount() + 1);
        }

        voteRepository.save(vote);
    }

    // 특정 게시글의 추천/비추천 수 조회
    public Map<String, Integer> getVoteCounts(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Map<String, Integer> voteCounts = new HashMap<>();
        voteCounts.put("upVotes", post.getUpVoteCount());
        voteCounts.put("downVotes", post.getDownVoteCount());

        return voteCounts;
    }

    // 사용자의 투표 여부 확인
    public Optional<Vote.VoteType> getUserVote(Long postId, String username) {
        return voteRepository.findByUserUsernameAndPostId(username, postId)
                .map(Vote::getVoteType);
    }
}