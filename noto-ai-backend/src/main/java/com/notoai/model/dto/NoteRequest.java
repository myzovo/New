package com.notoai.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class NoteRequest {
    @NotBlank
    private String noteId;

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private List<String> tags;
}
