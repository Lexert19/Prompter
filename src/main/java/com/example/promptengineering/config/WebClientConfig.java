package com.example.promptengineering.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class WebClientConfig {

//    @Bean
//    public WebClient webClient() {
//        HttpClient httpClient = HttpClient.create()
//                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 300000)
//                .option(ChannelOption.SO_KEEPALIVE, true)
//                .option(ChannelOption.TCP_NODELAY, true)
//                .doOnConnected(conn -> conn
//                        .addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))
//                        .addHandlerLast(new WriteTimeoutHandler(300, TimeUnit.SECONDS)))
//                .responseTimeout(Duration.ofSeconds(300))
//                .keepAlive(true);
//
//        return WebClient.builder()
//                .clientConnector(new ReactorClientHttpConnector(httpClient))
//                .build();
//    }
//
//    @Bean
//    public Sinks.Many<String> sink() {
//        return Sinks.many().replay().latest();
//    }
//
//    @Bean
//    public Flux<String> flux(Sinks.Many<String> sink){
//        return sink.asFlux();
//    }
}
