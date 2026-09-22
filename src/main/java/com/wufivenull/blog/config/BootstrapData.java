package com.wufivenull.blog.config;

import com.wufivenull.blog.user.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BootstrapData implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    public BootstrapData(RoleRepository roleRepository, UserRepository userRepository,
                         PasswordEncoder passwordEncoder, Environment environment) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.environment = environment;
    }

    @Override
    public void run(String... args) {
        Role admin = role(RoleCode.ADMIN, "管理员");
        Role owner = role(RoleCode.OWNER, "站长");
        Role user = role(RoleCode.USER, "普通用户");
        if (!Boolean.parseBoolean(environment.getProperty("app.seed.enabled", "false"))) {
            return;
        }
        seedUser("app.seed.admin-username", "app.seed.admin-password", admin);
        seedUser("app.seed.owner-username", "app.seed.owner-password", owner);
        seedUser("app.seed.user-username", "app.seed.user-password", user);
    }

    private Role role(RoleCode code, String name) {
        return roleRepository.findByCode(code).orElseGet(() -> roleRepository.save(new Role(code, name)));
    }

    private void seedUser(String usernameKey, String passwordKey, Role role) {
        String username = environment.getProperty(usernameKey);
        String password = environment.getProperty(passwordKey);
        if (username == null || password == null || password.isBlank()
                || userRepository.findByUsername(username).isPresent()) {
            return;
        }
        UserAccount account = new UserAccount(username, passwordEncoder.encode(password), username);
        account.getRoles().add(role);
        userRepository.save(account);
    }
}
