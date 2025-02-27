package com.example.promptengineering.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class OAuth2Config {

        @Value("${spring.security.oauth2.client.registration.google.client-id}")
        private String clientId;

        @Value("${spring.security.oauth2.client.registration.google.client-secret}")
        private String clientSecret;

        @Bean
        public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
                http.csrf(csrf -> csrf.disable());

                http.authorizeExchange(exchanges -> exchanges
                                .pathMatchers("/", "/login", "/error", "/static/**", "/auth/**").permitAll()
                                .anyExchange().authenticated());

                http.oauth2Login(oauth2 -> oauth2
                                .authenticationSuccessHandler(new SuccessHandler()));

                //http.httpBasic(withDefaults());
                return http.build();
        }

}