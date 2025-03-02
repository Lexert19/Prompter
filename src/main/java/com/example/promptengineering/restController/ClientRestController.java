package com.example.promptengineering.restController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.promptengineering.entity.User;
import com.example.promptengineering.model.RequestBuilder;
import com.example.promptengineering.repository.RequestHistoryRepository;
import com.example.promptengineering.service.ChatService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.reflect.TypeToken;
import com.nimbusds.jose.shaded.gson.Gson;

import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/client")
public class ClientRestController {
    @Autowired
    private ChatService chatService;
    @Autowired
    private RequestHistoryRepository requestHistoryRepository;
    private final Gson gson = new Gson();

    @PostMapping("/chat")
    public Flux<String> makeRequest(
            @AuthenticationPrincipal OAuth2User oAuth2User,
            @RequestBody String body) throws JsonProcessingException {

        try {
            java.lang.reflect.Type requestType = new TypeToken<RequestBuilder>(){}.getType();
            RequestBuilder request = gson.fromJson(body, requestType);
            //User user = (User) oAuth2User;
            requestHistoryRepository.save(request);

            return chatService.makeRequest(request);
        } catch (JsonProcessingException e) {
            return Flux.just("Error: Invalid request body format.");
        } catch (Exception e) {
            return Flux.just("Error: An unexpected error occurred.");
        }

    }

}
