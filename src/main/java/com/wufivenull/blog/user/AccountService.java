package com.wufivenull.blog.user;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AccountService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(UserRepository userRepository, RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserAccount current(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Authentication is required");
        }
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
    }

    public List<UserAccount> findAll() {
        return userRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public UserAccount create(String username, String nickname, String rawPassword, RoleCode roleCode) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("用户名已存在");
        }
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new IllegalStateException("角色未初始化"));
        UserAccount account = new UserAccount(username.trim(),
                passwordEncoder.encode(rawPassword), nickname.trim());
        account.getRoles().add(role);
        return userRepository.save(account);
    }

    @Transactional
    public void disable(Long id) {
        UserAccount account = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        account.setStatus(UserStatus.DISABLED);
    }
}
