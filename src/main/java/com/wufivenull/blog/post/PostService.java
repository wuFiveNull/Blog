package com.wufivenull.blog.post;

import com.wufivenull.blog.user.Role;
import com.wufivenull.blog.user.RoleCode;
import com.wufivenull.blog.user.RoleRepository;
import com.wufivenull.blog.user.UserAccount;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final RoleRepository roleRepository;
    private final TagRepository tagRepository;
    private final CategoryRepository categoryRepository;

    public PostService(PostRepository postRepository, RoleRepository roleRepository,
                       TagRepository tagRepository, CategoryRepository categoryRepository) {
        this.postRepository = postRepository;
        this.roleRepository = roleRepository;
        this.tagRepository = tagRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Post create(UserAccount author, String title, String summary, String content,
                       String roleText, String categoryText, String tagText, boolean publish) {
        String slug = uniqueSlug(title);
        Post post = new Post(title.trim(), slug, author);
        apply(post, summary, content, roleText, categoryText, tagText, publish);
        return postRepository.save(post);
    }

    @Transactional
    public Post update(Post post, String title, String summary, String content,
                       String roleText, String categoryText, String tagText, boolean publish) {
        post.setTitle(title.trim());
        post.setSummary(blankToNull(summary));
        post.setContentMarkdown(content);
        post.getAllowedRoles().clear();
        post.getAllowedRoles().addAll(resolveRoles(roleText));
        post.setCategory(resolveCategory(categoryText));
        post.getTags().clear();
        post.getTags().addAll(resolveTags(tagText));
        if (publish) {
            post.publish();
        } else {
            post.unpublish();
        }
        return postRepository.save(post);
    }

    private void apply(Post post, String summary, String content, String roleText,
                       String categoryText, String tagText, boolean publish) {
        post.setSummary(blankToNull(summary));
        post.setContentMarkdown(content);
        post.getAllowedRoles().addAll(resolveRoles(roleText));
        post.setCategory(resolveCategory(categoryText));
        post.getTags().addAll(resolveTags(tagText));
        if (publish) {
            post.publish();
        }
    }

    private Category resolveCategory(String categoryText) {
        if (categoryText == null || categoryText.isBlank()) {
            return null;
        }
        String name = categoryText.trim();
        return categoryRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> categoryRepository.save(new Category(name)));
    }

    private Set<Role> resolveRoles(String roleText) {
        Set<RoleCode> codes = Arrays.stream((roleText == null ? "" : roleText).split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(value -> RoleCode.valueOf(value.toUpperCase(Locale.ROOT)))
                .collect(Collectors.toCollection(HashSet::new));
        if (codes.isEmpty()) {
            codes.add(RoleCode.OWNER);
            codes.add(RoleCode.USER);
        }
        return codes.stream()
                .map(code -> roleRepository.findByCode(code)
                        .orElseThrow(() -> new IllegalStateException("Role not initialized: " + code)))
                .collect(Collectors.toSet());
    }

    private Set<Tag> resolveTags(String tagText) {
        if (tagText == null || tagText.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(tagText.split("[,，\\s]+"))
                .map(String::trim)
                .map(this::normalizeTagName)
                .filter(value -> !value.isBlank())
                .map(value -> tagRepository.findByNameIgnoreCase(value)
                        .orElseGet(() -> tagRepository.save(new Tag(value))))
                .collect(Collectors.toSet());
    }

    private String normalizeTagName(String value) {
        return value.replaceFirst("^#+", "").trim();
    }

    private String uniqueSlug(String title) {
        String base = Normalizer.normalize(title, Normalizer.Form.NFKD)
                .replaceAll("[^\\p{Alnum}]+", "-")
                .toLowerCase(Locale.ROOT)
                .replaceAll("^-|-$", "");
        if (base.isBlank()) {
            base = "post";
        }
        String slug = base;
        int suffix = 2;
        while (postRepository.existsBySlug(slug)) {
            slug = base + "-" + suffix++;
        }
        return slug;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
