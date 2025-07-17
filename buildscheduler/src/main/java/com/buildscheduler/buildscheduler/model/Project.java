package com.buildscheduler.buildscheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project extends BaseEntity {
    @NotBlank
    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_supervisor_id")
    private User siteSupervisor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_manager_id")
    private User equipmentManager;

    @Column(length = 2000)
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "manager_id", nullable = false)
    private User projectManager;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDate actualStartDate;
    private LocalDate actualEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status = ProjectStatus.PLANNING;

    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedBudget;

    @Column(precision = 10, scale = 2)
    private BigDecimal actualBudget;

    private String location;

    @Column(nullable = false)
    private Integer priority = 1; // 1=Low, 2=Medium, 3=High, 4=Critical

    // Relationships
    @JsonIgnore
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<MainTask> mainTasks = new HashSet<>();
    @JsonIgnore
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ProjectAssignment> projectAssignments = new HashSet<>();

//    // Helper methods
//    public double getCompletionPercentage() {
//        if (mainTasks.isEmpty()) return 0.0;
//        return mainTasks.stream()
//                .mapToDouble(MainTask::getCompletionPercentage)
//                .average()
//                .orElse(0.0);
//    }
//
//    public boolean isOverdue() {
//        return endDate != null && endDate.isBefore(LocalDate.now()) &&
//                status != ProjectStatus.COMPLETED && status != ProjectStatus.CANCELLED;
//    }

    public enum ProjectStatus {
        PLANNING, APPROVED, IN_PROGRESS, ON_HOLD, COMPLETED, CANCELLED, DELAYED
    }
}
