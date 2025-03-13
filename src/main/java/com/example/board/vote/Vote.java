package com.example.board.vote;

import com.example.board.post.Post;
import com.example.board.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@ToString
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "post_id"})
})
public class Vote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 추천/비추천을 수행한 사용자

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Post post; // 추천/비추천 대상 게시글

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private VoteType voteType; // Integer 대신 enum 사용

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum VoteType {
        DOWN_VOTE(-1),
        UP_VOTE(1);

        private final int value;

        VoteType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }


}
