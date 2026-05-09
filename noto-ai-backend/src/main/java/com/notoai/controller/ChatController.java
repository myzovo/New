package com.notoai.controller;

import com.notoai.model.dto.ChatRequest;
import com.notoai.model.dto.ChatResponse;
import com.notoai.service.RagService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@Slf4j
public class ChatController {

    @Autowired
    private RagService ragService;

    @PostMapping("/ask")
    public ResponseEntity<ChatResponse> askQuestion(@Valid @RequestBody ChatRequest request) {
        try {
            ChatResponse response = ragService.answerQuestion(request.getQuestion());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("问答失败", e);

            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setAnswer("抱歉，处理您的问题时出现错误，请稍后再试。");
            errorResponse.setSources(List.of());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
