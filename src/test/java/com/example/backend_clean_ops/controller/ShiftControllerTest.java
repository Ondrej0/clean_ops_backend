package com.example.backend_clean_ops.controller;

import com.example.backend_clean_ops.dto.responses.GetShiftsResponse;
import com.example.backend_clean_ops.service.ShiftService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ShiftControllerTest {
    @Test
    void bindsExplicitCalendarDatesAndRejectsMalformedDates() throws Exception {
        ShiftService service = mock(ShiftService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new ShiftController(service)).build();
        UUID tenant = UUID.randomUUID();
        UUID user = UUID.randomUUID();
        LocalDate from = LocalDate.parse("2026-11-09");
        LocalDate to = LocalDate.parse("2026-11-15");
        when(service.getShifts(tenant, user, from, to))
                .thenReturn(new GetShiftsResponse("Europe/London", from, to, List.of()));
        mvc.perform(get("/api/shifts").param("tenantId", tenant.toString()).param("userId", user.toString())
                        .param("fromDate", from.toString()).param("toDate", to.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromDate").value("2026-11-09"))
                .andExpect(jsonPath("$.toDate").value("2026-11-15"));
        mvc.perform(get("/api/shifts").param("tenantId", tenant.toString()).param("userId", user.toString())
                        .param("fromDate", "not-a-date").param("toDate", to.toString()))
                .andExpect(status().isBadRequest());
        verify(service).getShifts(tenant, user, from, to);
        verifyNoMoreInteractions(service);
    }

    @Test
    void returnsWindowAndEmptyArrayAndRequiresBothIds() throws Exception {
        ShiftService service = mock(ShiftService.class);
        var mvc = MockMvcBuilders.standaloneSetup(new ShiftController(service)).build();
        UUID tenant = UUID.randomUUID();
        UUID user = UUID.randomUUID();
        when(service.getShifts(tenant, user)).thenReturn(new GetShiftsResponse("Europe/London",
                LocalDate.parse("2026-11-04"), LocalDate.parse("2026-11-10"), List.of()));
        mvc.perform(get("/api/shifts").param("tenantId", tenant.toString()).param("userId", user.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timezone").value("Europe/London"))
                .andExpect(jsonPath("$.fromDate").value("2026-11-04"))
                .andExpect(jsonPath("$.toDate").value("2026-11-10"))
                .andExpect(jsonPath("$.shifts").isEmpty());
        mvc.perform(get("/api/shifts").param("tenantId", tenant.toString()))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/shifts").param("tenantId", "invalid").param("userId", user.toString()))
                .andExpect(status().isBadRequest());
        verify(service).getShifts(tenant, user);
        verifyNoMoreInteractions(service);
    }
}
