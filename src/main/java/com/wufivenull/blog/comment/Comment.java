package com.wufivenull.blog.comment;

import com.wufivenull.blog.post.Post;
import com.wufivenull.blog.user.UserAccount;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Lob
    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommentStatus status = CommentStatus.VISIBLE;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected Comment() {
    }

    public Comment(Post post, UserAccount user, String content) {
        this.post = post;
        this.user = user;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = createdAt;
        }
    }

    public Long getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public UserAccount getUser() {
        return user;
    }

    public String getContent() {
        return content;
    }

    public CommentStatus getStatus() {
        return status;
    }

    public void hide() {
        status = CommentStatus.HIDDEN;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
