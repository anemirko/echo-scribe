package com.nemirko.echoscribe.controller;

import com.nemirko.echoscribe.dto.TranscriptionResponse;
import com.nemirko.echoscribe.dto.UrlTranscriptionRequest;
import com.nemirko.echoscribe.service.TranscriptionService;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/transcriptions")
@Validated
public class TranscriptionApiController {

    private final TranscriptionService transcriptionService;

    public TranscriptionApiController(TranscriptionService transcriptionService) {
        this.transcriptionService = transcriptionService;
    }

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TranscriptionResponse transcribeFile(@RequestPart("file") MultipartFile file,
                                                @RequestParam(value = "language", required = false) String language) {
        return TranscriptionResponse.fromResult(
                transcriptionService.transcribeFile(file, Optional.ofNullable(language)));
    }

    @PostMapping(value = "/url", consumes = MediaType.APPLICATION_JSON_VALUE)
    public TranscriptionResponse transcribeUrl(@Valid @RequestBody UrlTranscriptionRequest request) {
        return TranscriptionResponse.fromResult(
                transcriptionService.transcribeUrl(request.getUrl(), Optional.ofNullable(request.getLanguage())));
    }
}
