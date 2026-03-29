package com.nemirko.echoscribe.infra.media;

import static org.assertj.core.api.Assertions.assertThat;

import com.nemirko.echoscribe.config.TranscriptionProperties;
import com.nemirko.echoscribe.infra.process.CommandRequest;
import com.nemirko.echoscribe.infra.process.CommandResult;
import com.nemirko.echoscribe.infra.process.ExternalCommandExecutor;
import java.util.List;
import org.junit.jupiter.api.Test;

class DefaultFfprobeExecutorTest {

    @Test
    void buildsJsonProbeCommand() {
        RecordingExecutor executor = new RecordingExecutor();
        TranscriptionProperties properties = new TranscriptionProperties();
        properties.setFfprobeCommand("ffprobe-custom");
        DefaultFfprobeExecutor service = new DefaultFfprobeExecutor(properties, executor);

        MediaProbeResult result = service.probe(java.nio.file.Path.of("movie.mp4"));
        assertThat(result.formatName()).isEqualTo("mp4");
        List<String> command = executor.lastRequest.command();
        assertThat(command.get(0)).isEqualTo("ffprobe-custom");
        assertThat(command).contains("-show_entries");
    }

    private static class RecordingExecutor implements ExternalCommandExecutor {
        private CommandRequest lastRequest;

        @Override
        public CommandResult run(CommandRequest request) {
            this.lastRequest = request;
            String json = "{\"format\": {\"format_name\": \"mp4\", \"duration\": \"5.0\"}}";
            return new CommandResult(0, json, "");
        }
    }
}
