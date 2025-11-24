package com.example.promptengineering.restController;

import com.example.promptengineering.entity.User;
import com.google.gson.JsonSyntaxException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.promptengineering.model.RequestBuilder;
import com.example.promptengineering.service.ChatService;
import com.nimbusds.jose.shaded.gson.Gson;

import reactor.core.publisher.Flux;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/client")
public class ClientRestController {
    @Autowired
    private ChatService chatService;
    private Gson gson = new Gson();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(
            @AuthenticationPrincipal User user,
            @RequestBody String body) {

        try {
            RequestBuilder request = gson.fromJson(body, RequestBuilder.class);
            return chatService.makeRequest(request);
        } catch (JsonSyntaxException e) {
            return Flux.just(ServerSentEvent.<String>builder()
                    .event("error")
                    .data("{\"error\": \"Invalid JSON format\"}")
                    .build());
        } catch (Exception e) {
            return Flux.just(ServerSentEvent.<String>builder()
                    .event("error")
                    .data("{\"error\": \"Unexpected error\"}")
                    .build());
        }
    }

}
