package com.nemirko.echoscribe.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nemirko.echoscribe.dto.TranscriptionJobResponse;
import com.nemirko.echoscribe.dto.TranscriptionResponse;
import com.nemirko.echoscribe.exception.InvalidRequestException;
import com.nemirko.echoscribe.model.TranscriptionJobStatus;
import com.nemirko.echoscribe.service.TranscriptionJobService;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TranscriptionApiController.class)
class TranscriptionApiControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    TranscriptionJobService transcriptionJobService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void enqueuesFileUpload() throws Exception {
        TranscriptionJobResponse job = new TranscriptionJobResponse();
        job.setJobId("job-1");
        job.setStatus(TranscriptionJobStatus.PENDING);
        job.setCreatedAt(Instant.now());
        job.setUpdatedAt(Instant.now());
        when(transcriptionJobService.submitFile(any(), any())).thenReturn(job);

        MockMultipartFile file = new MockMultipartFile("file", "clip.wav", "audio/wav", new byte[] {1});
        mockMvc.perform(multipart("/api/transcriptions/file").file(file).param("language", "en"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").value("job-1"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void returnsProblemDetailForInvalidUrl() throws Exception {
        when(transcriptionJobService.submitUrl(any(), any())).thenThrow(new InvalidRequestException("bad url"));

        var body = objectMapper.createObjectNode();
        body.put("url", "bad");

        mockMvc.perform(post("/api/transcriptions/url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"));
    }

    @Test
    void returnsJobStatus() throws Exception {
        TranscriptionResponse response = new TranscriptionResponse();
        response.setSourceName("clip.wav");
        response.setTranscriptionText("hi");
        TranscriptionJobResponse job = new TranscriptionJobResponse();
        job.setJobId("job-2");
        job.setStatus(TranscriptionJobStatus.SUCCEEDED);
        job.setResult(response);
        job.setCreatedAt(Instant.now());
        job.setUpdatedAt(Instant.now());
        when(transcriptionJobService.findJob(eq("job-2"))).thenReturn(Optional.of(job));

        mockMvc.perform(get("/api/transcriptions/job-2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.result.sourceName").value("clip.wav"));
    }
}
