package com.example.promptengineering.component;

import com.example.promptengineering.entity.MigrationFlag;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.AppRole;
import com.example.promptengineering.repository.MigrationFlagRepository;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.ModelService;
import com.example.promptengineering.service.SharedKeyService;
import com.example.promptengineering.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Autowired
    private ModelService modelService;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Autowired
    private MigrationFlagRepository migrationFlagRepository;

    @Autowired
    private SharedKeyService sharedKeyService;

    @Override
    public void run(String... args) throws Exception {
        User admin = userRepository.findByEmail(adminEmail).orElse(null);
        if (admin == null) {
            User newAdmin = new User();
            newAdmin.setEmail(adminEmail);
            newAdmin.setPassword(passwordEncoder.encode(adminPassword));
            newAdmin.setRoles(List.of(AppRole.ADMIN));
            admin = userRepository.save(newAdmin);
            log.info("Admin created: {}", adminEmail);
        } else {
            log.debug("Admin already exists: {}", adminEmail);
        }

        modelService.loadDefaultModelsFromJson();


        addGeminiSharedKeyIfNeeded(admin);
    }

    private void addGeminiSharedKeyIfNeeded(User user) {
        Optional<MigrationFlag> flag = migrationFlagRepository.findByName("gemini_shared_key_added");
        if (flag.isPresent() && flag.get().isExecuted()) {
            return;
        }

        if (geminiApiKey != null && !geminiApiKey.isBlank()) {
            sharedKeyService.addKey("GEMINI", geminiApiKey, user);

            MigrationFlag newFlag = flag.orElse(new MigrationFlag("gemini_shared_key_added"));
            newFlag.setExecuted(true);
            newFlag.setExecutedAt(LocalDateTime.now());
            migrationFlagRepository.save(newFlag);
        }
    }
}
