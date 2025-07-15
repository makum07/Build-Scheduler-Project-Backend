package com.buildscheduler.buildscheduler.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tasks")
@Getter @Setter @NoArgsConstructor
public class MainTask extends BaseEntity {
    @NotBlank
    private String title;
    @Column(length = 2000)
    private String description;
//    @ManyToOne(optional = false)
//    @JoinColumn(name = "project_id", nullable = false)
//    private Project project;
    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    private User siteSupervisor;
//    @OneToMany(mappedBy = "mainTask", cascade = CascadeType.ALL)
//    private Set<Subtask> subtasks = new HashSet<>();

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.PENDING;



    public enum TaskStatus { PENDING, IN_PROGRESS, COMPLETED, CANCELLED }
}