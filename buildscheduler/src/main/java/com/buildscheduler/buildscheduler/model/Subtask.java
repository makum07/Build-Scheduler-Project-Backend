package com.buildscheduler.buildscheduler.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "subtasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subtask extends BaseEntity {
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

    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.PLANNED;

    @Column(nullable = false)
    private Integer estimatedHours = 0;

    @Column(nullable = false)
    private Integer actualHours = 0;

    @Column(nullable = false)
    private Integer requiredWorkers = 1;

    @Column(nullable = false)
    private Integer priority = 1;

    // Relationships
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "subtask_required_skills",
            joinColumns = @JoinColumn(name = "subtask_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private Set<Skill> requiredSkills = new HashSet<>();

    @OneToMany(mappedBy = "subtask", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Assignment> assignments = new HashSet<>();

//    @OneToMany(mappedBy = "subtask", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private Set<EquipmentAssignment> equipmentAssignments = new HashSet<>();
//
//    @OneToMany(mappedBy = "subtask", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private Set<EquipmentRequest> equipmentRequests = new HashSet<>();



    public boolean hasRequiredSkills(Set<Skill> workerSkills) {
        return workerSkills.containsAll(requiredSkills);
    }

    public enum TaskStatus {
        PLANNED, ASSIGNED, IN_PROGRESS, ON_HOLD, COMPLETED, CANCELLED, DELAYED
    }
}
