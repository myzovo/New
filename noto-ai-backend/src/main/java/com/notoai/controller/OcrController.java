package com.notoai.controller;

import com.notoai.model.dto.OcrResponse;
import com.notoai.service.OcrService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ocr")
@Slf4j
public class OcrController {

    @Autowired
    private OcrService ocrService;

    @PostMapping("/recognize")
    public ResponseEntity<OcrResponse> recognize(@RequestParam("image") MultipartFile image) {
        try {
            String text = ocrService.recognizeText(image);

            OcrResponse response = new OcrResponse();
            response.setSuccess(true);
            response.setText(text);
            response.setMessage("识别成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("OCR识别失败", e);

            OcrResponse response = new OcrResponse();
            response.setSuccess(false);
            response.setMessage("识别失败: " + e.getMessage());

            return ResponseEntity.status(500).body(response);
        }
    }
}
