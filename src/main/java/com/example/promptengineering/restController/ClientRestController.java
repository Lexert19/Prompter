package com.example.promptengineering.restController;

import com.example.promptengineering.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.promptengineering.model.RequestBuilder;
import com.example.promptengineering.service.ChatService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.reflect.TypeToken;
import com.nimbusds.jose.shaded.gson.Gson;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/client")
public class ClientRestController {
    @Autowired
    private ChatService chatService;
    private Gson gson = new Gson();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @PostMapping("/chat")
    public SseEmitter makeRequest(
            @AuthenticationPrincipal User user,
            @RequestBody String body) throws JsonProcessingException {

        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        executor.execute(() -> {
            try {
                java.lang.reflect.Type requestType = new TypeToken<RequestBuilder>(){}.getType();
                RequestBuilder request = gson.fromJson(body, requestType);
                chatService.makeRequest(request, emitter);
            } catch (JsonProcessingException e) {
                try {
                    emitter.send("Error: Invalid request body format.");
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
            } catch (Exception e) {
                try {
                    emitter.send("Error: An unexpected error occurred.");
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
            }
        });

        return emitter;

    }

}
