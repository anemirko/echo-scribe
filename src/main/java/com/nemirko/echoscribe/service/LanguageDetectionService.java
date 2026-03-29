package com.nemirko.echoscribe.service;

import com.nemirko.echoscribe.config.TranscriptionProperties;
import com.nemirko.echoscribe.infra.media.PreparedAudio;
import com.nemirko.echoscribe.infra.process.CommandRequest;
import com.nemirko.echoscribe.infra.process.CommandResult;
import com.nemirko.echoscribe.infra.process.ExternalCommandExecutor;
import com.nemirko.echoscribe.infra.whisper.WhisperExecutor;
import com.nemirko.echoscribe.infra.whisper.WhisperResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LanguageDetectionService {

    private static final Logger log = LoggerFactory.getLogger(LanguageDetectionService.class);

    private final TranscriptionProperties properties;
    private final ExternalCommandExecutor commandExecutor;
    private final WhisperExecutor whisperExecutor;

    public LanguageDetectionService(TranscriptionProperties properties,
                                    ExternalCommandExecutor commandExecutor,
                                    WhisperExecutor whisperExecutor) {
        this.properties = properties;
        this.commandExecutor = commandExecutor;
        this.whisperExecutor = whisperExecutor;
    }

    public Optional<String> detectLanguage(Path mediaPath) {
        long sampleSeconds = properties.getLanguageDetectionSampleSeconds();
        if (sampleSeconds <= 0) {
            return Optional.empty();
        }
        try {
            Path tempDir = Files.createDirectories(properties.tempDirPath());
            Path sampleFile = Files.createTempFile(tempDir, "lang-", ".wav");
            extractSample(mediaPath, sampleFile, sampleSeconds);
            try (PreparedAudio sampleAudio = new PreparedAudio(sampleFile, true)) {
                WhisperResult result = whisperExecutor.transcribe(sampleAudio.audioFile(), Optional.empty());
                if (StringUtils.hasText(result.language())) {
                    log.info("Detected language '{}' using {}s sample", result.language(), sampleSeconds);
                    return Optional.of(result.language());
                }
            }
        } catch (IOException | RuntimeException e) {
            log.warn("Language detection failed for {}: {}", mediaPath.getFileName(), e.getMessage());
        }
        return Optional.empty();
    }

    private void extractSample(Path source, Path target, long seconds) {
        List<String> command = new ArrayList<>();
        command.add(properties.getFfmpegCommand());
        command.add("-y");
        command.add("-i");
        command.add(source.toAbsolutePath().toString());
        command.add("-t");
        command.add(String.valueOf(seconds));
        command.add("-vn");
        command.add("-ac");
        command.add("1");
        command.add("-ar");
        command.add("16000");
        command.add(target.toAbsolutePath().toString());

        CommandResult result = commandExecutor.run(CommandRequest.builder(command)
                .timeout(properties.requestTimeout())
                .build());
        if (!result.isSuccess()) {
            throw new RuntimeException("ffmpeg sample extraction failed: " + result.stderr());
        }
    }
}
