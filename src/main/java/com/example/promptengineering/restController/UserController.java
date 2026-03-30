package com.example.promptengineering.restController;

import com.example.promptengineering.dto.UserDto;
import com.example.promptengineering.entity.User;
import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<UserDto> getOwnData(@AuthenticationPrincipal User user) {
        User freshUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserDto userDto = userService.mapUserToDto(freshUser);
        userDto.setTwoFactorEnabled(freshUser.isTwoFactorEnabled());
        userDto.setTwoFactorEmail(freshUser.getTwoFactorEmail());
        return ResponseEntity.ok(userDto);
    }

}
