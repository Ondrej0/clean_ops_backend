package com.example.backend_clean_ops.service;

import com.example.backend_clean_ops.entity.Shift;
import com.example.backend_clean_ops.enums.ShiftStatus;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class ScheduleOverlapDetector {
    public record Conflict(Shift first, Shift second, String key) {}

    public List<Conflict> detect(List<Shift> shifts) {
        var active = shifts.stream().filter(s -> s.getStatus() != ShiftStatus.CANCELLED)
                .filter(s -> s.getScheduledEnd().isAfter(s.getScheduledStart())).toList();
        Map<String, Conflict> conflicts = new LinkedHashMap<>();
        for (int i = 0; i < active.size(); i++) {
            for (int j = i + 1; j < active.size(); j++) {
                Shift a = active.get(i), b = active.get(j);
                if (a.getId().equals(b.getId())) continue;
                // Same half-open interval semantics as the cleaner's calendar, in site time.
                var aStart = a.getScheduledStart().atZone(ShiftTimingPolicy.ZONE).toInstant();
                var aEnd = a.getScheduledEnd().atZone(ShiftTimingPolicy.ZONE).toInstant();
                var bStart = b.getScheduledStart().atZone(ShiftTimingPolicy.ZONE).toInstant();
                var bEnd = b.getScheduledEnd().atZone(ShiftTimingPolicy.ZONE).toInstant();
                if (aStart.isBefore(bEnd) && bStart.isBefore(aEnd)) {
                    String first = identity(a), second = identity(b);
                    String key = first.compareTo(second) <= 0 ? first + "|" + second : second + "|" + first;
                    conflicts.putIfAbsent(key, new Conflict(a, b, key));
                }
            }
        }
        return List.copyOf(conflicts.values());
    }

    private String identity(Shift shift) {
        // Schedule edits regenerate shift IDs; unchanged schedule/time pairs remain the same conflict.
        return (shift.getSchedule() == null ? "shift:" + shift.getId() : "schedule:" + shift.getSchedule().getId())
                + "/" + shift.getScheduledStart() + "/" + shift.getScheduledEnd();
    }
}
