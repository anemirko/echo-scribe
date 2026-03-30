package com.nemirko.echoscribe.infra.whisper;

import com.nemirko.echoscribe.config.TranscriptionProperties;
import com.nemirko.echoscribe.infra.media.PreparedAudio;
import com.nemirko.echoscribe.infra.process.CommandExecutionException;
import com.nemirko.echoscribe.infra.process.CommandRequest;
import com.nemirko.echoscribe.infra.process.CommandResult;
import com.nemirko.echoscribe.infra.process.ExternalCommandExecutor;
import com.nemirko.echoscribe.util.PathUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DefaultWhisperExecutor implements WhisperExecutor {

    private static final Logger log = LoggerFactory.getLogger(DefaultWhisperExecutor.class);

    private final TranscriptionProperties properties;
    private final ExternalCommandExecutor commandExecutor;

    public DefaultWhisperExecutor(TranscriptionProperties properties,
                                  ExternalCommandExecutor commandExecutor) {
        this.properties = properties;
        this.commandExecutor = commandExecutor;
    }

    @Override
    public WhisperResult transcribe(Path audioFile, Optional<String> language) {
        String modelPath = properties.getWhisperModelPath();
        if (modelPath == null || modelPath.isBlank()) {
            throw new CommandExecutionException("Whisper model path must be configured");
        }

        try {
            Path resolvedModelPath = PathUtils.expandUserHome(modelPath);
            Path tempDir = properties.tempDirPath();
            Files.createDirectories(tempDir);
            Path outputTemp = Files.createTempFile(tempDir, "whisper-", "");
            String lang = language.filter(s -> !s.isBlank()).orElse("auto");

            List<String> command = new ArrayList<>();
            command.add(properties.getWhisperCommand());
            command.add("-m");
            command.add(resolvedModelPath.toString());
            command.add("-f");
            command.add(audioFile.toAbsolutePath().toString());
            command.add("-otxt");
            command.add("-of");
            command.add(outputTemp.toAbsolutePath().toString());
            command.add("-l");
            command.add(lang);

            log.info("Running whisper.cpp transcription: {}", audioFile);
            CommandRequest request = CommandRequest.builder(command)
                    .timeout(properties.requestTimeout())
                    .build();
            CommandResult result = commandExecutor.run(request);
            if (!result.isSuccess()) {
                throw new CommandExecutionException("whisper-cli failed: " + result.stderr());
            }

            Path transcriptFile = Path.of(outputTemp.toAbsolutePath().toString() + ".txt");
            String transcript = Files.readString(transcriptFile, StandardCharsets.UTF_8);
            Files.deleteIfExists(transcriptFile);
            Files.deleteIfExists(outputTemp);
            log.info("Completed whisper.cpp transcription: lang={}, bytes={}", lang, transcript.length());
            return new WhisperResult(lang, transcript.trim());
        } catch (IOException e) {
            throw new CommandExecutionException("Failed to process whisper output", e);
        }
    }
}
