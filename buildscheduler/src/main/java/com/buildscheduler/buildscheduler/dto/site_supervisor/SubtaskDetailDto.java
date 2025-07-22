package com.buildscheduler.buildscheduler.dto.site_supervisor;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter @Setter
public class SubtaskDetailDto {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime plannedStart;
    private LocalDateTime plannedEnd;
    private String status;
    private Integer estimatedHours;
    private Integer requiredWorkers;
    private Integer priority;
    private String equipmentRequestNotes;
    private Set<String> requiredSkills = new HashSet<>();
    private Set<SimpleEquipmentDto> equipmentNeeds = new HashSet<>();
    private Set<WorkerAssignmentDto> workerAssignments = new HashSet<>();
    private Set<EquipmentAssignmentDto> equipmentAssignments = new HashSet<>();
}