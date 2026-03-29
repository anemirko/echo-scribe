package com.nemirko.echoscribe.controller;

import com.nemirko.echoscribe.dto.SystemStatusResponse;
import com.nemirko.echoscribe.dto.TranscriptionResponse;
import com.nemirko.echoscribe.service.SystemStatusService;
import com.nemirko.echoscribe.service.TranscriptionService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@Validated
public class HomeController {

    private final SystemStatusService systemStatusService;
    private final TranscriptionService transcriptionService;

    public HomeController(SystemStatusService systemStatusService,
                          TranscriptionService transcriptionService) {
        this.systemStatusService = systemStatusService;
        this.transcriptionService = transcriptionService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("status", systemStatusService.currentStatus());
        return "index";
    }

    @PostMapping("/transcriptions/file")
    public String transcribeFile(@RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "language", required = false) String language,
                                 Model model) {
        TranscriptionResponse response = TranscriptionResponse.fromResult(
                transcriptionService.transcribeFile(file, java.util.Optional.ofNullable(language)));
        populateModel(model, response);
        return "index";
    }

    @PostMapping("/transcriptions/url")
    public String transcribeUrl(@RequestParam("url") @NotBlank String url,
                                @RequestParam(value = "language", required = false) String language,
                                Model model) {
        TranscriptionResponse response = TranscriptionResponse.fromResult(
                transcriptionService.transcribeUrl(url, java.util.Optional.ofNullable(language)));
        populateModel(model, response);
        return "index";
    }

    private void populateModel(Model model, TranscriptionResponse response) {
        model.addAttribute("status", systemStatusService.currentStatus());
        model.addAttribute("result", response);
    }
}
