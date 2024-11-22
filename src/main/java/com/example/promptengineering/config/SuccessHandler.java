package com.example.promptengineering.config;

import java.time.Duration;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;

import reactor.core.publisher.Mono;

public class SuccessHandler extends RedirectServerAuthenticationSuccessHandler {

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        webFilterExchange.getExchange().getSession().subscribe(session->session.setMaxIdleTime(Duration.ofMinutes(-1)));
        return super.onAuthenticationSuccess(webFilterExchange, authentication);
    }
}
