package com.wufivenull.blog.web;

import com.wufivenull.blog.comment.Comment;
import com.wufivenull.blog.comment.CommentRepository;
import com.wufivenull.blog.post.PostRepository;
import com.wufivenull.blog.user.AccountService;
import com.wufivenull.blog.user.RoleCode;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AccountService accountService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public AdminController(AccountService accountService, PostRepository postRepository,
                           CommentRepository commentRepository) {
        this.accountService = accountService;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    @GetMapping
    public String home() {
        return "redirect:/admin/posts";
    }

    @GetMapping("/posts")
    public String posts(Model model) {
        model.addAttribute("posts", postRepository.findAllByOrderByUpdatedAtDesc());
        return "admin/posts";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", accountService.findAll());
        return "admin/users";
    }

    @PostMapping("/users")
    public String createUser(@RequestParam String username, @RequestParam String nickname,
                             @RequestParam String password, @RequestParam RoleCode role) {
        accountService.create(username, nickname, password, role);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/disable")
    public String disableUser(@PathVariable Long id) {
        accountService.disable(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/comments")
    public String comments(Model model) {
        model.addAttribute("comments", commentRepository.findAllByOrderByCreatedAtDesc());
        return "admin/comments";
    }

    @PostMapping("/comments/{id}/hide")
    public String hideComment(@PathVariable Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("评论不存在"));
        comment.hide();
        commentRepository.save(comment);
        return "redirect:/admin/comments";
    }
}
