package com.nemirko.echoscribe.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nemirko.echoscribe.dto.SystemStatusResponse;
import com.nemirko.echoscribe.service.SystemStatusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = SystemStatusApiController.class)
class SystemStatusApiControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    SystemStatusService systemStatusService;

    @Test
    void returnsStatus() throws Exception {
        SystemStatusResponse response = new SystemStatusResponse();
        response.setFileTranscriptionReady(true);
        response.setUrlTranscriptionReady(false);
        response.setChecks(java.util.List.of());
        when(systemStatusService.currentStatus()).thenReturn(response);

        mockMvc.perform(get("/api/system/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileTranscriptionReady").value(true));
    }
}
