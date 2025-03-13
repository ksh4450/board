package com.example.board.post;

import com.example.board.comment.Comment;
import com.example.board.comment.CommentRepository;
import com.example.board.user.User;
import com.example.board.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequiredArgsConstructor
public class PostController {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PostService postService;

    @GetMapping("/home")
    public String home(Model model) {

        return page(1, model);
    }

    @GetMapping("/home/page/{pageNumber}")
    public String page(@PathVariable int pageNumber, Model model) {

        Page<Post> posts = postRepository.findPageByOrderByCreatedAtDesc(PageRequest.of(pageNumber - 1, 10));
        model.addAttribute("posts", posts);
        model.addAttribute("currentPage", pageNumber); // 현재 페이지 번호 추가
        model.addAttribute("totalPages", posts.getTotalPages()); // 전체 페이지 수 추가
        return "home";
    }

    @GetMapping("/write")
    public String write() {
        return "post-create";
    }

    @PostMapping("/write")
    public String writePost(String title, String content, Model model,
                            @AuthenticationPrincipal UserDetails userDetails) {

        title = title.trim();
        if (title.isEmpty() || title.length() > 80) {
            model.addAttribute("error", "제목은 1-80자 사이어야 합니다.");
            return "post-create";
        }

        // Trim and validate content
        content = content.trim();
        if (content.isEmpty()) {
            model.addAttribute("error", "내용을 입력해주세요.");
            return "post-create";
        }

        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setViews(0L);

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        post.setUser(user);

        postRepository.save(post);

        return "redirect:/home";
    }

    @GetMapping("/search")
    public String searchPost(@RequestParam(defaultValue = "title") String searchType,
                             @RequestParam(defaultValue = "") String keyword,
                             @RequestParam(defaultValue = "1") int page,
                             Model model) {

        Page<Post> searchResults;
        PageRequest pageRequest = PageRequest.of(page - 1, 10);

        switch (searchType) {
            case "title":
                searchResults = postRepository.findByTitleContainingOrderByCreatedAtDesc(keyword, pageRequest);
                break;
            case "content":
                searchResults = postRepository.findByContentContainingOrderByCreatedAtDesc(keyword, pageRequest);
                break;
            case "user":
                searchResults = postRepository.findByUser_NicknameContainingOrderByCreatedAtDesc(keyword, pageRequest);
                break;
            case "title+content":
                searchResults = postRepository.findByTitleContainingOrContentContainingOrderByCreatedAtDesc(
                        keyword, keyword, pageRequest);
                break;
            default:
                searchResults = postRepository.findAll(PageRequest.of(page - 1, 10,
                        Sort.by(Sort.Direction.DESC, "createdAt")));
        }

        model.addAttribute("posts", searchResults);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", searchResults.getTotalPages());
        model.addAttribute("searchType", searchType);
        model.addAttribute("keyword", keyword);

        return "search";
    }

    @GetMapping("/post/{id}")
    public String viewPost(@PathVariable Long id, @RequestParam(defaultValue = "1") int page, Model model) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        post.setViews(post.getViews() + 1);
        postRepository.save(post);

        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtDesc(id);

        Page<Post> pagePosts = postRepository.findPageByOrderByCreatedAtDesc(PageRequest.of(page - 1, 10));
        List<Post> filteredPosts = pagePosts.getContent().stream()
                .filter(p -> !p.getId().equals(id))
                .collect(Collectors.toList());

        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        model.addAttribute("recentPosts", filteredPosts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pagePosts.getTotalPages());

        return "post";
    }

    @GetMapping("/post/edit/{id}")
    public String editPostForm(@PathVariable Long id, Model model, Principal principal) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 현재 사용자와 게시글 작성자가 같은지 확인
        if (!post.getUser().getUsername().equals(principal.getName())) {
            // 권한 없음 처리 (예: 홈 페이지로 리다이렉트 또는 에러 페이지)
            return "redirect:/home";
        }

        model.addAttribute("post", post);
        return "post-edit"; // 게시글 수정 폼 페이지
    }

    @PostMapping("/post/edit/{id}")
    public String editPost(@PathVariable Long id,
                           @RequestParam String title,
                           @RequestParam String content,
                           Model model,
                           Principal principal) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 작성자 확인
        if (!post.getUser().getUsername().equals(principal.getName())) {
            throw new RuntimeException("게시글의 작성자만 수정할 수 있습니다.");
        }

        // 제목 유효성 검사
        title = title.trim();
        if (title.isEmpty() || title.length() > 80) {
            model.addAttribute("error", "제목은 1-80자 사이어야 합니다.");
            model.addAttribute("post", post);
            return "post-edit";
        }

        // 내용 유효성 검사
        content = content.trim();
        if (content.isEmpty()) {
            model.addAttribute("error", "내용을 입력해주세요.");
            model.addAttribute("post", post);
            return "post-edit";
        }

        // 게시글 업데이트
        post.setTitle(title);
        post.setContent(content);
        postRepository.save(post);

        return "redirect:/post/" + id;
    }

    @PostMapping("/post/delete/{id}")
    public String deletePost(@PathVariable Long id, Principal principal) {
        postService.deletePost(id, principal.getName());
        return "redirect:/home";  // 홈페이지로 리다이렉트
    }


}
