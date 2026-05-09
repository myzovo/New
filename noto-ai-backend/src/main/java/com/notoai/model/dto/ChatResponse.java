package com.notoai.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatResponse {
    private String answer;
    private List<Source> sources;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Source {
        private String noteId;
        private String title;
        private float similarity;
    }
}
