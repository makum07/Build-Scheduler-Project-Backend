package com.buildscheduler.buildscheduler.model;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "main_tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MainTask extends BaseEntity {
    @NotBlank
    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    private User siteSupervisor;

    @Column(nullable = false)
    private LocalDate plannedStartDate;

    @Column(nullable = false)
    private LocalDate plannedEndDate;

    private LocalDate actualStartDate;
    private LocalDate actualEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.PLANNED;

    @Column(nullable = false)
    private Integer priority = 1;

    @Column(nullable = false)
    private Integer estimatedHours = 0;

    @Column(nullable = false)
    private Integer actualHours = 0;

    @OneToMany(mappedBy = "mainTask", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Subtask> subtasks = new HashSet<>();



    public enum TaskStatus {
        PLANNED, IN_PROGRESS, ON_HOLD, COMPLETED, CANCELLED, DELAYED
    }
}
