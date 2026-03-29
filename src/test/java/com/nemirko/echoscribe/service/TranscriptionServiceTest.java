package com.nemirko.echoscribe.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nemirko.echoscribe.infra.media.PreparedAudio;
import com.nemirko.echoscribe.infra.whisper.WhisperExecutor;
import com.nemirko.echoscribe.infra.whisper.WhisperResult;
import com.nemirko.echoscribe.model.AcquiredMedia;
import com.nemirko.echoscribe.model.TranscriptionResult;
import com.nemirko.echoscribe.model.TranscriptionSourceType;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.springframework.mock.web.MockMultipartFile;

class TranscriptionServiceTest {

    private final MediaAcquisitionService acquisitionService = mock(MediaAcquisitionService.class);
    private final MediaPreparationService preparationService = mock(MediaPreparationService.class);
    private final WhisperExecutor whisperExecutor = mock(WhisperExecutor.class);
    private final TranscriptionService service = new TranscriptionService(acquisitionService, preparationService, whisperExecutor);

    @Test
    void orchestratesFileTranscription() {
        MockMultipartFile file = new MockMultipartFile("file", "clip.wav", "audio/wav", new byte[] {1, 2});
        AcquiredMedia media = new AcquiredMedia(TranscriptionSourceType.FILE_UPLOAD, Path.of("clip.wav"), "clip.wav", null);
        MediaPreparationService.PreparationResult prep = new MediaPreparationService.PreparationResult(
                new com.nemirko.echoscribe.infra.media.MediaProbeResult("wav", Duration.ofSeconds(5)),
                new PreparedAudio(Path.of("clip.wav"), false));

        when(acquisitionService.acquireFromFile(file)).thenReturn(media);
        when(preparationService.prepare(media.getMediaPath())).thenReturn(prep);
        when(whisperExecutor.transcribe(any(), any())).thenReturn(new WhisperResult("en", "hello"));

        TranscriptionResult result = service.transcribeFile(file, Optional.of("en"));
        assertThat(result.getTranscriptionText()).isEqualTo("hello");
        assertThat(result.getLanguage()).isEqualTo("en");
    }
}
