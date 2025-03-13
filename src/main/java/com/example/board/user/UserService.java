package com.example.board.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    public void saveUser(String username,
                         String password,
                         String email,
                         String nickname) {
        User user = new User();
        user.setUsername(username);
        var hash = passwordEncoder.encode(password);
        user.setPassword(hash);
        user.setEmail(email);
        user.setNickname(nickname);
        userRepository.save(user);
    }

}
