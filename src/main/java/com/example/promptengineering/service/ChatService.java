package com.example.promptengineering.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.example.promptengineering.model.RequestBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jose.shaded.gson.Gson;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

@Service
public class ChatService {
    private final Gson gson = new Gson();

    public void makeRequest(RequestBuilder request, SseEmitter emitter) throws JsonProcessingException {
        String requestBodyJson = gson.toJson(request.build());
        try {
            URL url = new URL(request.getUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(300000);
            connection.setReadTimeout(0);
            connection.setRequestMethod("POST");
            configureHeaders(connection, request);
            connection.setDoOutput(true);

            connection.getOutputStream().write(requestBodyJson.getBytes());

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        if (inputLine.startsWith("data: ")) {
                            String json = inputLine.substring(6);
                            emitter.send(SseEmitter.event().data(json, MediaType.APPLICATION_JSON));
                        } else {
                            emitter.send(SseEmitter.event().data(inputLine, MediaType.APPLICATION_JSON));
                        }
                    }
                }
            } else {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                    StringBuilder content = new StringBuilder();
                    while ((in.readLine()) != null) {
                        content.append(in.readLine());
                    }
                    String jsonError = gson.toJson(Collections.singletonMap("error", content.toString()));
                    emitter.send(SseEmitter.event()
                            .data(jsonError, MediaType.APPLICATION_JSON));
                    //emitter.send(jsonError);
                }
            }
            emitter.complete();
        } catch (IOException e) {
            try {
                emitter.send("Error: " + e.getMessage());
            } catch (IOException ex) {
                emitter.completeWithError(ex);
            }
        }
    }

    private void configureHeaders(HttpURLConnection connection, RequestBuilder request) {
        if (request.getProvider().equals("ANTHROPIC")) {
            connection.setRequestProperty("x-api-key", request.getKey());
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("anthropic-version", "2023-06-01");
            connection.setRequestProperty("anthropic-beta", "prompt-caching-2024-07-31");
        } else {
            connection.setRequestProperty("Authorization", "Bearer " + request.getKey());
            connection.setRequestProperty("Content-Type", "application/json");
        }
    }

}
