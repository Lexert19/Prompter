package com.example.promptengineering.restController;

import com.example.promptengineering.dto.UserDto;
import com.example.promptengineering.entity.User;
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

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserDto> getOwnData(@AuthenticationPrincipal User user){
        UserDto userDto = userService.mapUserToDto(user);
        return ResponseEntity.ok(userDto);
    }

}
