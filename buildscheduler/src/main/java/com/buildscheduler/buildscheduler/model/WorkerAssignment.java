package com.buildscheduler.buildscheduler.model;

import lombok.*;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(
        name = "worker_assignments",
        indexes = {
                @Index(name = "idx_worker_date", columnList = "worker_id, start_date"),
                @Index(name = "idx_subtask", columnList = "subtask_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkerAssignment extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "worker_id", nullable = false)
    private User worker;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subtask_id", nullable = false)
    private Subtask subtask;

    @ManyToOne(optional = false)
    @JoinColumn(name = "assigned_by_id", nullable = false)
    private User assignedBy;


    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(length = 1000)
    private String workerNotes;

    public boolean overlapsWith(LocalDate startDate, LocalTime startTime, LocalDate endDate, LocalTime endTime) {
        LocalDateTime thisStart = LocalDateTime.of(this.startDate, this.startTime);
        LocalDateTime thisEnd = LocalDateTime.of(this.endDate, this.endTime);

        LocalDateTime otherStart = LocalDateTime.of(startDate, startTime);
        LocalDateTime otherEnd = LocalDateTime.of(endDate, endTime);

        return !otherEnd.isBefore(thisStart) && !otherStart.isAfter(thisEnd);
    }


}
