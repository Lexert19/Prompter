package com.example.promptengineering.restController;

import java.util.Map;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.PasswordChangeRequest;
import com.example.promptengineering.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.promptengineering.service.UserService;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final UserService userService;
    private final AuthService authService;

    public AccountController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/save-key/{keyName}")
    public String saveKeyToMap(@AuthenticationPrincipal User user, @PathVariable String keyName,
            @RequestBody String keyValue) {
        userService.appendKeyToMap(user, keyName, keyValue);
        return String.format("Key '%s' saved to map for user with email: %s", keyName, user.getEmail());
    }

    @GetMapping("/keys")
    public Map<String, String> getAllKeys(@AuthenticationPrincipal User user) {
        return userService.getUserKeys(user);
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@AuthenticationPrincipal User user,
            @RequestBody PasswordChangeRequest request) {

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("New password and confirmation do not match");
        }

        authService.updatePassword(user, request.getNewPassword());
        return ResponseEntity.ok("Password has been changed successfully");
    }
}
