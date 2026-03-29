package com.nemirko.echoscribe.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.nemirko.echoscribe.config.TranscriptionProperties;
import com.nemirko.echoscribe.dto.SystemCheckResult;
import com.nemirko.echoscribe.infra.process.CommandRequest;
import com.nemirko.echoscribe.infra.process.CommandResult;
import com.nemirko.echoscribe.infra.process.ExternalCommandExecutor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExternalToolDiagnosticsServiceTest {

    private final ExternalCommandExecutor commandExecutor = mock(ExternalCommandExecutor.class);

    @Test
    void resolvesTildePrefixedModelPath() throws IOException {
        when(commandExecutor.run(any(CommandRequest.class))).thenReturn(new CommandResult(0, "", ""));

        TranscriptionProperties properties = new TranscriptionProperties();
        properties.setUrlInputEnabled(false);

        Path home = Path.of(System.getProperty("user.home")).toAbsolutePath();
        Path modelFile = Files.createTempFile(home, "whisper-model", ".bin");
        modelFile.toFile().deleteOnExit();
        String relativePath = home.relativize(modelFile).toString().replace('\\', '/');
        properties.setWhisperModelPath("~/" + relativePath);

        ExternalToolDiagnosticsService service = new ExternalToolDiagnosticsService(properties, commandExecutor);

        List<SystemCheckResult> checks = service.systemChecks();
        SystemCheckResult modelResult = checks.stream()
                .filter(check -> check.getName().equals("whisper-model"))
                .findFirst()
                .orElseThrow();

        assertThat(modelResult.isAvailable()).isTrue();
        assertThat(modelResult.getMessage()).isEqualTo("readable");
    }
}
