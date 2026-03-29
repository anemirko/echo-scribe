package com.nemirko.echoscribe.controller;

import com.nemirko.echoscribe.dto.DependencyInstallInstruction;
import com.nemirko.echoscribe.service.SystemStatusService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/system/status")
public class SystemStatusController {

    private final SystemStatusService systemStatusService;

    private static final List<DependencyInstallInstruction> INSTALL_GUIDES = List.of(
            new DependencyInstallInstruction(
                    "ffmpeg & ffprobe",
                    "brew install ffmpeg",
                    "Installs both ffmpeg and ffprobe binaries used for probing and extraction."),
            new DependencyInstallInstruction(
                    "whisper-cli (whisper.cpp)",
                    "brew install whisper-cpp",
                    "Provides whisper-cli; download a ggml model and set app.transcription.whisper-model-path."),
            new DependencyInstallInstruction(
                    "yt-dlp",
                    "brew install yt-dlp",
                    "Required for URL transcription when app.transcription.url-input-enabled=true."));

    public SystemStatusController(SystemStatusService systemStatusService) {
        this.systemStatusService = systemStatusService;
    }

    @GetMapping
    public String view(Model model) {
        model.addAttribute("status", systemStatusService.currentStatus());
        model.addAttribute("installGuides", INSTALL_GUIDES);
        return "system-status";
    }

}
