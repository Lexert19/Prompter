package com.example.promptengineering.config;

import com.example.promptengineering.component.CustomAuthenticationEntryPoint;
import com.example.promptengineering.component.CustomAuthenticationManager;
import com.example.promptengineering.service.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class OAuth2Config implements WebMvcConfigurer   {
    @Autowired
    private CustomAuthenticationManager authenticationManager;
    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public SecurityFilterChain securityWebFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http.csrf(csrf -> csrf.disable());
        //http.requiresChannel(channel -> channel.anyRequest().requiresInsecure());
        http.authorizeHttpRequests(exchanges -> exchanges
                .requestMatchers("/", "/login", "/error", "/static/**", "/auth/**", "/favicon.ico", "/favicon")
                .permitAll()
                .anyRequest().authenticated());

        http.oauth2Login(oauth2 -> oauth2
                .loginPage("/auth/login")
                .userInfoEndpoint(userInfo -> userInfo
                        .oidcUserService(oidcUserService())
                        .userService(customOAuth2UserService))
                .successHandler((request, response, authentication) -> {
                    String serverName = request.getServerName();
                    response.sendRedirect("http://" + serverName + ":8080/");
                }));

        http.authenticationManager(authenticationManager);

        http.formLogin(customizer -> customizer
                .loginPage("/auth/login")
                .failureHandler(authenticationFailureHandler()));

        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(authenticationEntryPoint())
        );
        http.logout(customizer -> customizer.logoutUrl("/auth/logout"));
        return http.build();
    }

    @Bean
    public OidcUserService oidcUserService() {
        return new OidcUserService();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            response.sendRedirect("/auth/login?error=true");
        };
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return filter;
    }


    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint("/auth/login");
    }
}