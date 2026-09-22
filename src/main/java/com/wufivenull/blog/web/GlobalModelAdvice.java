package com.wufivenull.blog.web;

import com.wufivenull.blog.user.AccountService;
import com.wufivenull.blog.user.UserAccount;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    private final AccountService accountService;

    public GlobalModelAdvice(AccountService accountService) {
        this.accountService = accountService;
    }

    @ModelAttribute
    public void addCurrentUser(Authentication authentication, org.springframework.ui.Model model) {
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            UserAccount user = accountService.current(authentication);
            model.addAttribute("currentUser", user);
            model.addAttribute("isAdmin", user.hasRole(com.wufivenull.blog.user.RoleCode.ADMIN));
        }
    }
}
