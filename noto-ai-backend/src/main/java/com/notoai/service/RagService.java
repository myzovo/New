package com.notoai.service;

import com.notoai.model.dto.ChatResponse;
import com.notoai.model.dto.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RagService {

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private VectorDbService vectorDbService;

    @Autowired
    private LlmService llmService;

    private static final String SYSTEM_PROMPT = """
        你是一个智能笔记助手。你的任务是基于用户的笔记内容回答问题。

        规则：
        1. 只基于提供的笔记内容回答问题
        2. 如果笔记中没有相关信息，如实说明"您的笔记中没有找到相关信息"
        3. 回答要简洁清晰，必要时可以引用原文
        4. 在回答末尾标注参考来源
        """;

    /**
     * 基于笔记内容回答问题
     */
    public ChatResponse answerQuestion(String question) {
        List<Float> questionEmbedding = embeddingService.getEmbedding(question);

        List<SearchResult> relevantNotes = vectorDbService.searchSimilar(questionEmbedding, 5);

        if (relevantNotes.isEmpty()) {
            ChatResponse response = new ChatResponse();
            response.setAnswer("您的笔记中没有找到与问题相关的内容。");
            response.setSources(List.of());
            return response;
        }

        String context = buildContext(relevantNotes);

        String userMessage = String.format("""
            【相关笔记内容】
            %s

            【用户问题】
            %s

            请基于以上笔记内容回答问题。
            """, context, question);

        String answer = llmService.chat(SYSTEM_PROMPT, userMessage);

        ChatResponse response = new ChatResponse();
        response.setAnswer(answer);
        response.setSources(relevantNotes.stream()
            .map(r -> new ChatResponse.Source(r.getNoteId(), r.getTitle(), r.getSimilarity()))
            .collect(Collectors.toList()));

        return response;
    }

    private String buildContext(List<SearchResult> notes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < notes.size(); i++) {
            SearchResult note = notes.get(i);
            sb.append(String.format("笔记%d（相似度: %.2f）\n标题: %s\n内容:\n%s\n",
                i + 1, note.getSimilarity(), note.getTitle(), note.getContent()));
            if (i < notes.size() - 1) {
                sb.append("---\n");
            }
        }
        return sb.toString();
    }
}
