package com.nemirko.echoscribe.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.nemirko.echoscribe.dto.SystemStatusResponse;
import com.nemirko.echoscribe.service.SystemStatusService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SystemStatusControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    SystemStatusService systemStatusService;

    @BeforeEach
    void setup() {
        SystemStatusResponse response = new SystemStatusResponse();
        response.setFileTranscriptionReady(false);
        response.setUrlTranscriptionReady(false);
        response.setChecks(List.of());
        org.mockito.Mockito.when(systemStatusService.currentStatus()).thenReturn(response);
    }

    @Test
    void rendersStatusPage() throws Exception {
        mockMvc.perform(get("/system/status"))
                .andExpect(status().isOk())
                .andExpect(view().name("system-status"));
    }
}
