package com.nemirko.echoscribe.service;

import com.nemirko.echoscribe.config.TranscriptionProperties;
import com.nemirko.echoscribe.dto.SystemCheckResult;
import com.nemirko.echoscribe.infra.process.CommandExecutionException;
import com.nemirko.echoscribe.infra.process.CommandRequest;
import com.nemirko.echoscribe.infra.process.ExternalCommandExecutor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ExternalToolDiagnosticsService {

    private static final Logger log = LoggerFactory.getLogger(ExternalToolDiagnosticsService.class);

    private final TranscriptionProperties properties;
    private final ExternalCommandExecutor commandExecutor;

    public ExternalToolDiagnosticsService(TranscriptionProperties properties,
                                          ExternalCommandExecutor commandExecutor) {
        this.properties = properties;
        this.commandExecutor = commandExecutor;
    }

    public List<SystemCheckResult> systemChecks() {
        List<SystemCheckResult> checks = new ArrayList<>();
        checks.add(checkCommand("ffmpeg", properties.getFfmpegCommand()));
        checks.add(checkCommand("ffprobe", properties.getFfprobeCommand()));
        checks.add(checkCommand("whisper-cli", properties.getWhisperCommand()));
        checks.add(checkModel());
        if (properties.isUrlInputEnabled()) {
            checks.add(checkCommand("downloader", properties.getDownloaderCommand()));
        } else {
            checks.add(new SystemCheckResult("downloader", false, "URL transcription disabled"));
        }
        return checks;
    }

    private SystemCheckResult checkCommand(String label, String command) {
        try {
            var request = CommandRequest.builder(List.of(command, "--version"))
                    .timeout(properties.requestTimeout())
                    .build();
            commandExecutor.run(request);
            return new SystemCheckResult(label, true, "available");
        } catch (RuntimeException ex) {
            log.warn("{} check failed: {}", label, ex.getMessage());
            return new SystemCheckResult(label, false, ex.getMessage());
        }
    }

    private SystemCheckResult checkModel() {
        String modelPath = properties.getWhisperModelPath();
        if (modelPath == null || modelPath.isBlank()) {
            return new SystemCheckResult("whisper-model", false, "Model path not configured");
        }
        Path path = Path.of(modelPath);
        boolean available = Files.isRegularFile(path) && Files.isReadable(path);
        return new SystemCheckResult(
                "whisper-model",
                available,
                available ? "readable" : "missing or unreadable");
    }
}
