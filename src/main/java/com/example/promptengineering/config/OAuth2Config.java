package com.example.promptengineering.config;

import com.example.promptengineering.component.CustomAuthenticationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.PortMapper;
import org.springframework.security.web.PortMapperImpl;
import org.springframework.security.web.PortResolverImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.util.Collections;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class OAuth2Config implements WebMvcConfigurer {
    @Autowired
    private CustomAuthenticationManager authenticationManager;

    @Bean
    public SecurityFilterChain securityWebFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.requiresChannel(channel -> channel.anyRequest().requiresInsecure());
        http.authorizeHttpRequests(exchanges -> exchanges
                .requestMatchers("/", "/login", "/error", "/static/**", "/auth/**", "/favicon.ico", "/favicon")
                .permitAll()
                .anyRequest().authenticated());

        http.oauth2Login(oauth2 -> oauth2
                .loginPage("/auth/login")
                .successHandler((request, response, authentication) -> {
                    response.sendRedirect("/");
                }));

        http.authenticationManager(authenticationManager);

        http.formLogin(customizer -> customizer
                .loginPage("/auth/login")
                .failureHandler(authenticationFailureHandler()));

        http.logout(customizer -> customizer.logoutUrl("/auth/logout"));

        return http.build();
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




}