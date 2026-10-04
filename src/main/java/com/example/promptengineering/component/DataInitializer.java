package com.example.promptengineering.component;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.AppRole;
import com.example.promptengineering.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
            @Value("${admin.email}") String adminEmail,
            @Value("${admin.password}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) throws Exception {
        User admin = userRepository.findByEmail(adminEmail).orElse(null);
        if (admin == null) {
            User newAdmin = new User();
            newAdmin.setEmail(adminEmail);
            newAdmin.setPassword(passwordEncoder.encode(adminPassword));
            newAdmin.setRoles(List.of(AppRole.ADMIN));
            newAdmin.setTwoFactorEnabled(true);
            newAdmin.setTwoFactorEmail(adminEmail);
            admin = userRepository.save(newAdmin);
            log.info("Admin created: {}", adminEmail);
        } else {
            log.debug("Admin already exists: {}", adminEmail);
        }

    }
}
