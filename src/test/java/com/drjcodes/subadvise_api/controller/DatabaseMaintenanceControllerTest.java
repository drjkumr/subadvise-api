package com.drjcodes.subadvise_api.controller;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.drjcodes.subadvise_api.service.DatabasePurgeService;

class DatabaseMaintenanceControllerTest {

    private DatabasePurgeService databasePurgeService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        databasePurgeService = org.mockito.Mockito.mock(DatabasePurgeService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                new DatabaseMaintenanceController(databasePurgeService))
                .build();
    }

    @Test
    void purgesSubscriptionsSeparately() throws Exception {
        mockMvc.perform(delete("/api/data/subscriptions"))
                .andExpect(status().isNoContent());

        verify(databasePurgeService).purgeSubscriptions();
    }

    @Test
    void purgesTargetsSeparately() throws Exception {
        mockMvc.perform(delete("/api/data/targets"))
                .andExpect(status().isNoContent());

        verify(databasePurgeService).purgeTargets();
    }

    @Test
    void purgesSubscriptionsAndTargetsTogether() throws Exception {
        mockMvc.perform(delete("/api/data/subscriptions-and-targets"))
                .andExpect(status().isNoContent());

        verify(databasePurgeService).purgeSubscriptionsAndTargets();
    }
}
