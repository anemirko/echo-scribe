package com.nemirko.echoscribe.infra.whisper;

import static org.assertj.core.api.Assertions.assertThat;

import com.nemirko.echoscribe.config.TranscriptionProperties;
import com.nemirko.echoscribe.infra.process.CommandRequest;
import com.nemirko.echoscribe.infra.process.CommandResult;
import com.nemirko.echoscribe.infra.process.ExternalCommandExecutor;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultWhisperExecutorTest {

    private Path tempDir;
    private Path modelFile;

    @BeforeEach
    void setup() throws IOException {
        tempDir = Files.createTempDirectory("whisper-test");
        modelFile = Files.createTempFile(tempDir, "model", ".bin");
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
    void buildsCommandAndReadsTranscript() throws IOException {
        TranscriptionProperties properties = new TranscriptionProperties();
        properties.setTempDir(tempDir.toString());
        properties.setWhisperCommand("whisper-cli");
        properties.setWhisperModelPath(modelFile.toString());
        RecordingExecutor executor = new RecordingExecutor();
        DefaultWhisperExecutor service = new DefaultWhisperExecutor(properties, executor);

        WhisperResult result = service.transcribe(Files.createTempFile(tempDir, "audio", ".wav"), Optional.of("en"));
        assertThat(result.transcript()).isEqualTo("done");
        List<String> command = executor.lastRequest.command();
        assertThat(command).contains("-m", modelFile.toString());
    }

    @Test
    void expandsUserHomeInModelPath() throws IOException {
        String originalHome = System.getProperty("user.home");
        System.setProperty("user.home", tempDir.toAbsolutePath().toString());
        try {
            TranscriptionProperties properties = new TranscriptionProperties();
            properties.setTempDir(tempDir.toString());
            properties.setWhisperCommand("whisper-cli");
            String relative = tempDir.relativize(modelFile).toString().replace('\\', '/');
            properties.setWhisperModelPath("~/" + relative);
            RecordingExecutor executor = new RecordingExecutor();
            DefaultWhisperExecutor service = new DefaultWhisperExecutor(properties, executor);

            WhisperResult result = service.transcribe(Files.createTempFile(tempDir, "audio", ".wav"), Optional.empty());
            assertThat(result.transcript()).isEqualTo("done");
            assertThat(executor.lastRequest.command()).contains("-m", modelFile.toString());
        } finally {
            if (originalHome != null) {
                System.setProperty("user.home", originalHome);
            } else {
                System.clearProperty("user.home");
            }
        }
    }

    private static class RecordingExecutor implements ExternalCommandExecutor {
        private CommandRequest lastRequest;

        @Override
        public CommandResult run(CommandRequest request) {
            this.lastRequest = request;
            int outputIndex = request.command().indexOf("-of");
            if (outputIndex >= 0 && outputIndex + 1 < request.command().size()) {
                String base = request.command().get(outputIndex + 1);
                Path transcript = Path.of(base + ".txt");
                try {
                    Files.writeString(transcript, "done", StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return new CommandResult(0, "", "");
        }
    }
}
