package com.buildscheduler.buildscheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "subtasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Subtask extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "main_task_id", nullable = false)
    private MainTask mainTask;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private LocalTime plannedStartTime;

    @Column(nullable = false)
    private LocalTime plannedEndTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.PLANNED;

    @Column(nullable = false)
    private Integer estimatedHours = 0;

    @Column(nullable = false)
    private Integer requiredWorkers = 1;

    @Column(nullable = false)
    private Integer priority = 1;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "subtask_required_skills",
            joinColumns = @JoinColumn(name = "subtask_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> requiredSkills = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "subtask", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<WorkerAssignment> workerAssignments = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "subtask", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<EquipmentAssignment> equipmentAssignments = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "subtask_equipment_needs",
            joinColumns = @JoinColumn(name = "subtask_id"),
            inverseJoinColumns = @JoinColumn(name = "equipment_id")
    )
    private Set<Equipment> equipmentNeeds = new HashSet<>();

    @Column(length = 1000)
    private String equipmentRequestNotes;

    public enum TaskStatus {
        PLANNED, ASSIGNED, IN_PROGRESS, ON_HOLD, COMPLETED, CANCELLED, DELAYED
    }

    public boolean isOverdue() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime subtaskEndDateTime = LocalDateTime.of(endDate, endTime);
        return status != TaskStatus.COMPLETED && now.isAfter(subtaskEndDateTime);
    }
}
