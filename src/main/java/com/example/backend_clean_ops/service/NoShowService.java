package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.enums.ShiftStatus;
import com.example.backend_clean_ops.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NoShowService {

    private final ShiftRepository shiftRepository;
    private final ShiftTimingPolicy timing;
    private final Clock clock;

    /**
     * Marks shifts as no-shows only after their inclusive clock-in deadline has passed.
     */
    @Transactional
    public int markExpiredUnstartedShifts() {
        Instant now = clock.instant();
        LocalDateTime localNow = LocalDateTime.ofInstant(now, ShiftTimingPolicy.ZONE);
        int marked = 0;
        for (var shift : shiftRepository.findUnstartedScheduledShiftsEndedBy(ShiftStatus.SCHEDULED, localNow)) {
            if (now.isAfter(timing.clockInDeadline(shift).toInstant())) {
                shift.setStatus(ShiftStatus.NO_SHOW);
                marked++;
            }
        }
        return marked;
    }
}
