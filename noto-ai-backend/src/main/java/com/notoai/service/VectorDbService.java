package com.notoai.service;

import com.notoai.model.dto.SearchResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VectorDbService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 存储笔记向量
     */
    public void storeEmbedding(String noteId, String title, String content,
                               List<String> tags, List<Float> embedding) {
        String sql = """
            INSERT INTO note_embeddings (note_id, title, content, tags, embedding)
            VALUES (?, ?, ?, ?, ?::vector)
            ON CONFLICT (note_id) DO UPDATE SET
                title = EXCLUDED.title,
                content = EXCLUDED.content,
                tags = EXCLUDED.tags,
                embedding = EXCLUDED.embedding,
                updated_at = NOW()
            """;

        String embeddingStr = embedding.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(",", "[", "]"));

        // 使用PostgreSQL数组语法
        String[] tagsArray = tags.toArray(new String[0]);

        jdbcTemplate.update(sql, noteId, title, content, tagsArray, embeddingStr);
    }

    /**
     * 检索相似笔记
     */
    public List<SearchResult> searchSimilar(List<Float> queryEmbedding, int topK) {
        String embeddingStr = queryEmbedding.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(",", "[", "]"));

        String sql = """
            SELECT note_id, title, content, similarity
            FROM match_notes(?::vector, ?)
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            SearchResult result = new SearchResult();
            result.setNoteId(rs.getString("note_id"));
            result.setTitle(rs.getString("title"));
            result.setContent(rs.getString("content"));
            result.setSimilarity(rs.getFloat("similarity"));
            return result;
        }, embeddingStr, topK);
    }
}
