package com.buildscheduler.buildscheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    /**
     * Prevent Jackson from climbing back up into Subtask
     * while Hibernate is still mutating its collection.
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "subtask_id", nullable = false)
    @JsonIgnore
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