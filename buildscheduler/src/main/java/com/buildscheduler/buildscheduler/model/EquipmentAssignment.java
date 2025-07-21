package com.buildscheduler.buildscheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
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

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "subtask_id", nullable = false)
    private Subtask subtask;

    @ManyToOne(optional = false)
    @JoinColumn(name = "assigned_by_id", nullable = false)
    private User assignedBy;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(length = 1000)
    private String equipmentNotes;

    @PostPersist
    private void createAssignedNonAvailableSlot() {
        EquipmentNonAvailableSlot slot = new EquipmentNonAvailableSlot();
        slot.setEquipment(this.equipment);
        slot.setStartTime(this.startTime);
        slot.setEndTime(this.endTime);
        slot.setType(EquipmentNonAvailableSlot.NonAvailabilityType.ASSIGNED);
        slot.setNotes("Assigned to subtask: " + subtask.getTitle());

        // Add to equipment's non-available slots
        equipment.getNonAvailableSlots().add(slot);
    }

    public boolean overlapsWith(LocalDateTime checkStart, LocalDateTime checkEnd) {
        return !checkEnd.isBefore(startTime) && !checkStart.isAfter(endTime);
    }
}