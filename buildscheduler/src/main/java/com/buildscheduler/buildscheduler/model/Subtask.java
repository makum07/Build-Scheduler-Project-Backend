package com.buildscheduler.buildscheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
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
    private LocalDateTime plannedStartTime;

    @Column(nullable = false)
    private LocalDateTime plannedEndTime;

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
    @JoinTable(name = "subtask_required_skills",
            joinColumns = @JoinColumn(name = "subtask_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private Set<Skill> requiredSkills = new HashSet<>();

    // 🧑‍🔧 Workers assigned
    @OneToMany(mappedBy = "subtask", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Assignment> assignments = new HashSet<>();

    // ⚙️ Equipments assigned
    @OneToMany(mappedBy = "subtask", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EquipmentAssignment> equipmentAssignments = new HashSet<>();

    @OneToMany(mappedBy = "subtask", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<EquipmentNeed> equipmentNeeds = new HashSet<>();


    public enum TaskStatus {
        PLANNED, ASSIGNED, IN_PROGRESS, ON_HOLD, COMPLETED, CANCELLED, DELAYED
    }

    public boolean isOverdue() {
        return plannedEndTime.isBefore(LocalDateTime.now())
                && status != TaskStatus.COMPLETED
                && status != TaskStatus.CANCELLED;
    }
}