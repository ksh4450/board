package com.example.board.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class CustomUser extends User {

    private final String nickname;

    public CustomUser(String username,
                      String password,
                      Collection<? extends GrantedAuthority> authorities,
                      String nickname
    ){
        super(username, password, authorities);
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

}
