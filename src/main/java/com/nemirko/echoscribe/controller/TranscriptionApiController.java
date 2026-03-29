package com.nemirko.echoscribe.controller;

import com.nemirko.echoscribe.dto.UrlTranscriptionRequest;
import com.nemirko.echoscribe.dto.TranscriptionJobResponse;
import com.nemirko.echoscribe.service.TranscriptionJobService;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/transcriptions")
@Validated
public class TranscriptionApiController {

    private final TranscriptionJobService transcriptionJobService;

    public TranscriptionApiController(TranscriptionJobService transcriptionJobService) {
        this.transcriptionJobService = transcriptionJobService;
    }

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TranscriptionJobResponse> transcribeFile(@RequestPart("file") MultipartFile file,
                                                                   @RequestParam(value = "language", required = false) String language) {
        TranscriptionJobResponse job = transcriptionJobService.submitFile(file, Optional.ofNullable(language));
        return ResponseEntity.accepted()
                .header(HttpHeaders.LOCATION, "/api/transcriptions/" + job.getJobId())
                .body(job);
    }

    @PostMapping(value = "/url", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TranscriptionJobResponse> transcribeUrl(@Valid @RequestBody UrlTranscriptionRequest request) {
        TranscriptionJobResponse job = transcriptionJobService.submitUrl(
                request.getUrl(), Optional.ofNullable(request.getLanguage()));
        return ResponseEntity.accepted()
                .header(HttpHeaders.LOCATION, "/api/transcriptions/" + job.getJobId())
                .body(job);
    }

    @GetMapping("/{jobId}")
    public TranscriptionJobResponse jobStatus(@PathVariable String jobId) {
        return transcriptionJobService.findJob(jobId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
    }
}
