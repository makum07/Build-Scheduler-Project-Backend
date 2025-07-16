package com.buildscheduler.buildscheduler.model;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Assignment extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "worker_id", nullable = false)
    private User worker;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subtask_id", nullable = false)
    private Subtask subtask;

    // Add this relationship to MainTask
    @ManyToOne
    @JoinColumn(name = "main_task_id")
    private MainTask mainTask;

    @ManyToOne(optional = false)
    @JoinColumn(name = "assigned_by_id", nullable = false)
    private User assignedBy;

    @Column(nullable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Column(nullable = false)
    private boolean accepted = false;

    private LocalDateTime acceptedAt;

    @Column(nullable = false)
    private boolean completed = false;

    private LocalDateTime completedAt;

    // Add startTime and endTime fields
    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(length = 1000)
    private String completionNotes;

    @Column(length = 1000)
    private String workerNotes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentStatus status = AssignmentStatus.ASSIGNED;

    @Column(nullable = false)
    private Integer hoursWorked = 0;

    @Column(precision = 5, scale = 2)
    private BigDecimal performanceRating; // 1.0 to 5.0

    @Column(length = 500)
    private String performanceNotes;

    // Time tracking
    @OneToMany(mappedBy = "assignment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<TimeEntry> timeEntries = new HashSet<>();

    // Helper methods
    public boolean isOverdue() {
        return subtask.getPlannedEndTime().isBefore(LocalDateTime.now()) &&
                status != AssignmentStatus.COMPLETED && status != AssignmentStatus.CANCELLED;
    }

    public boolean canBeAccepted() {
        return status == AssignmentStatus.ASSIGNED && !accepted;
    }

    public boolean canBeCompleted() {
        return status == AssignmentStatus.IN_PROGRESS && accepted;
    }

    public enum AssignmentStatus {
        ASSIGNED, ACCEPTED, IN_PROGRESS, COMPLETED, CANCELLED, REJECTED
    }
}