package com.wufivenull.blog.post;

import com.wufivenull.blog.user.Role;
import com.wufivenull.blog.user.UserAccount;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, unique = true, length = 220)
    private String slug;

    @Column(length = 500)
    private String summary;

    @Lob
    @Column(nullable = false)
    private String contentMarkdown = "";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status = PostStatus.DRAFT;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private UserAccount author;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "post_allowed_roles",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> allowedRoles = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "post_tags",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @Column(length = 500)
    private String coverImageUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime publishedAt;

    protected Post() {
    }

    public Post(String title, String slug, UserAccount author) {
        this.title = title;
        this.slug = slug;
        this.author = author;
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

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void publish() {
        status = PostStatus.PUBLISHED;
        publishedAt = LocalDateTime.now();
    }

    public void archive() {
        status = PostStatus.ARCHIVED;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContentMarkdown() {
        return contentMarkdown;
    }

    public void setContentMarkdown(String contentMarkdown) {
        this.contentMarkdown = contentMarkdown == null ? "" : contentMarkdown;
    }

    public PostStatus getStatus() {
        return status;
    }

    public UserAccount getAuthor() {
        return author;
    }

    public Set<Role> getAllowedRoles() {
        return allowedRoles;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }
}
