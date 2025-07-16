package com.buildscheduler.buildscheduler.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "time_entries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeEntry extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Column(nullable = false)
    private Integer hoursWorked;

    @Column(length = 500)
    private String workDescription;

    @Column(nullable = false)
    private boolean approved = false;

    @ManyToOne
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;

    private LocalDateTime approvedAt;

    // Helper methods
    public Integer calculateHours() {
        return (int) Duration.between(startTime, endTime).toHours();
    }
}