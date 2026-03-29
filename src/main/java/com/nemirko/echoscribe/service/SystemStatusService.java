package com.nemirko.echoscribe.service;

import com.nemirko.echoscribe.dto.SystemCheckResult;
import com.nemirko.echoscribe.dto.SystemStatusResponse;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SystemStatusService {

    private static final Logger log = LoggerFactory.getLogger(SystemStatusService.class);

    private final ExternalToolDiagnosticsService diagnosticsService;

    public SystemStatusService(ExternalToolDiagnosticsService diagnosticsService) {
        this.diagnosticsService = diagnosticsService;
    }

    public SystemStatusResponse currentStatus() {
        List<SystemCheckResult> checks = diagnosticsService.systemChecks();
        boolean ffmpeg = isAvailable(checks, "ffmpeg");
        boolean ffprobe = isAvailable(checks, "ffprobe");
        boolean whisper = isAvailable(checks, "whisper-cli");
        boolean model = isAvailable(checks, "whisper-model");
        boolean downloader = isAvailable(checks, "downloader");

        SystemStatusResponse response = new SystemStatusResponse();
        response.setChecks(checks);
        response.setFileTranscriptionReady(ffmpeg && ffprobe && whisper && model);
        response.setUrlTranscriptionReady(response.isFileTranscriptionReady() && downloader);
        log.debug("System status: fileReady={}, urlReady={}",
                response.isFileTranscriptionReady(), response.isUrlTranscriptionReady());
        return response;
    }

    private boolean isAvailable(List<SystemCheckResult> checks, String name) {
        return checks.stream().filter(c -> c.getName().equals(name)).findFirst().map(SystemCheckResult::isAvailable).orElse(false);
    }
}
