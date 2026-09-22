package com.wufivenull.blog.post;

import com.wufivenull.blog.user.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Post> findByStatusOrderByPublishedAtDesc(PostStatus status);

    List<Post> findByAuthorOrderByUpdatedAtDesc(UserAccount author);

    List<Post> findAllByOrderByUpdatedAtDesc();
}
