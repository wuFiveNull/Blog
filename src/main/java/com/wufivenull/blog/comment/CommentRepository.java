package com.wufivenull.blog.comment;

import com.wufivenull.blog.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostAndStatusOrderByCreatedAtAsc(Post post, CommentStatus status);

    List<Comment> findAllByOrderByCreatedAtDesc();
}
