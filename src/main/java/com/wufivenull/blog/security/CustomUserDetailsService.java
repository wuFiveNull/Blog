package com.wufivenull.blog.security;

import com.wufivenull.blog.user.UserAccount;
import com.wufivenull.blog.user.UserRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount account = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (account.getStatus().name().equals("DISABLED")) {
            throw new DisabledException("User is disabled");
        }
        return User.withUsername(account.getUsername())
                .password(account.getPasswordHash())
                .authorities(account.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getCode()))
                        .toList())
                .build();
    }
}
