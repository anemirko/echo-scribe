package com.nemirko.echoscribe.infra.media;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nemirko.echoscribe.config.TranscriptionProperties;
import com.nemirko.echoscribe.infra.process.CommandRequest;
import com.nemirko.echoscribe.infra.process.CommandResult;
import com.nemirko.echoscribe.infra.process.CommandExecutionException;
import com.nemirko.echoscribe.infra.process.ExternalCommandExecutor;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DefaultFfprobeExecutor implements FfprobeExecutor {

    private static final Logger log = LoggerFactory.getLogger(DefaultFfprobeExecutor.class);

    private final TranscriptionProperties properties;
    private final ExternalCommandExecutor commandExecutor;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DefaultFfprobeExecutor(TranscriptionProperties properties, ExternalCommandExecutor commandExecutor) {
        this.properties = properties;
        this.commandExecutor = commandExecutor;
    }

    @Override
    public MediaProbeResult probe(Path mediaPath) {
        log.info("Running ffprobe for {}", mediaPath);
        List<String> command = List.of(
                properties.getFfprobeCommand(),
                "-v", "error",
                "-show_entries", "format=format_name,duration",
                "-print_format", "json",
                mediaPath.toAbsolutePath().toString());

        CommandRequest request = CommandRequest.builder(command)
                .timeout(properties.requestTimeout())
                .build();
        CommandResult result = commandExecutor.run(request);
        if (!result.isSuccess()) {
            throw new CommandExecutionException("ffprobe failed: " + result.stderr());
        }

        try {
            JsonNode root = objectMapper.readTree(result.stdout());
            JsonNode format = root.path("format");
            String formatName = format.path("format_name").asText();
            double durationSeconds = format.path("duration").asDouble(0);
            return new MediaProbeResult(formatName, Duration.ofMillis(Math.round(durationSeconds * 1000)));
        } catch (IOException e) {
            throw new CommandExecutionException("Unable to parse ffprobe response", e);
        }
    }
}
