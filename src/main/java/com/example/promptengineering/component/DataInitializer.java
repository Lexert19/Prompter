package com.example.promptengineering.component;

import com.example.promptengineering.entity.MigrationFlag;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.AppRole;
import com.example.promptengineering.repository.MigrationFlagRepository;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.ModelService;
import com.example.promptengineering.service.SharedKeyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;
    private final ModelService modelService;
    private final String geminiApiKey;
    private final MigrationFlagRepository migrationFlagRepository;
    private final SharedKeyService sharedKeyService;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
            @Value("${admin.email}") String adminEmail,
            @Value("${admin.password}") String adminPassword, ModelService modelService,
            @Value("${gemini.api.key:}") String geminiApiKey,
            MigrationFlagRepository migrationFlagRepository,
            SharedKeyService sharedKeyService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.modelService = modelService;
        this.geminiApiKey = geminiApiKey;
        this.migrationFlagRepository = migrationFlagRepository;
        this.sharedKeyService = sharedKeyService;
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

        modelService.loadDefaultModelsFromJson();

        addGeminiSharedKeyIfNeeded(admin);
    }

    private void addGeminiSharedKeyIfNeeded(User user) {
        Optional<MigrationFlag> flag = migrationFlagRepository
                .findByName("gemini_shared_key_added");
        if (flag.isPresent() && flag.get().isExecuted()) {
            return;
        }

        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            sharedKeyService.addKey("GEMINI", geminiApiKey, user);

            MigrationFlag newFlag = flag
                    .orElse(new MigrationFlag("gemini_shared_key_added"));
            newFlag.setExecuted(true);
            newFlag.setExecutedAt(LocalDateTime.now());
            migrationFlagRepository.save(newFlag);
        }
    }
}
