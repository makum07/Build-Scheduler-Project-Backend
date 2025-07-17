package com.buildscheduler.buildscheduler.dto.project_manager;


import com.buildscheduler.buildscheduler.model.Subtask.TaskStatus;
import lombok.Data;

@Data
public class SubtaskResponseDto {
    private Long id;
    private String title;
    private TaskStatus status;
    private double completionPercentage;
    private boolean overdue;
}