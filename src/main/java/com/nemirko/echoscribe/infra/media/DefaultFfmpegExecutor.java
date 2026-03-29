package com.nemirko.echoscribe.infra.media;

import com.nemirko.echoscribe.config.TranscriptionProperties;
import com.nemirko.echoscribe.infra.process.CommandRequest;
import com.nemirko.echoscribe.infra.process.CommandResult;
import com.nemirko.echoscribe.infra.process.CommandExecutionException;
import com.nemirko.echoscribe.infra.process.ExternalCommandExecutor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DefaultFfmpegExecutor implements FfmpegExecutor {

    private static final Logger log = LoggerFactory.getLogger(DefaultFfmpegExecutor.class);

    private final TranscriptionProperties properties;
    private final ExternalCommandExecutor commandExecutor;

    public DefaultFfmpegExecutor(TranscriptionProperties properties, ExternalCommandExecutor commandExecutor) {
        this.properties = properties;
        this.commandExecutor = commandExecutor;
    }

    @Override
    public PreparedAudio convertToWav(Path source, Path tempDir) {
        try {
            Files.createDirectories(tempDir);
            Path target = Files.createTempFile(tempDir, "prepared-", ".wav");
            List<String> command = new ArrayList<>();
            command.add(properties.getFfmpegCommand());
            command.add("-y");
            command.add("-i");
            command.add(source.toAbsolutePath().toString());
            command.add("-vn");
            command.add("-ac");
            command.add("1");
            command.add("-ar");
            command.add("16000");
            command.add(target.toAbsolutePath().toString());

            log.info("Running ffmpeg to prepare audio: {}", target);
            CommandResult result = commandExecutor.run(CommandRequest.builder(command)
                    .timeout(properties.requestTimeout())
                    .build());
            if (!result.isSuccess()) {
                throw new CommandExecutionException("ffmpeg failed: " + result.stderr());
            }
            return new PreparedAudio(target, true);
        } catch (IOException e) {
            throw new CommandExecutionException("Unable to prepare audio file", e);
        }
    }
}
