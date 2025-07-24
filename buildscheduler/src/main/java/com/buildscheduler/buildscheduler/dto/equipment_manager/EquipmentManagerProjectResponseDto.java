package com.buildscheduler.buildscheduler.dto.equipment_manager;

import com.buildscheduler.buildscheduler.dto.worker.SimpleUserDto;
import com.buildscheduler.buildscheduler.model.Project;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EquipmentManagerProjectResponseDto {
    private Long id;
    private String title;
    private String description;
    private SimpleUserDto siteSupervisor;
    private SimpleUserDto equipmentManager; // This will be the current user
    private Long projectManagerId;
    private String projectManagerName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Project.ProjectStatus status;
    private BigDecimal estimatedBudget;
    private String location;
    private Integer priority;
    private double completionPercentage; // The key metric
    private boolean isOverdue;
}