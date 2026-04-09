package com.example.promptengineering.restController;

import com.example.promptengineering.component.UserActionLimiter;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.security.IpRateLimiter;
import com.example.promptengineering.service.TwoFactorEmailService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/2fa")
public class TwoFactorSetupController {

    private final TwoFactorEmailService twoFactorService;
    private final UserRepository userRepository;
    private final IpRateLimiter rateLimiter;
    private final UserActionLimiter userActionLimiter;

    public TwoFactorSetupController(TwoFactorEmailService twoFactorService,
            UserRepository userRepository, IpRateLimiter rateLimiter,
            UserActionLimiter userActionLimiter) {
        this.twoFactorService = twoFactorService;
        this.userRepository = userRepository;
        this.rateLimiter = rateLimiter;
        this.userActionLimiter = userActionLimiter;
    }

    private String getSessionId(User user) {
        return user.getId().toString();
    }

    @PostMapping("/send-test")
    public ResponseEntity<?> sendTestCode(@AuthenticationPrincipal User user,
                                          @RequestParam String email,
                                          HttpServletRequest request) {
        if (!rateLimiter.isAllowed(request)) {
            return ResponseEntity.status(429)
                    .body("Too many requests. Please try again later.");
        }
        if (!userActionLimiter.canPerform(user)) {
            return ResponseEntity.status(429).body("Too many requests");
        }
        String sessionId = getSessionId(user);
        twoFactorService.createAndSendCode(sessionId, email);
        return ResponseEntity.ok(Map.of("message", "Kod wysłany na " + email));
    }

    @PostMapping("/enable")
    public ResponseEntity<?> enableTwoFactor(@AuthenticationPrincipal User user,
                                             @RequestParam String email,
                                             @RequestParam String code,
                                             HttpServletRequest request) {
        if (!rateLimiter.isAllowed(request)) {
            return ResponseEntity.status(429)
                    .body("Too many requests. Please try again later.");
        }
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
