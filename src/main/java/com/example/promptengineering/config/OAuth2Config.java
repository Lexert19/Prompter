package com.example.promptengineering.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@EnableWebFluxSecurity
public class OAuth2Config {
        @Bean
        public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
                http.csrf(csrf -> csrf.disable());

                http.authorizeExchange(exchanges -> exchanges
                                .pathMatchers("/", "/login", "/error", "/static/**", "/auth/**", "/favicon.ico")
                                .permitAll()
                                .anyExchange().authenticated());

                http.oauth2Login(oauth2 -> oauth2
                                .authenticationSuccessHandler(new SuccessHandler()));

                return http.build();
        }

        @Bean
        public RouterFunction<ServerResponse> faviconRouter() {
                return RouterFunctions.route(
                                RequestPredicates.GET("/favicon.ico"),
                                request -> ServerResponse.ok()
                                                .contentType(MediaType.valueOf("image/svg+xml"))
                                                .bodyValue(new ClassPathResource("static/favicon.ico")));
        }

}