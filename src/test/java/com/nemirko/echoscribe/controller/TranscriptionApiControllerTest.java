package com.nemirko.echoscribe.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nemirko.echoscribe.exception.InvalidRequestException;
import com.nemirko.echoscribe.model.TranscriptionResult;
import com.nemirko.echoscribe.model.TranscriptionSourceType;
import com.nemirko.echoscribe.service.TranscriptionService;
import java.time.Duration;
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
    TranscriptionService transcriptionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void transcribesFileUpload() throws Exception {
        TranscriptionResult result = new TranscriptionResult(
                TranscriptionSourceType.FILE_UPLOAD,
                "clip.wav",
                null,
                "en",
                "hello world",
                Duration.ofSeconds(5));
        when(transcriptionService.transcribeFile(any(), any())).thenReturn(result);

        MockMultipartFile file = new MockMultipartFile("file", "clip.wav", "audio/wav", new byte[] {1});
        mockMvc.perform(multipart("/api/transcriptions/file").file(file).param("language", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sourceName").value("clip.wav"));
    }

    @Test
    void returnsProblemDetailForInvalidUrl() throws Exception {
        when(transcriptionService.transcribeUrl(any(), any())).thenThrow(new InvalidRequestException("bad url"));

        var body = objectMapper.createObjectNode();
        body.put("url", "bad");

        mockMvc.perform(post("/api/transcriptions/url")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request"));
    }
}
