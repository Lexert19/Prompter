package com.example.promptengineering.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
public class OAuth2Config {
    @Autowired
    private ReactiveAuthenticationManager reactiveAuthenticationManager;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http.csrf(csrf -> csrf.disable());

        http.authorizeExchange(exchanges -> exchanges
                .pathMatchers("/", "/login", "/error", "/static/**", "/auth/**", "/favicon.ico")
                .permitAll()
                .anyExchange().authenticated());

        http.oauth2Login(oauth2 -> oauth2
                .authenticationSuccessHandler(new SuccessHandler()));

        http.authenticationManager(reactiveAuthenticationManager);

        http.formLogin(customizer -> customizer
                .loginPage("/auth/login")
                .authenticationFailureHandler((exchange, exception) -> {
                    return Mono.fromRunnable(() -> {
                        exchange.getExchange().getResponse().setStatusCode(HttpStatus.FOUND);
                        exchange.getExchange().getResponse().getHeaders().setLocation(URI.create("/auth/login?error=true"));
                    });
                }));

        http.logout(customizer -> customizer.logoutUrl("/auth/logout"));

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