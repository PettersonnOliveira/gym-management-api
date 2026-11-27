package com.portfolio.Gym.Management.API.Security;

import com.portfolio.Gym.Management.API.Entities.User;
import com.portfolio.Gym.Management.API.Repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Locale;

@Component
public class AdminSeedConfig implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminSeedConfig.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminSeedConfig(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${admin.email:admin@sistema.com}") String adminEmail,
            @Value("${admin.password:}") String adminPassword
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (!StringUtils.hasText(adminPassword)) {
            log.info("ADMIN_PASSWORD não configurada; seed de administrador ignorado.");
            return;
        }

        String email = adminEmail.trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            log.info("Usuário admin {} já existe; seed ignorado.", email);
            return;
        }

        userRepository.save(new User(
                email,
                passwordEncoder.encode(adminPassword),
                User.Role.ADMIN
        ));
        log.info("Usuário admin {} criado com sucesso.", email);
    }
}
