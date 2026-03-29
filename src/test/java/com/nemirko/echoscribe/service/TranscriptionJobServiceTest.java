package com.nemirko.echoscribe.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.nemirko.echoscribe.dto.TranscriptionJobResponse;
import com.nemirko.echoscribe.model.AcquiredMedia;
import com.nemirko.echoscribe.model.TranscriptionJobStatus;
import com.nemirko.echoscribe.model.TranscriptionResult;
import com.nemirko.echoscribe.model.TranscriptionSourceType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mock.web.MockMultipartFile;

class TranscriptionJobServiceTest {

    private final MediaAcquisitionService mediaAcquisitionService = Mockito.mock(MediaAcquisitionService.class);
    private final TranscriptionService transcriptionService = Mockito.mock(TranscriptionService.class);
    private final LanguageDetectionService languageDetectionService = Mockito.mock(LanguageDetectionService.class);
    private final TaskExecutor executor = Runnable::run;
    private final TranscriptionJobService jobService = new TranscriptionJobService(
            mediaAcquisitionService, transcriptionService, languageDetectionService, executor);

    @TempDir
    Path tempDir;

    @Test
    void completesFileJob() throws IOException {
        Path file = Files.createTempFile(tempDir, "upload", ".wav");
        AcquiredMedia media = new AcquiredMedia(TranscriptionSourceType.FILE_UPLOAD, file, "upload.wav", null);
        when(mediaAcquisitionService.acquireFromFile(any())).thenReturn(media);
        TranscriptionResult result = new TranscriptionResult(
                TranscriptionSourceType.FILE_UPLOAD,
                "upload.wav",
                null,
                "en",
                "done",
                Duration.ofSeconds(10));
        when(transcriptionService.transcribeMedia(any(AcquiredMedia.class), any())).thenReturn(result);

        MockMultipartFile multipartFile = new MockMultipartFile("file", "upload.wav", "audio/wav", new byte[] {1});
        TranscriptionJobResponse job = jobService.submitFile(multipartFile, Optional.of("en"));
        assertThat(job.getStatus()).isEqualTo(TranscriptionJobStatus.PENDING);

        TranscriptionJobResponse stored = jobService.findJob(job.getJobId()).orElseThrow();
        assertThat(stored.getStatus()).isEqualTo(TranscriptionJobStatus.SUCCEEDED);
        assertThat(stored.getResult().getTranscriptionText()).isEqualTo("done");
        Mockito.verify(languageDetectionService, Mockito.never()).detectLanguage(any());
    }

    @Test
    void marksJobFailedOnException() throws IOException {
        Path file = Files.createTempFile(tempDir, "download", ".wav");
        AcquiredMedia media = new AcquiredMedia(TranscriptionSourceType.URL_DOWNLOAD, file, "media.wav", "https://example.com");
        doNothing().when(mediaAcquisitionService).validateUrl("https://example.com");
        when(mediaAcquisitionService.acquireFromUrl("https://example.com")).thenReturn(media);
        doThrow(new RuntimeException("boom")).when(transcriptionService).transcribeMedia(any(AcquiredMedia.class), any());
        when(languageDetectionService.detectLanguage(any())).thenReturn(Optional.of("ru"));

        TranscriptionJobResponse job = jobService.submitUrl("https://example.com", Optional.empty());
        assertThat(jobService.findJob(job.getJobId())).map(TranscriptionJobResponse::getStatus)
                .contains(TranscriptionJobStatus.FAILED);
        assertThat(jobService.findJob(job.getJobId())).map(TranscriptionJobResponse::getError)
                .contains("boom");
    }

    @Test
    void usesDetectedLanguageWhenNotProvided() throws IOException {
        Path file = Files.createTempFile(tempDir, "upload", ".wav");
        AcquiredMedia media = new AcquiredMedia(TranscriptionSourceType.FILE_UPLOAD, file, "upload.wav", null);
        when(mediaAcquisitionService.acquireFromFile(any())).thenReturn(media);
        when(languageDetectionService.detectLanguage(any())).thenReturn(Optional.of("fr"));
        TranscriptionResult result = new TranscriptionResult(
                TranscriptionSourceType.FILE_UPLOAD,
                "upload.wav",
                null,
                "fr",
                "bonjour",
                Duration.ofSeconds(4));
        when(transcriptionService.transcribeMedia(any(AcquiredMedia.class), any())).thenAnswer(invocation -> {
            Optional<String> lang = invocation.getArgument(1);
            assertThat(lang).contains("fr");
            return result;
        });

        MockMultipartFile multipartFile = new MockMultipartFile("file", "upload.wav", "audio/wav", new byte[] {1});
        TranscriptionJobResponse job = jobService.submitFile(multipartFile, Optional.empty());
        TranscriptionJobResponse stored = jobService.findJob(job.getJobId()).orElseThrow();
        assertThat(stored.getStatus()).isEqualTo(TranscriptionJobStatus.SUCCEEDED);
        assertThat(stored.getResult().getDetectedLanguage()).isEqualTo("fr");
    }
}
