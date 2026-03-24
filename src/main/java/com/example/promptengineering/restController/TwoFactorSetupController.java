package com.example.promptengineering.restController;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.TwoFactorEmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/2fa")
public class TwoFactorSetupController {

    private final TwoFactorEmailService twoFactorService;
    private final UserRepository userRepository;

    public TwoFactorSetupController(TwoFactorEmailService twoFactorService,
                                    UserRepository userRepository) {
        this.twoFactorService = twoFactorService;
        this.userRepository = userRepository;
    }

    private String getSessionId(User user) {
        return user.getId().toString();
    }

    @PostMapping("/send-test")
    public ResponseEntity<?> sendTestCode(@AuthenticationPrincipal User user,
                                          @RequestParam String email) {
        String sessionId = getSessionId(user);
        twoFactorService.createAndSendCode(sessionId, email);
        return ResponseEntity.ok(Map.of("message", "Kod wysłany na " + email));
    }

    @PostMapping("/enable")
    public ResponseEntity<?> enableTwoFactor(@AuthenticationPrincipal User user,
                                             @RequestParam String email,
                                             @RequestParam String code) {
        String sessionId = getSessionId(user);
        if (twoFactorService.verifyCode(sessionId, code)) {
            user.setTwoFactorEnabled(true);
            user.setTwoFactorEmail(email);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("success", true));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Nieprawidłowy kod"));
        }
    }

    @PostMapping("/disable")
    public ResponseEntity<?> disableTwoFactor(@AuthenticationPrincipal User user,
                                              @RequestParam String code) {
        String sessionId = getSessionId(user);
        if (twoFactorService.verifyCode(sessionId, code)) {
            user.setTwoFactorEnabled(false);
            user.setTwoFactorEmail(null);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("success", true));
        } else {
            return ResponseEntity.badRequest().body(Map.of("error", "Nieprawidłowy kod"));
        }
    }
}