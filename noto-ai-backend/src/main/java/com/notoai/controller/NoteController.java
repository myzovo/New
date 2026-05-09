package com.notoai.controller;

import com.notoai.model.dto.NoteRequest;
import com.notoai.model.dto.NoteResponse;
import com.notoai.service.NoteService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notes")
@Slf4j
public class NoteController {

    @Autowired
    private NoteService noteService;

    @PostMapping("/save")
    public ResponseEntity<NoteResponse> saveNote(@Valid @RequestBody NoteRequest request) {
        try {
            noteService.processNote(
                request.getNoteId(),
                request.getTitle(),
                request.getContent(),
                request.getTags()
            );

            NoteResponse response = new NoteResponse();
            response.setSuccess(true);
            response.setMessage("笔记已保存，正在建立索引...");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("保存笔记失败", e);
            return ResponseEntity.status(500).body(new NoteResponse(false, "保存失败"));
        }
    }
}
