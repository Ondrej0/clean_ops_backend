package com.example.backend_clean_ops.config;

import com.example.backend_clean_ops.service.NoShowService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NoShowStartupRunnerTest {

    @Test
    void run_triggersNoShowSweepOnce() throws Exception {
        NoShowService noShowService = mock(NoShowService.class);
        when(noShowService.markExpiredUnstartedShifts()).thenReturn(2);
        NoShowStartupRunner runner = new NoShowStartupRunner(noShowService);

        runner.run(null);

        verify(noShowService).markExpiredUnstartedShifts();
    }
}
