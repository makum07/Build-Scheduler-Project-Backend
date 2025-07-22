package com.buildscheduler.buildscheduler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "subtasks")
@Getter // Use @Getter and @Setter instead of @Data
@Setter
@ToString(callSuper = true) // Include BaseEntity toString
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
    private LocalDateTime plannedStart;

    @Column(nullable = false)
    private LocalDateTime plannedEnd;

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
        return LocalDateTime.now().isAfter(plannedEnd)
                && status != TaskStatus.COMPLETED
                && status != TaskStatus.CANCELLED;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        // Use getClass() equality for safety with proxies, or check if o is instance of HibernateProxy
        // If using proxies, 'o instanceof Subtask' might not work as expected with uninitialized proxies.
        // Using o.getClass() != getClass() can be safer.
        // For a proxy, getClass() returns the proxy class, not the entity class.
        // A more robust check: if (o == null || (o instanceof HibernateProxy && !Hibernate.isInitialized(o))) return false;
        // However, relying on ID for equality is the most common approach for JPA entities.
        if (o == null || getClass() != o.getClass()) return false;
        Subtask subtask = (Subtask) o;
        // Crucially, use the ID for equality. If ID is null (new entity), rely on object identity.
        return getId() != null && getId().equals(subtask.getId());
    }

    @Override
    public final int hashCode() {
        // Use the ID for hashCode. If ID is null, return 0 (or a constant).
        return getId() != null ? getId().hashCode() : 0;
    }

}