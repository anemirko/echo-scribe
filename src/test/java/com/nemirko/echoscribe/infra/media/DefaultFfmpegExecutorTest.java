package com.nemirko.echoscribe.infra.media;

import static org.assertj.core.api.Assertions.assertThat;

import com.nemirko.echoscribe.config.TranscriptionProperties;
import com.nemirko.echoscribe.infra.process.CommandRequest;
import com.nemirko.echoscribe.infra.process.CommandResult;
import com.nemirko.echoscribe.infra.process.ExternalCommandExecutor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultFfmpegExecutorTest {

    private Path tempDir;
    private RecordingExecutor executor;

    @BeforeEach
    void setup() throws IOException {
        tempDir = Files.createTempDirectory("ffmpeg-test");
        executor = new RecordingExecutor();
    }

    @AfterEach
    void cleanup() throws IOException {
        Files.walk(tempDir)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ignored) {
                    }
                });
    }

    @Test
    void buildsExpectedCommand() {
        TranscriptionProperties properties = new TranscriptionProperties();
        properties.setTempDir(tempDir.toString());
        properties.setFfmpegCommand("ffmpeg-custom");
        DefaultFfmpegExecutor executorService = new DefaultFfmpegExecutor(properties, executor);

        PreparedAudio prepared = executorService.convertToWav(Path.of("input.mov"), tempDir);
        assertThat(prepared.audioFile()).exists();
        List<String> command = executor.lastRequest.command();
        assertThat(command.get(0)).isEqualTo("ffmpeg-custom");
        assertThat(command).contains("-i", Path.of("input.mov").toAbsolutePath().toString());
    }

    private static class RecordingExecutor implements ExternalCommandExecutor {
        private CommandRequest lastRequest;

        @Override
        public CommandResult run(CommandRequest request) {
            this.lastRequest = request;
            return new CommandResult(0, "", "");
        }
    }
}
