package com.buildscheduler.buildscheduler.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentAssignment extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subtask_id", nullable = false)
    private Subtask subtask;

    @ManyToOne(optional = false)
    @JoinColumn(name = "assigned_by_id", nullable = false)
    private User assignedBy;

    @Column(nullable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status = AssignmentStatus.ASSIGNED;

    // Helper methods
    public boolean overlapsWith(LocalDateTime start, LocalDateTime end) {
        return start.isBefore(endTime) && end.isAfter(startTime);
    }

    public boolean isActive() {
        return status == AssignmentStatus.ASSIGNED || status == AssignmentStatus.IN_USE;
    }

    public enum AssignmentStatus {
        ASSIGNED, IN_USE, COMPLETED, CANCELLED, RETURNED_DAMAGED
    }
}
