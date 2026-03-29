package com.nemirko.echoscribe.service;

import com.nemirko.echoscribe.dto.TranscriptionJobResponse;
import com.nemirko.echoscribe.dto.TranscriptionResponse;
import com.nemirko.echoscribe.model.AcquiredMedia;
import com.nemirko.echoscribe.model.TranscriptionJob;
import com.nemirko.echoscribe.model.TranscriptionResult;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

@Service
public class TranscriptionJobService {

    private static final Logger log = LoggerFactory.getLogger(TranscriptionJobService.class);

    private final MediaAcquisitionService mediaAcquisitionService;
    private final TranscriptionService transcriptionService;
    private final LanguageDetectionService languageDetectionService;
    private final TaskExecutor transcriptionTaskExecutor;
    private final Map<String, TranscriptionJob> jobs = new ConcurrentHashMap<>();

    public TranscriptionJobService(MediaAcquisitionService mediaAcquisitionService,
                                   TranscriptionService transcriptionService,
                                   LanguageDetectionService languageDetectionService,
                                   TaskExecutor transcriptionTaskExecutor) {
        this.mediaAcquisitionService = mediaAcquisitionService;
        this.transcriptionService = transcriptionService;
        this.languageDetectionService = languageDetectionService;
        this.transcriptionTaskExecutor = transcriptionTaskExecutor;
    }

    public TranscriptionJobResponse submitFile(MultipartFile file, Optional<String> language) {
        final AcquiredMedia media = mediaAcquisitionService.acquireFromFile(file);
        TranscriptionJob job = registerJob();
        TranscriptionJobResponse initial = TranscriptionJobResponse.fromJob(job);
        enqueue(job, () -> {
            try (media) {
                Optional<String> languageToUse = determineLanguage(media, language);
                return transcriptionService.transcribeMedia(media, languageToUse);
            }
        });
        return initial;
    }

    public TranscriptionJobResponse submitUrl(String url, Optional<String> language) {
        mediaAcquisitionService.validateUrl(url);
        TranscriptionJob job = registerJob();
        TranscriptionJobResponse initial = TranscriptionJobResponse.fromJob(job);
        enqueue(job, () -> {
            try (AcquiredMedia media = mediaAcquisitionService.acquireFromUrl(url)) {
                Optional<String> languageToUse = determineLanguage(media, language);
                return transcriptionService.transcribeMedia(media, languageToUse);
            }
        });
        return initial;
    }

    public Optional<TranscriptionJobResponse> findJob(String jobId) {
        return Optional.ofNullable(jobs.get(jobId)).map(TranscriptionJobResponse::fromJob);
    }

    private TranscriptionJob registerJob() {
        TranscriptionJob job = new TranscriptionJob();
        jobs.put(job.getId(), job);
        return job;
    }

    private void enqueue(TranscriptionJob job, Callable<TranscriptionResult> task) {
        transcriptionTaskExecutor.execute(() -> executeJob(job, task));
    }

    private void executeJob(TranscriptionJob job, Callable<TranscriptionResult> task) {
        job.markRunning();
        try {
            TranscriptionResult result = task.call();
            TranscriptionResponse response = TranscriptionResponse.fromResult(result);
            job.markCompleted(response);
        } catch (Exception e) {
            log.error("Transcription job {} failed", job.getId(), e);
            String message = e.getMessage() == null ? "Transcription failed" : e.getMessage();
            job.markFailed(message);
        }
    }

    private Optional<String> determineLanguage(AcquiredMedia media, Optional<String> requestedLanguage) {
        Optional<String> normalized = requestedLanguage
                .map(String::trim)
                .filter(StringUtils::hasText);
        if (normalized.isPresent()) {
            return normalized;
        }
        return languageDetectionService.detectLanguage(media.getMediaPath())
                .map(String::trim)
                .filter(StringUtils::hasText);
    }
}
