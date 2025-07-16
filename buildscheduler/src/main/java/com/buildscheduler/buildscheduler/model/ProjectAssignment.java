package com.buildscheduler.buildscheduler.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectAssignment extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "assigned_by_id", nullable = false)
    private User assignedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssignmentRole assignmentRole;

    @Column(nullable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Column(nullable = false)
    private boolean isActive = true;

    public enum AssignmentRole {
        SITE_SUPERVISOR, EQUIPMENT_MANAGER
    }
}