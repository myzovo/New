package com.notoai.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class LlmService {

    @Value("${notoai.llm.api-key}")
    private String apiKey;

    @Value("${notoai.llm.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 调用LLM生成回答
     */
    public String chat(String systemPrompt, String userMessage) {
        String url = "https://api.deepseek.com/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> body = Map.of(
            "model", model,
            "messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userMessage)
            ),
            "temperature", 0.7,
            "max_tokens", 2000
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
            url, request, String.class
        );

        return parseResponse(response.getBody());
    }

    private String parseResponse(String json) {
        JSONObject jsonObject = JSON.parseObject(json);
        JSONArray choices = jsonObject.getJSONArray("choices");
        return choices.getJSONObject(0)
            .getJSONObject("message")
            .getString("content");
    }
}
