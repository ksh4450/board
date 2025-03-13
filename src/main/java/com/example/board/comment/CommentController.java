package com.example.board.comment;

import com.example.board.post.Post;
import com.example.board.post.PostRepository;
import com.example.board.user.User;
import com.example.board.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @PostMapping("/comment/write")
    public String writeComment(@RequestParam Long postId,
                               @RequestParam String content,
                               RedirectAttributes redirectAttributes) {


        if (content.length() > 500) {
            redirectAttributes.addFlashAttribute("errorMessage", "댓글은 500자 이내로 작성해주세요.");
            redirectAttributes.addFlashAttribute("previousContent", content);
            redirectAttributes.addFlashAttribute("postId", postId);
            return "redirect:/post/" + postId;
        }

        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        // Find the post
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Find the user
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create new comment
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setPost(post);
        comment.setUser(user);

        // Save comment
        commentRepository.save(comment);

        // Redirect back to the post page
        return "redirect:/post/" + postId;
    }

    @PostMapping("/comment/delete")
    public String deleteComment(@RequestParam Long commentId, RedirectAttributes redirectAttributes) {

        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        // Find the comment
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // Check if the current user is the comment author
        if (!comment.getUser().getUsername().equals(currentUsername)) {
            redirectAttributes.addFlashAttribute("errorMessage", "댓글을 삭제할 권한이 없습니다.");
            return "redirect:/post/" + comment.getPost().getId();
        }

        // Get the post ID before deleting
        Long postId = comment.getPost().getId();

        // Delete the comment
        commentRepository.delete(comment);

        return "redirect:/post/" + postId;
    }


    @PostMapping("/comment/edit")
    public String editComment(@RequestParam Long commentId,
                              @RequestParam String content,
                              RedirectAttributes redirectAttributes) {
        // Check content length
        if (content.length() > 500) {
            redirectAttributes.addFlashAttribute("errorMessage", "댓글은 500자 이내로 작성해주세요.");
            return "redirect:/post/{postId}"; // You might need to pass postId
        }

        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        // Find the comment
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // Check if the current user is the comment author
        if (!comment.getUser().getUsername().equals(currentUsername)) {
            redirectAttributes.addFlashAttribute("errorMessage", "댓글을 수정할 권한이 없습니다.");
            return "redirect:/post/" + comment.getPost().getId();
        }

        // Update comment
        comment.setContent(content);
        commentRepository.save(comment);

        // Redirect back to the post page
        return "redirect:/post/" + comment.getPost().getId();
    }
}
