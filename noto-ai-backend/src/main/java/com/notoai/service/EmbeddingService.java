package com.notoai.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class EmbeddingService {

    @Value("${notoai.embedding.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 获取文本的向量表示
     */
    public List<Float> getEmbedding(String text) {
        String url = "https://open.bigmodel.cn/api/paas/v4/embeddings";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        Map<String, Object> body = Map.of(
            "model", "embedding-3",
            "input", text
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
            url, request, String.class
        );

        return parseEmbedding(response.getBody());
    }

    private List<Float> parseEmbedding(String json) {
        JSONObject jsonObject = JSON.parseObject(json);
        JSONArray data = jsonObject.getJSONArray("data");
        JSONArray embedding = data.getJSONObject(0).getJSONArray("embedding");

        List<Float> result = new ArrayList<>();
        for (int i = 0; i < embedding.size(); i++) {
            result.add(embedding.getFloat(i));
        }
        return result;
    }
}
