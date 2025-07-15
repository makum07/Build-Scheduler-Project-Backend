package com.buildscheduler.buildscheduler.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignments",
        indexes = {
                @Index(name = "idx_assignment_worker", columnList = "worker_id"),
                @Index(name = "idx_assignment_task", columnList = "task_id"),
                @Index(name = "idx_assignment_time", columnList = "start_time, end_time")
        })
@Getter @Setter @NoArgsConstructor
public class Assignment extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "worker_id", nullable = false)
    private User worker;

    @ManyToOne(optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private MainTask mainTask;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status = AssignmentStatus.ASSIGNED;

    public enum AssignmentStatus { ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED }
}