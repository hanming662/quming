package com.aining.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OpenAiClient {

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.base-url}")
    private String baseUrl;

    @Value("${ai.model}")
    private String model;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String chatCompletion(String systemPrompt, String userPrompt) {
        Map<String, Object> body = buildRequestBody(systemPrompt, userPrompt, false);
        try {
            String response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return extractContent(response);
        } catch (Exception e) {
            log.error("AI chat completion error", e);
            throw new RuntimeException("AI服务调用失败：" + e.getMessage());
        }
    }

    public Flux<String> chatCompletionStream(String systemPrompt, String userPrompt) {
        Map<String, Object> body = buildRequestBody(systemPrompt, userPrompt, true);
        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)
                .filter(line -> line.startsWith("data:"))
                .map(line -> line.substring(5).trim())
                .filter(line -> !"[DONE]".equals(line))
                .map(this::extractStreamContent);
    }

    private Map<String, Object> buildRequestBody(String systemPrompt, String userPrompt, boolean stream) {
        List<Map<String, String>> messages = java.util.Arrays.asList(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)
        );
        return Map.of(
                "model", model,
                "messages", messages,
                "stream", stream,
                "temperature", 0.8
        );
    }

    private String extractContent(String responseJson) {
        try {
            JSONObject json = JSON.parseObject(responseJson);
            JSONArray choices = json.getJSONArray("choices");
            if (choices != null && !choices.isEmpty()) {
                return choices.getJSONObject(0).getJSONObject("message").getString("content");
            }
        } catch (Exception e) {
            log.error("Parse AI response error: {}", responseJson);
        }
        return "";
    }

    private String extractStreamContent(String data) {
        try {
            JSONObject json = JSON.parseObject(data);
            JSONArray choices = json.getJSONArray("choices");
            if (choices != null && !choices.isEmpty()) {
                JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                if (delta != null) {
                    return delta.getString("content");
                }
            }
        } catch (Exception e) {
            log.error("Parse AI stream chunk error: {}", data);
        }
        return "";
    }
}
