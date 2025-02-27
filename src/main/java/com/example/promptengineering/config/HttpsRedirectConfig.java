package com.example.promptengineering.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.WebFilter;
import org.springframework.web.util.UriComponentsBuilder;

import reactor.core.publisher.Mono;

import java.net.URI;

@Configuration
public class HttpsRedirectConfig {
    
    @Bean
    public WebFilter httpsRedirectFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            if (request.getURI().getScheme().equals("http")) {
                URI httpsUri = UriComponentsBuilder
                    .fromUri(request.getURI())
                    .scheme("https")
                    .port(8080)
                    .build().toUri();
                
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
                response.getHeaders().setLocation(httpsUri);
                return Mono.empty();
            }
            return chain.filter(exchange);
        };
    }
}