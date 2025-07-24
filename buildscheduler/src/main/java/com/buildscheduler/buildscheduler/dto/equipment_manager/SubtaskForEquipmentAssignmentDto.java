package com.buildscheduler.buildscheduler.dto.equipment_manager;

import com.buildscheduler.buildscheduler.model.Subtask; // To get the TaskStatus enum if needed
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubtaskForEquipmentAssignmentDto {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime plannedStart;
    private LocalDateTime plannedEnd;
    private Subtask.TaskStatus status;
}