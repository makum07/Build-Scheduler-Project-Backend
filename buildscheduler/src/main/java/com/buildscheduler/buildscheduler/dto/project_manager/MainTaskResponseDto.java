package com.buildscheduler.buildscheduler.dto.project_manager;

import com.buildscheduler.buildscheduler.model.MainTask.TaskStatus;

import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class MainTaskResponseDto {
    private Long id;
    private String title;
    private String description;
    private Long projectId;
    private Long supervisorId;
    private String supervisorName;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
    private LocalDate actualStartDate;
    private LocalDate actualEndDate;
    private TaskStatus status;
    private Integer priority;
    private Integer estimatedHours;
    private Integer actualHours;
    private double completionPercentage;  // Added
    private boolean overdue;              // Added
}