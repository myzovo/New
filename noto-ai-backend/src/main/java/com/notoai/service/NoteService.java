package com.notoai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class NoteService {

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private VectorDbService vectorDbService;

    /**
     * 保存笔记并自动向量化
     */
    @Async("noteExecutor")
    public void processNote(String noteId, String title, String content, List<String> tags) {
        try {
            String textToEmbed = title + "\n\n" + content;
            List<Float> embedding = embeddingService.getEmbedding(textToEmbed);

            vectorDbService.storeEmbedding(noteId, title, content, tags, embedding);

            log.info("笔记 {} 向量化完成", noteId);
        } catch (Exception e) {
            log.error("笔记 {} 向量化失败", noteId, e);
        }
    }
}
