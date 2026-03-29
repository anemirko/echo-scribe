package com.nemirko.echoscribe.service;

import com.nemirko.echoscribe.exception.InvalidRequestException;
import com.nemirko.echoscribe.infra.whisper.WhisperExecutor;
import com.nemirko.echoscribe.infra.whisper.WhisperResult;
import com.nemirko.echoscribe.model.AcquiredMedia;
import com.nemirko.echoscribe.model.TranscriptionResult;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TranscriptionService {

    private static final Logger log = LoggerFactory.getLogger(TranscriptionService.class);

    private final MediaAcquisitionService mediaAcquisitionService;
    private final MediaPreparationService mediaPreparationService;
    private final WhisperExecutor whisperExecutor;

    public TranscriptionService(MediaAcquisitionService mediaAcquisitionService,
                                MediaPreparationService mediaPreparationService,
                                WhisperExecutor whisperExecutor) {
        this.mediaAcquisitionService = mediaAcquisitionService;
        this.mediaPreparationService = mediaPreparationService;
        this.whisperExecutor = whisperExecutor;
    }

    public TranscriptionResult transcribeFile(MultipartFile file, Optional<String> language) {
        try (AcquiredMedia media = mediaAcquisitionService.acquireFromFile(file)) {
            return process(media, language);
        }
    }

    public TranscriptionResult transcribeUrl(String url, Optional<String> language) {
        try (AcquiredMedia media = mediaAcquisitionService.acquireFromUrl(url)) {
            return process(media, language);
        }
    }

    private TranscriptionResult process(AcquiredMedia media, Optional<String> language) {
        log.info("Starting transcription for {}", media.getDisplayName());
        try (MediaPreparationService.PreparationResult preparation = mediaPreparationService.prepare(media.getMediaPath())) {
            WhisperResult whisperResult = whisperExecutor.transcribe(preparation.audio().audioFile(), language);
            return new TranscriptionResult(
                    media.getSourceType(),
                    media.getDisplayName(),
                    media.getSourceUrl(),
                    whisperResult.language(),
                    whisperResult.transcript(),
                    preparation.probe().duration());
        }
    }
}
