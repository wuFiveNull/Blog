package com.wufivenull.blog.web;

import com.wufivenull.blog.comment.*;
import com.wufivenull.blog.favorite.Favorite;
import com.wufivenull.blog.favorite.FavoriteRepository;
import com.wufivenull.blog.markdown.MarkdownService;
import com.wufivenull.blog.post.*;
import com.wufivenull.blog.user.AccountService;
import com.wufivenull.blog.user.UserAccount;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@Controller
public class PostController {

    private final PostRepository postRepository;
    private final PostService postService;
    private final PostAccessService accessService;
    private final AccountService accountService;
    private final TagRepository tagRepository;
    private final CategoryRepository categoryRepository;
    private final CommentRepository commentRepository;
    private final FavoriteRepository favoriteRepository;
    private final MarkdownService markdownService;

    public PostController(PostRepository postRepository, PostService postService,
                          PostAccessService accessService, AccountService accountService,
                          TagRepository tagRepository, CategoryRepository categoryRepository,
                          CommentRepository commentRepository, FavoriteRepository favoriteRepository,
                          MarkdownService markdownService) {
        this.postRepository = postRepository;
        this.postService = postService;
        this.accessService = accessService;
        this.accountService = accountService;
        this.tagRepository = tagRepository;
        this.categoryRepository = categoryRepository;
        this.commentRepository = commentRepository;
        this.favoriteRepository = favoriteRepository;
        this.markdownService = markdownService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/posts";
    }

    @GetMapping("/posts")
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String tag,
                       @RequestParam(required = false) String category,
                       Authentication authentication, Model model) {
        UserAccount user = accountService.current(authentication);
        String selectedTag = normalizeTag(tag);
        String selectedCategory = category == null ? "" : category.trim();
        List<Post> posts = postRepository.findByStatusOrderByPublishedAtDesc(PostStatus.PUBLISHED)
                .stream()
                .filter(post -> accessService.canView(post, user))
                .filter(post -> q == null || q.isBlank()
                        || post.getTitle().toLowerCase(Locale.ROOT).contains(q.trim().toLowerCase(Locale.ROOT)))
                .filter(post -> selectedTag.isBlank()
                        || post.getTags().stream()
                        .anyMatch(postTag -> postTag.getName().equalsIgnoreCase(selectedTag)))
                .filter(post -> selectedCategory.isBlank()
                        || (post.getCategory() != null
                        && post.getCategory().getName().equalsIgnoreCase(selectedCategory)))
                .toList();
        model.addAttribute("posts", posts);
        model.addAttribute("query", q == null ? "" : q);
        model.addAttribute("selectedTag", selectedTag);
        model.addAttribute("selectedCategory", selectedCategory);
        model.addAttribute("tagOptions", tagRepository.findAllByOrderByNameAsc());
        model.addAttribute("categoryOptions", categoryRepository.findAllByOrderByNameAsc());
        return "posts/list";
    }

    @GetMapping("/posts/{slug}")
    public String detail(@PathVariable String slug, Authentication authentication, Model model) {
        UserAccount user = accountService.current(authentication);
        Post post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        if (!accessService.canView(post, user)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN);
        }
        model.addAttribute("post", post);
        model.addAttribute("contentHtml", markdownService.render(post.getContentMarkdown()));
        model.addAttribute("comments", commentRepository.findByPostAndStatusOrderByCreatedAtAsc(
                post, CommentStatus.VISIBLE));
        model.addAttribute("favorite", favoriteRepository.existsByUserAndPost(user, post));
        model.addAttribute("canManage", accessService.canManage(post, user));
        return "posts/detail";
    }

    @GetMapping("/me/posts")
    public String mine(Authentication authentication, Model model) {
        model.addAttribute("posts", postRepository.findByAuthorOrderByUpdatedAtDesc(
                accountService.current(authentication)));
        return "posts/mine";
    }

    @GetMapping("/posts/new")
    public String newPost(Model model) {
        model.addAttribute("postForm", new PostForm());
        model.addAttribute("categoryOptions", categoryRepository.findAllByOrderByNameAsc());
        return "posts/form";
    }

    @PostMapping("/posts")
    public String create(@ModelAttribute PostForm form, Authentication authentication) {
        Post post = postService.create(accountService.current(authentication), form.getTitle(),
                form.getSummary(), form.getContentMarkdown(), form.getAllowedRoles(),
                form.getCategory(), form.getTags(), form.isPublish());
        return "redirect:/posts/" + post.getSlug();
    }

    @GetMapping("/posts/{id}/edit")
    public String edit(@PathVariable Long id, Authentication authentication, Model model) {
        UserAccount user = accountService.current(authentication);
        Post post = getPost(id);
        ensureManage(post, user);
        PostForm form = new PostForm();
        form.setId(post.getId());
        form.setTitle(post.getTitle());
        form.setSummary(post.getSummary());
        form.setContentMarkdown(post.getContentMarkdown());
        form.setAllowedRoles(post.getAllowedRoles().stream()
                .map(role -> role.getCode().name()).sorted().reduce((a, b) -> a + "," + b).orElse(""));
        form.setCategory(post.getCategory() == null ? "" : post.getCategory().getName());
        form.setTags(post.getTags().stream().map(Tag::getName).sorted()
                .reduce((a, b) -> a + ", " + b).orElse(""));
        form.setPublish(post.getStatus() == PostStatus.PUBLISHED);
        model.addAttribute("postForm", form);
        model.addAttribute("categoryOptions", categoryRepository.findAllByOrderByNameAsc());
        return "posts/form";
    }

    @PostMapping("/posts/{id}")
    public String update(@PathVariable Long id, @ModelAttribute PostForm form,
                         Authentication authentication) {
        UserAccount user = accountService.current(authentication);
        Post post = getPost(id);
        ensureManage(post, user);
        postService.update(post, form.getTitle(), form.getSummary(), form.getContentMarkdown(),
                form.getAllowedRoles(), form.getCategory(), form.getTags(), form.isPublish());
        return "redirect:/posts/" + post.getSlug();
    }

    @PostMapping("/posts/{id}/delete")
    public String delete(@PathVariable Long id, Authentication authentication) {
        UserAccount user = accountService.current(authentication);
        Post post = getPost(id);
        ensureManage(post, user);
        postRepository.delete(post);
        return "redirect:/me/posts";
    }

    @PostMapping("/posts/{id}/comments")
    public String comment(@PathVariable Long id, @RequestParam String content,
                          Authentication authentication) {
        UserAccount user = accountService.current(authentication);
        Post post = getPost(id);
        if (!accessService.canView(post, user) || post.getStatus() != PostStatus.PUBLISHED
                || content == null || content.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN);
        }
        commentRepository.save(new Comment(post, user, content.trim()));
        return "redirect:/posts/" + post.getSlug() + "#comments";
    }

    @PostMapping("/comments/{id}/delete")
    public String deleteComment(@PathVariable Long id, Authentication authentication) {
        UserAccount user = accountService.current(authentication);
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("评论不存在"));
        if (!comment.getUser().getId().equals(user.getId()) && !accessService.isAdmin(user)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN);
        }
        commentRepository.delete(comment);
        return "redirect:/posts/" + comment.getPost().getSlug() + "#comments";
    }

    @PostMapping("/posts/{id}/favorite")
    public String favorite(@PathVariable Long id, Authentication authentication) {
        UserAccount user = accountService.current(authentication);
        Post post = getPost(id);
        if (!accessService.canView(post, user) || post.getStatus() != PostStatus.PUBLISHED) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN);
        }
        if (!favoriteRepository.existsByUserAndPost(user, post)) {
            favoriteRepository.save(new Favorite(user, post));
        }
        return "redirect:/posts/" + post.getSlug();
    }

    @PostMapping("/posts/{id}/unfavorite")
    public String unfavorite(@PathVariable Long id, Authentication authentication) {
        UserAccount user = accountService.current(authentication);
        Post post = getPost(id);
        favoriteRepository.deleteByUserAndPost(user, post);
        return "redirect:/posts/" + post.getSlug();
    }

    @PostMapping(value = "/posts/preview", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String preview(@RequestParam String contentMarkdown) {
        return markdownService.render(contentMarkdown);
    }

    private Post getPost(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
    }

    private void ensureManage(Post post, UserAccount user) {
        if (!accessService.canManage(post, user)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN);
        }
    }

    private String normalizeTag(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceFirst("^#+", "");
    }
}
