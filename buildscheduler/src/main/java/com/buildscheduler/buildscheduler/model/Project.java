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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "project_workers",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "worker_id")
    )
    private Set<User> workers = new HashSet<>();


    @Column(length = 2000)
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "manager_id", nullable = false)
    private User projectManager;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status = ProjectStatus.PLANNING;

    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedBudget;

    private String location;

    @Column(nullable = false)
    private Integer priority = 1; // 1=Low, 2=Medium, 3=High, 4=Critical

    // Relationships
    @JsonIgnore
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<MainTask> mainTasks = new HashSet<>();

    public enum ProjectStatus {
        PLANNING, APPROVED, IN_PROGRESS, ON_HOLD, COMPLETED, CANCELLED, DELAYED
    }
}
