package com.example.promptengineering.service;

import com.example.promptengineering.exception.UserAlreadyExistsException;
import com.example.promptengineering.model.AppRole;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.promptengineering.repository.UserRepository;

import java.util.List;

@Service
public class CustomOAuth2UserService
        implements
            OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private final UserRepository userRepository;
    private final UserService userService;

    public CustomOAuth2UserService(UserRepository userRepository,
            UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");

        return userRepository.findByEmail(email).orElseGet(() -> {
            try {
                return userService.createUser(email, List.of(AppRole.USER));
            } catch (UserAlreadyExistsException e) {
                throw new RuntimeException(e);
            }
        });

    }

}
