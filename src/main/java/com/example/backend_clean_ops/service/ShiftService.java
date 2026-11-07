package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.dto.responses.GetShiftsResponse;
import com.example.backend_clean_ops.dto.responses.GetShiftsResponse.ShiftDetails;
import com.example.backend_clean_ops.dto.responses.GetShiftsResponse.SiteDetails;
import com.example.backend_clean_ops.entity.Shift;
import com.example.backend_clean_ops.repository.ShiftRepository;
import com.example.backend_clean_ops.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShiftService {
    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;
    private final ShiftTimingPolicy timing;
    private final Clock clock;

    @Transactional(readOnly = true)
    public GetShiftsResponse getShifts(UUID tenantId, UUID userId) {
        if (!userRepository.existsByIdAndTenantId(userId, tenantId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found in tenant.");
        }
        Instant now = clock.instant();
        LocalDate today = now.atZone(ShiftTimingPolicy.ZONE).toLocalDate();
        LocalDate from = today.minusDays(3);
        LocalDate to = today.plusDays(3);
        var shifts = shiftRepository.findInWindow(tenantId, userId, from.atStartOfDay(), to.plusDays(1).atStartOfDay())
                .stream().map(shift -> details(shift, now)).toList();
        return new GetShiftsResponse(ShiftTimingPolicy.ZONE.getId(), from, to, shifts);
    }

    private ShiftDetails details(Shift shift, Instant now) {
        return new ShiftDetails(shift.getId(), new SiteDetails(shift.getSite().getId(), shift.getSite().getName()),
                timing.timestamp(shift.getScheduledStart()), timing.timestamp(shift.getScheduledEnd()),
                timing.timestamp(shift.getActualStart()), timing.timestamp(shift.getActualEnd()), shift.getStatus(),
                timing.clockInOpensAt(shift), timing.clockInDeadline(shift), timing.clockOutDeadline(shift),
                timing.canClockIn(shift, now), timing.canClockOut(shift, now), timing.requiresManagerCorrection(shift, now));
    }
}
