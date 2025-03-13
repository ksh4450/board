package com.example.board.vote;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Controller
@RequestMapping("/vote")
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    @PostMapping("/up/{postId}")
    @ResponseBody
    public ResponseEntity<?> upVote(@PathVariable Long postId, Principal principal) {
        try {
            voteService.vote(postId, principal.getName(), Vote.VoteType.UP_VOTE);
            Map<String, Integer> voteCounts = voteService.getVoteCounts(postId);
            return ResponseEntity.ok(voteCounts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/down/{postId}")
    @ResponseBody
    public ResponseEntity<?> downVote(@PathVariable Long postId, Principal principal) {
        try {
            voteService.vote(postId, principal.getName(), Vote.VoteType.DOWN_VOTE);
            Map<String, Integer> voteCounts = voteService.getVoteCounts(postId);
            return ResponseEntity.ok(voteCounts);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
