package com.buildscheduler.buildscheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "workerassignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkerAssignment extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "worker_id", nullable = false)
    private User worker;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "subtask_id", nullable = false)
    private Subtask subtask;

    @Column(nullable = false)
    private LocalDateTime assignmentStart;

    @Column(nullable = false)
    private LocalDateTime assignmentEnd;

    @ManyToOne(optional = false)
    @JoinColumn(name = "assigned_by_id", nullable = false)
    private User assignedBy;

    @Column(length = 1000)
    private String workerNotes;

    public boolean overlapsWith(LocalDateTime start, LocalDateTime end) {
        return !end.isBefore(assignmentStart) && !start.isAfter(assignmentEnd);
    }
}