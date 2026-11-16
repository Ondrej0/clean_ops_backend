package com.example.backend_clean_ops.config;

import com.example.backend_clean_ops.service.NoShowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class NoShowStartupRunner implements ApplicationRunner {

    private final NoShowService noShowService;

    @Override
    public void run(ApplicationArguments args) {
        int marked = noShowService.markExpiredUnstartedShifts();
        log.info("Marked {} expired unstarted shifts as no-shows during startup", marked);
    }
}
