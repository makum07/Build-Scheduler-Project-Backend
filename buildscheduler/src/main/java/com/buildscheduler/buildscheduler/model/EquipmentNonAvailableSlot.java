package com.buildscheduler.buildscheduler.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipment_non_available_slot")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentNonAvailableSlot extends BaseEntity {

    public enum NonAvailabilityType {
        MAINTENANCE, ASSIGNED
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NonAvailabilityType type;

    @Column(length = 1000)
    private String notes;

    public boolean overlapsWith(LocalDateTime checkStart, LocalDateTime checkEnd) {
        return !checkEnd.isBefore(startTime) && !checkStart.isAfter(endTime);
    }
}