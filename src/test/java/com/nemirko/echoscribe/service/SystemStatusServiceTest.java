package com.nemirko.echoscribe.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.nemirko.echoscribe.dto.SystemCheckResult;
import com.nemirko.echoscribe.dto.SystemStatusResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SystemStatusServiceTest {

    private final ExternalToolDiagnosticsService diagnosticsService = Mockito.mock(ExternalToolDiagnosticsService.class);
    private final SystemStatusService service = new SystemStatusService(diagnosticsService);

    @Test
    void aggregatesChecks() {
        when(diagnosticsService.systemChecks()).thenReturn(List.of(
                new SystemCheckResult("ffmpeg", true, "ok"),
                new SystemCheckResult("ffprobe", true, "ok"),
                new SystemCheckResult("whisper-cli", true, "ok"),
                new SystemCheckResult("whisper-model", true, "ok"),
                new SystemCheckResult("downloader", false, "missing")));

        SystemStatusResponse response = service.currentStatus();
        assertThat(response.isFileTranscriptionReady()).isTrue();
        assertThat(response.isUrlTranscriptionReady()).isFalse();
    }
}
