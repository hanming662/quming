package com.aining.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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

    public String chatCompletion(String systemPrompt, String userPrompt) {
        Map<String, Object> body = buildRequestBody(systemPrompt, userPrompt, false);
        HttpURLConnection conn = null;
        try {
            conn = createConnection("/chat/completions");
            String jsonBody = JSON.toJSONString(body);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
            }
            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            String response = readAll(is);
            if (code < 200 || code >= 300) {
                log.error("AI API error, code={}, response={}", code, response);
                throw new RuntimeException("AI服务调用失败: " + response);
            }
            return extractContent(response);
        } catch (IOException e) {
            log.error("AI chat completion error", e);
            throw new RuntimeException("AI服务调用失败：" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public Flux<String> chatCompletionStream(String systemPrompt, String userPrompt) {
        Map<String, Object> body = buildRequestBody(systemPrompt, userPrompt, true);
        String jsonBody = JSON.toJSONString(body);
        return Flux.<String>create(sink -> {
            HttpURLConnection conn = null;
            try {
                log.info("AI stream request start, model={}", model);
                conn = createConnection("/chat/completions");
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
                if (code < 200 || code >= 300) {
                    String errorBody = readAll(is);
                    log.error("AI stream API error, code={}, response={}", code, errorBody);
                    sink.error(new RuntimeException("AI服务调用失败: " + errorBody));
                    return;
                }

                log.info("AI stream connection established, reading chunks...");
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null && !sink.isCancelled()) {
                        log.debug("AI raw line: {}", line);
                        if (line.startsWith("data:")) {
                            String data = line.substring(5).trim();
                            if ("[DONE]".equals(data)) {
                                log.info("AI stream [DONE] received");
                                sink.complete();
                                return;
                            }
                            String chunk = extractStreamContent(data);
                            if (chunk != null && !chunk.isEmpty()) {
                                log.debug("AI parsed chunk: {}", chunk);
                                sink.next(chunk);
                            }
                        }
                    }
                    log.info("AI stream read finished");
                    sink.complete();
                }
            } catch (Exception e) {
                log.error("AI stream connection error", e);
                sink.error(e);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private HttpURLConnection createConnection(String path) throws IOException {
        URL url = new URL(baseUrl + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        conn.setRequestProperty(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
        conn.setRequestProperty(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE);
        conn.setDoOutput(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(120000);
        return conn;
    }

    private String readAll(InputStream is) throws IOException {
        if (is == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    private Map<String, Object> buildRequestBody(String systemPrompt, String userPrompt, boolean stream) {
        List<Map<String, String>> messages = new java.util.ArrayList<>();
        Map<String, String> systemMsg = new java.util.HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);
        Map<String, String> userMsg = new java.util.HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userPrompt);
        messages.add(userMsg);
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("stream", stream);
        body.put("temperature", 0.8);
        return body;
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
                    String content = delta.getString("content");
                    return content != null ? content : "";
                }
            }
        } catch (Exception e) {
            log.error("Parse AI stream chunk error: {}", data);
        }
        return "";
    }
}
