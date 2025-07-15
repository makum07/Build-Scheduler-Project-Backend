package com.buildscheduler.buildscheduler.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "availability_slots",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "date"}),
        indexes = {
                @Index(name = "idx_avail_date", columnList = "date"),
                @Index(name = "idx_avail_user_date", columnList = "user_id, date")
        })
@Getter @Setter @NoArgsConstructor
public class AvailabilitySlot extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    // Check if slot covers a specific time range
    public boolean covers(LocalDateTime start, LocalDateTime end) {
        if (!date.equals(start.toLocalDate())) return false;
        LocalTime s = start.toLocalTime();
        LocalTime e = end.toLocalTime();
        return !s.isBefore(startTime) && !e.isAfter(endTime);
    }

    // Split around assignment
    public List<AvailabilitySlot> splitForAssignment(LocalDateTime aStart, LocalDateTime aEnd) {
        List<AvailabilitySlot> newSlots = new ArrayList<>();
        LocalTime as = aStart.toLocalTime(), ae = aEnd.toLocalTime();
        if (startTime.isBefore(as)) {
            newSlots.add(createNewSlot(startTime, as));
        }
        if (endTime.isAfter(ae)) {
            newSlots.add(createNewSlot(ae, endTime));
        }
        return newSlots;
    }

    private AvailabilitySlot createNewSlot(LocalTime ns, LocalTime ne) {
        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setUser(user);
        slot.setDate(date);
        slot.setStartTime(ns);
        slot.setEndTime(ne);
        return slot;
    }
}
