package com.example.promptengineering.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity.OAuth2ResourceServerSpec;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;

import reactor.core.publisher.Mono;

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

                http.oauth2Login(Customizer.withDefaults());



                // http.csrf(csrf -> csrf.disable()
                //                 .authorizeExchange(exchanges -> exchanges
                //                                 .pathMatchers("/", "/login", "/error", "/static/**", "/auth/**")
                //                                 .permitAll()
                //                                 .anyExchange().authenticated())
                //                 .oauth2Login(Customizer.withDefaults())
                //                 .sessionManagement(sessionManagement -> sessionManagement
                //                                 .sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
                // .exceptionHandling(handling -> handling
                // .authenticationEntryPoint((exchange, ex) -> {
                // ServerHttpResponse response = exchange.getResponse();
                // response.setStatusCode(HttpStatus.FOUND);
                // response.getHeaders().setLocation(URI.create("/auth/login"));
                // return response.setComplete();
                // }))
                //);

                return http.build();
        }

}