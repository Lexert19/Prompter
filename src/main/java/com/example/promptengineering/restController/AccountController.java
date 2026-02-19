package com.example.promptengineering.restController;

import java.security.Principal;
import java.util.Map;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.PasswordChangeRequest;
import com.example.promptengineering.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.UserService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthService authService;

    @PostMapping("/save-key/{keyName}")
    public String saveKeyToMap(
            @AuthenticationPrincipal User user,
            @PathVariable String keyName,
            @RequestBody String keyValue) {
        userService.saveKeyToMap(user, keyName, keyValue);
        return String.format("Key '%s' saved to map for user with email: %s", keyName, user.getEmail());
    }

    @GetMapping("/keys")
    public Map<String, String> getAllKeys(
            @AuthenticationPrincipal User user) {
        return userService.getKeys(user);
    }


    @PostMapping("/change-password")
    public String changePassword(
            @AuthenticationPrincipal User user,
            @RequestBody PasswordChangeRequest request) throws Exception {

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Nowe hasło i potwierdzenie nie są identyczne");
        }

        authService.updatePassword(user, request.getNewPassword());
        return "Hasło zostało pomyślnie zmienione";
    }
}
