package com.example.promptengineering.restController;

import java.security.Principal;
import java.util.Map;

import com.example.promptengineering.model.PasswordChangeRequest;
import com.example.promptengineering.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.UserService;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/account")
public class AccountController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthService authService;

    @PostMapping("/save-key/{keyName}")
    public Mono<String> saveKeyToMap(
            @AuthenticationPrincipal Principal oAuth2User,
            @PathVariable String keyName,
            @RequestBody String keyValue) {

        String userEmail = oAuth2User.getName();
        if (userEmail == null) {
            return Mono.error(new IllegalArgumentException("Email not found in OAuth2User attributes"));
        }

        return userService.findUserByEmail(userEmail)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found for email: " + userEmail)))
                .flatMap(existingUser -> userService.saveKeyToMap(existingUser, keyName, keyValue))
                .map(userSaved -> String.format("Key '%s' saved to map for user with email: %s", keyName, userEmail));

    }

    @GetMapping("/keys")
    public Mono<Map<String, String>> getAllKeys(
            @AuthenticationPrincipal Principal oAuth2User) {
        String userEmail = oAuth2User.getName();
        if (userEmail == null) {
            return Mono.error(new IllegalArgumentException("Email not found in OAuth2User attributes"));
        }

        return userRepository.findByEmail(userEmail)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found in repository for email: " + userEmail)))
                .flatMap(user -> userService.getKeys(user));
    }


    @PostMapping("/change-password")
    public Mono<String> changePassword(
            @AuthenticationPrincipal Principal oAuth2User,
            @RequestBody PasswordChangeRequest request) {

        String userEmail = oAuth2User.getName();
        if (userEmail == null) {
            return Mono.error(new IllegalArgumentException("Nie znaleziono adresu email w atrybutach użytkownika OAuth2"));
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return Mono.error(new IllegalArgumentException("Nowe hasło i potwierdzenie nie są identyczne"));
        }

        return authService.updatePassword(userEmail, request.getNewPassword())
                .thenReturn("Hasło zostało pomyślnie zmienione");
    }
}
