package com.wufivenull.blog.post;

import com.wufivenull.blog.user.RoleCode;
import com.wufivenull.blog.user.UserAccount;
import org.springframework.stereotype.Service;

@Service
public class PostAccessService {

    public boolean canView(Post post, UserAccount user) {
        if (user.hasRole(RoleCode.ADMIN)) {
            return true;
        }
        if (post.getAuthor().getId().equals(user.getId())) {
            return true;
        }
        if (post.getStatus() != PostStatus.PUBLISHED) {
            return false;
        }
        return post.getAllowedRoles().stream()
                .anyMatch(role -> user.hasRole(role.getCode()));
    }

    public boolean canManage(Post post, UserAccount user) {
        return user.hasRole(RoleCode.ADMIN)
                || post.getAuthor().getId().equals(user.getId());
    }

    public boolean isAdmin(UserAccount user) {
        return user.hasRole(RoleCode.ADMIN);
    }
}
