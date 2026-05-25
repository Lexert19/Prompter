package com.example.promptengineering.restController;

import com.example.promptengineering.service.UserService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserTokenController {

    private final UserService userService;

    @PostMapping("/{id}/token")
    public Map<String, String> generateForUser(@PathVariable Long id) {
        String token = userService.rotateApiToken(id);
        return Map.of("apiToken", token);
    }
}
