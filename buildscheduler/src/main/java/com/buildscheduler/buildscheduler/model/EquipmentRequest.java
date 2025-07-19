package com.buildscheduler.buildscheduler.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentRequest extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "subtask_id", nullable = false)
    private Subtask subtask;

    @ManyToOne
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Equipment.EquipmentType requiredType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "requested_by_id", nullable = false)
    private User requestedBy;

    @ManyToOne
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;


    @Column(nullable = false)
    private LocalDateTime requiredStartTime;

    @Column(nullable = false)
    private LocalDateTime requiredEndTime;

    @Column(length = 1000)
    private String requestNotes;

    @Column(length = 1000)
    private String approvalNotes;

    @Column(nullable = false)
    private Integer priority = 1;

    // Helper methods
    public boolean isUrgent() {
        return priority >= 3;
    }

    public boolean isOverdue() {
        return status == RequestStatus.PENDING &&
                requiredStartTime.isBefore(LocalDateTime.now().plusHours(24));
    }

    public enum RequestStatus {
        PENDING, APPROVED, REJECTED, FULFILLED, CANCELLED
    }
}
