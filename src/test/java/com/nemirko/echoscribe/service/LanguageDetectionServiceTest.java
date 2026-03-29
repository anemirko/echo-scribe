package com.nemirko.echoscribe.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.nemirko.echoscribe.config.TranscriptionProperties;
import com.nemirko.echoscribe.infra.process.CommandRequest;
import com.nemirko.echoscribe.infra.process.CommandResult;
import com.nemirko.echoscribe.infra.process.ExternalCommandExecutor;
import com.nemirko.echoscribe.infra.whisper.WhisperExecutor;
import com.nemirko.echoscribe.infra.whisper.WhisperResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

class LanguageDetectionServiceTest {

    private final TranscriptionProperties properties = new TranscriptionProperties();
    private final ExternalCommandExecutor commandExecutor = Mockito.mock(ExternalCommandExecutor.class);
    private final WhisperExecutor whisperExecutor = Mockito.mock(WhisperExecutor.class);
    private final LanguageDetectionService service = new LanguageDetectionService(properties, commandExecutor, whisperExecutor);

    @TempDir
    Path tempDir;

    @Test
    void detectsLanguageFromSample() throws IOException {
        properties.setTempDir(tempDir.toString());
        when(commandExecutor.run(any(CommandRequest.class))).thenAnswer(invocation -> {
            CommandRequest req = invocation.getArgument(0);
            String samplePath = req.command().get(req.command().size() - 1);
            Files.writeString(Path.of(samplePath), "audio");
            return new CommandResult(0, "", "");
        });
        when(whisperExecutor.transcribe(any(Path.class), any())).thenReturn(new WhisperResult("ru", "text"));

        Path media = Files.createTempFile(tempDir, "media", ".mp4");
        Optional<String> detected = service.detectLanguage(media);
        assertThat(detected).contains("ru");
    }

    @Test
    void skipsWhenDisabled() throws IOException {
        properties.setLanguageDetectionSampleSeconds(0);
        Path media = Files.createTempFile(tempDir, "media", ".mp4");
        Optional<String> detected = service.detectLanguage(media);
        assertThat(detected).isEmpty();
    }
}
