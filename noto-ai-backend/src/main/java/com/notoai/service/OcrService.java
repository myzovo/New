package com.notoai.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Service
@Slf4j
public class OcrService {

    @Value("${notoai.ocr.api-key}")
    private String apiKey;

    @Value("${notoai.ocr.secret-key}")
    private String secretKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private String accessToken;

    /**
     * 识别图片中的文字
     */
    public String recognizeText(MultipartFile image) throws IOException {
        String token = getAccessToken();

        String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic"
                + "?access_token=" + token;

        String base64Image = Base64.getEncoder().encodeToString(image.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("image", base64Image);
        body.add("language_type", "CHN_ENG");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        return parseOcrResult(response.getBody());
    }

    private String parseOcrResult(String json) {
        JSONObject jsonObject = JSON.parseObject(json);
        JSONArray wordsResult = jsonObject.getJSONArray("words_result");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < wordsResult.size(); i++) {
            sb.append(wordsResult.getJSONObject(i).getString("words"));
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    private synchronized String getAccessToken() {
        if (accessToken == null) {
            String url = "https://aip.baidubce.com/oauth/2.0/token"
                    + "?grant_type=client_credentials"
                    + "&client_id=" + apiKey
                    + "&client_secret=" + secretKey;

            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
            JSONObject json = JSON.parseObject(response.getBody());
            accessToken = json.getString("access_token");
        }
        return accessToken;
    }
}
