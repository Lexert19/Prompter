package com.example.promptengineering.component;

import com.example.promptengineering.repository.UserRepository;
import com.example.promptengineering.service.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationProvider;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class CustomAuthenticationManager implements AuthenticationManager {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final ProviderManager providerManager;

    @Autowired
    public CustomAuthenticationManager(UserRepository userRepository,
                                       PasswordEncoder passwordEncoder,
                                       UserDetailsService userDetailsService,
                                       CustomOAuth2UserService customOAuth2UserService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.customOAuth2UserService = customOAuth2UserService;

        OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient =
                new DefaultAuthorizationCodeTokenResponseClient();

        OAuth2LoginAuthenticationProvider oauth2LoginAuthenticationProvider =
                new OAuth2LoginAuthenticationProvider(accessTokenResponseClient, customOAuth2UserService);

        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);

        this.providerManager = new ProviderManager(Arrays.asList(
                daoAuthenticationProvider,
                oauth2LoginAuthenticationProvider
        ));
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            return providerManager.authenticate(authentication);
        } catch (Exception e) {
            System.out.println("Authentication error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}