package com.buildscheduler.buildscheduler.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(
        name = "equipment_non_available_slot",
        indexes = {
                @Index(name = "idx_equipment_date", columnList = "equipment_id, startDate")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentNonAvailableSlot extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    private NonAvailabilityType type;

    public boolean overlapsWith(LocalDateTime checkStart, LocalDateTime checkEnd) {
        LocalDateTime thisStart = LocalDateTime.of(startDate, startTime);
        LocalDateTime thisEnd   = LocalDateTime.of(endDate,   endTime);
        return !checkEnd.isBefore(thisStart) && !checkStart.isAfter(thisEnd);
    }

    public boolean isMaintenanceDue() {
        if (type != NonAvailabilityType.MAINTENANCE) return false;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.of(endDate, endTime);
        return now.isAfter(end);
    }


    public enum NonAvailabilityType {
        MAINTENANCE,ASSIGNED
    }
}

