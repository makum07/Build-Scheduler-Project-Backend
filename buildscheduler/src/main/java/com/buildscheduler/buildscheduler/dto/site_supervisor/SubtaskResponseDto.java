package com.buildscheduler.buildscheduler.dto.site_supervisor;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
public class SubtaskResponseDto {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime plannedStart;
    private LocalDateTime plannedEnd;
    private String status;
    private Integer estimatedHours;
    private Integer requiredWorkers;
    private Integer priority;
    private Long mainTaskId;
    private Long projectId;
    private Set<String> requiredSkills = new HashSet<>();
    private Set<Long> equipmentIds = new HashSet<>();
    private String equipmentRequestNotes;

}