package com.example.backend_clean_ops.entity;

import com.example.backend_clean_ops.enums.ShiftStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "shift")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shift extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @Column(name = "shift_date", nullable = false)
    private LocalDate shiftDate;

    @Column(name = "scheduled_start", nullable = false)
    private LocalDateTime scheduledStart;

    @Column(name = "scheduled_end", nullable = false)
    private LocalDateTime scheduledEnd;

    @Column(name = "actual_start")
    private LocalDateTime actualStart;

    @Column(name = "actual_end")
    private LocalDateTime actualEnd;

    @Column(name = "clock_in_latitude", precision = 10, scale = 7)
    private BigDecimal clockInLatitude;

    @Column(name = "clock_in_longitude", precision = 10, scale = 7)
    private BigDecimal clockInLongitude;

    @Column(name = "clock_out_latitude", precision = 10, scale = 7)
    private BigDecimal clockOutLatitude;

    @Column(name = "clock_out_longitude", precision = 10, scale = 7)
    private BigDecimal clockOutLongitude;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ShiftStatus status = ShiftStatus.SCHEDULED;

    @Lob
    @Column(name = "manager_note", columnDefinition = "text")
    private String managerNote;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
