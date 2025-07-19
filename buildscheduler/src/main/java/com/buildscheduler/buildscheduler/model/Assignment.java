package com.buildscheduler.buildscheduler.model;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "assignments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Assignment extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(name = "worker_id", nullable = false)
    private User worker;

    @ManyToOne(optional = false)
    @JoinColumn(name = "subtask_id", nullable = false)
    private Subtask subtask;


    @ManyToOne(optional = false)
    @JoinColumn(name = "assigned_by_id", nullable = false)
    private User assignedBy;


    @Column(length = 1000)
    private String workerNotes;

}