package com.wufivenull.blog.favorite;

import com.wufivenull.blog.post.Post;
import com.wufivenull.blog.user.UserAccount;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_favorites",
        uniqueConstraints = @UniqueConstraint(name = "uk_favorite_user_post", columnNames = {"user_id", "post_id"}))
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Favorite() {
    }

    public Favorite(UserAccount user, Post post) {
        this.user = user;
        this.post = post;
        this.createdAt = LocalDateTime.now();
    }
}
