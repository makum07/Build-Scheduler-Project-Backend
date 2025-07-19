package com.buildscheduler.buildscheduler.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_schedules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceSchedule extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(nullable = false)
    private LocalDateTime scheduledStartTime;

    @Column(nullable = false)
    private LocalDateTime scheduledEndTime;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceStatus status = MaintenanceStatus.SCHEDULED;

    @Column(nullable = false)
    private String reason;

    @Column(length = 2000)
    private String maintenanceNotes;

    @Column(length = 2000)
    private String completionNotes;

    @ManyToOne
    @JoinColumn(name = "scheduled_by_id")
    private User scheduledBy;

    @Column(precision = 8, scale = 2)
    private BigDecimal estimatedCost;

    // Helper methods
    public boolean overlapsWith(LocalDateTime start, LocalDateTime end) {
        return start.isBefore(scheduledEndTime) && end.isAfter(scheduledStartTime);
    }

    public boolean isOverdue() {
        return status == MaintenanceStatus.SCHEDULED &&
                scheduledStartTime.isBefore(LocalDateTime.now());
    }

    public enum MaintenanceType {
        PREVENTIVE, CORRECTIVE, EMERGENCY, INSPECTION, CALIBRATION
    }

    public enum MaintenanceStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED, DELAYED
    }
}

