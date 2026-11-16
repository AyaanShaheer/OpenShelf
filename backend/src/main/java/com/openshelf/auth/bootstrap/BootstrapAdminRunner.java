package com.openshelf.auth.bootstrap;

import com.openshelf.auth.domain.Role;
import com.openshelf.auth.domain.User;
import com.openshelf.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class BootstrapAdminRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapAdminRunner.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean enabled;
    private final String email;
    private final String password;
    private final String fullName;

    public BootstrapAdminRunner(UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                @Value("${app.bootstrap-admin.enabled:false}") boolean enabled,
                                @Value("${app.bootstrap-admin.email:}") String email,
                                @Value("${app.bootstrap-admin.password:}") String password,
                                @Value("${app.bootstrap-admin.full-name:System Administrator}") String fullName) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            log.warn("Bootstrap admin is enabled but email/password are not configured; skipping");
            return;
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            log.info("Bootstrap admin user already exists; skipping creation");
            return;
        }
        User user = new User();
        user.setFullName(StringUtils.hasText(fullName) ? fullName : "System Administrator");
        user.setEmail(email.trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(Role.ADMIN);
        user.setActive(true);
        userRepository.save(user);
        log.info("Bootstrap admin user created (development bootstrap)");
    }
}
