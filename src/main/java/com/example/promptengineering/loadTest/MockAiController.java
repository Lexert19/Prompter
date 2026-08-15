package com.example.promptengineering.loadTest;

import java.time.Duration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@Profile("loadtest")
@RequestMapping("/mock-ai")
public class MockAiController {

    @PostMapping(value = "/chat/completions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> stream() {
        return Flux.interval(Duration.ofMillis(40)).take(200)
                .map(i -> ServerSentEvent.<String>builder()
                        .data("{\"choices\":[{\"delta\":{\"content\":\"token" + i
                                + " \"}}]}")
                        .build())
                .concatWithValues(
                        ServerSentEvent.<String>builder().data("[DONE]").build());
    }
}
