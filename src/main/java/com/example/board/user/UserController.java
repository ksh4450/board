package com.example.board.user;

import com.example.board.comment.Comment;
import com.example.board.comment.CommentService;
import com.example.board.post.Post;
import com.example.board.post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @PostMapping("/signup")
    public String registerUser(RedirectAttributes redirectAttributes,
                               String username,
                               String password,
                               String email,
                               String nickname,
                               String password_check) {

        boolean hasError = false;

        // 아이디 검증
        if (!isValidUsername(username)) {
            redirectAttributes.addFlashAttribute("usernameError", "아이디는 5-15자의 영소문자와 숫자만 가능합니다.");
            hasError = true;
        }

        // 아이디 중복 체크
        var usernameResult = userRepository.findByUsername(username);
        if (usernameResult.isPresent()) {
            redirectAttributes.addFlashAttribute("usernameError", "이미 존재하는 아이디입니다.");
            hasError = true;
        }

        // 비밀번호 검증
        if (!isStrongPassword(password)) {
            redirectAttributes.addFlashAttribute("passwordError", "비밀번호는 8자 이상, 대문자, 소문자, 숫자, 특수문자를 포함해야 합니다.");
            hasError = true;
        }

        // 비밀번호 일치 확인
        if (!password.equals(password_check)) {
            redirectAttributes.addFlashAttribute("passwordCheckError", "비밀번호가 일치하지 않습니다.");
            hasError = true;
        }

        // 이메일 검증
        if (!isValidEmail(email)) {
            redirectAttributes.addFlashAttribute("emailError", "올바르지 않은 이메일 형식입니다.");
            hasError = true;
        }

        //이메일 중복 체크
        var emailResult = userRepository.findByEmail(email);
        if (emailResult.isPresent()) {
            redirectAttributes.addFlashAttribute("emailError", "이미 사용 중인 이메일입니다.");
            hasError = true;
        }

        // 닉네임 검증
        if (!isValidNickname(nickname)) {
            redirectAttributes.addFlashAttribute("nicknameError", "닉네임은 2-10자의 한글, 영문, 숫자만 가능합니다.");
            hasError = true;
        }

        //닉네임 중복 체크
        var nicknameResult = userRepository.findByNickname(nickname);
        if (nicknameResult.isPresent()) {
            redirectAttributes.addFlashAttribute("nicknameError", "이미 사용 중인 닉네임입니다.");
            hasError = true;
        }

        // 에러가 있으면 회원가입 페이지로 리다이렉트
        if (hasError) {
            return "redirect:/signup";
        }

        // 모든 검증 통과 후 회원가입 처리
        try {
            userService.saveUser(username, password, email, nickname);
            redirectAttributes.addAttribute("nickname", nickname);
            return "redirect:/welcome";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "회원가입 중 오류가 발생했습니다.");
            return "redirect:/signup";
        }
    }

    private boolean isValidUsername(String username) {
        return username != null && username.matches("^[a-z0-9]{5,15}$");
    }

    private boolean isStrongPassword(String password) {
        return password != null && password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+]).{8,}$");
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private boolean isValidNickname(String nickname) {
        return nickname != null && nickname.matches("^[가-힣a-zA-Z0-9]{2,10}$");
    }

    @GetMapping("/welcome")
    public String welcome(String nickname, Model model) {
        model.addAttribute("nickname", nickname);
        return "welcome";
    }

    @GetMapping("/users/{userId}")
    public String getUserProfile(@PathVariable Long userId, Model model) {
        // 사용자 정보 조회
        User user = userService.findById(userId);
        model.addAttribute("user", user);

        // 사용자가 작성한 게시글 목록 조회
        List<Post> userPosts = postService.findByUserId(userId);
        model.addAttribute("userPosts", userPosts);

        // 사용자가 작성한 댓글 목록 조회
        List<Comment> userComments = commentService.findByUserId(userId);
        model.addAttribute("userComments", userComments);

        return "user-profile";
    }

}
