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
@Table(name = "worker_availability_slots",
        indexes = {
                @Index(name = "idx_avail_date", columnList = "date"),
                @Index(name = "idx_avail_user_date", columnList = "user_id, date")
        })
@Getter @Setter @NoArgsConstructor
public class WorkerAvailabilitySlot extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    public boolean overlaps(LocalTime otherStart, LocalTime otherEnd) {
        return !otherEnd.isBefore(this.startTime) && !otherStart.isAfter(this.endTime);
    }

    // Check if slot covers a specific time range
    public boolean covers(LocalDateTime start, LocalDateTime end) {
        if (!date.equals(start.toLocalDate())) return false;
        LocalTime s = start.toLocalTime();
        LocalTime e = end.toLocalTime();
        return !s.isBefore(startTime) && !e.isAfter(endTime);
    }

    // Split around assignment
    public List<WorkerAvailabilitySlot> splitForAssignment(LocalDateTime aStart, LocalDateTime aEnd) {
        List<WorkerAvailabilitySlot> newSlots = new ArrayList<>();
        LocalTime as = aStart.toLocalTime(), ae = aEnd.toLocalTime();
        if (startTime.isBefore(as)) {
            newSlots.add(createNewSlot(startTime, as));
        }
        if (endTime.isAfter(ae)) {
            newSlots.add(createNewSlot(ae, endTime));
        }
        return newSlots;
    }

    private WorkerAvailabilitySlot createNewSlot(LocalTime ns, LocalTime ne) {
        WorkerAvailabilitySlot slot = new WorkerAvailabilitySlot();
        slot.setUser(user);
        slot.setDate(date);
        slot.setStartTime(ns);
        slot.setEndTime(ne);
        return slot;
    }
}
