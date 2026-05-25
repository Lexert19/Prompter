package com.example.promptengineering.restController;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.UserService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserTokenController {
    private final UserService userService;
    private final UserRepository userRepository;

    @PostMapping("/me/token")
    public Map<String, String> generateMyToken(@AuthenticationPrincipal User user) {
        String token = userService.rotateApiToken(user.getId());
        return Map.of("apiToken", token);
    }

    @GetMapping("/me/token")
    public Map<String, String> myToken(@AuthenticationPrincipal User user) {
        if (user.getApiToken() == null) {
            userService.generateApiToken(user);
        }
        return Map.of("apiToken", user.getApiToken());
    }
}
