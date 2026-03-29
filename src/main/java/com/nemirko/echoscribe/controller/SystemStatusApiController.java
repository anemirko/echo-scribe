package com.nemirko.echoscribe.controller;

import com.nemirko.echoscribe.dto.SystemStatusResponse;
import com.nemirko.echoscribe.service.SystemStatusService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system/status")
public class SystemStatusApiController {

    private final SystemStatusService systemStatusService;

    public SystemStatusApiController(SystemStatusService systemStatusService) {
        this.systemStatusService = systemStatusService;
    }

    @GetMapping
    public SystemStatusResponse status() {
        return systemStatusService.currentStatus();
    }
}
