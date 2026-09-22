package com.wufivenull.blog.favorite;

import com.wufivenull.blog.post.Post;
import com.wufivenull.blog.user.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    boolean existsByUserAndPost(UserAccount user, Post post);

    void deleteByUserAndPost(UserAccount user, Post post);
}
