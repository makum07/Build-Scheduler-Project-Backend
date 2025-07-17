package com.buildscheduler.buildscheduler.dto.project_manager;

import com.buildscheduler.buildscheduler.dto.worker.SimpleUserDto;
import com.buildscheduler.buildscheduler.model.Project.ProjectStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FullProjectResponseDto {
    private Long id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate actualStartDate;
    private LocalDate actualEndDate;
    private ProjectStatus status;
    private BigDecimal estimatedBudget;
    private BigDecimal actualBudget;
    private String location;
    private Integer priority;
    private double completionPercentage;  // Added
    private boolean overdue;             // Added
    private SimpleUserDto projectManager;
    private SimpleUserDto siteSupervisor;
    private SimpleUserDto equipmentManager;
}
