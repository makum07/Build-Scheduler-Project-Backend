package com.buildscheduler.buildscheduler.dto.project_manager;

import com.buildscheduler.buildscheduler.dto.worker.SimpleUserDto;
import com.buildscheduler.buildscheduler.model.Project.ProjectStatus;
import com.buildscheduler.buildscheduler.model.User;
import com.buildscheduler.buildscheduler.model.MainTask;
import com.buildscheduler.buildscheduler.model.ProjectAssignment;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
public class ProjectResponseDto {
    private Long id;
    private String title;
    private String description;

    private Long projectManagerId;
    private String projectManagerName;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate actualStartDate;
    private LocalDate actualEndDate;

    private ProjectStatus status;
    private BigDecimal estimatedBudget;
    private BigDecimal actualBudget;
    private String location;
    private Integer priority;
    private double completionPercentage;
    private boolean overdue;

    private SimpleUserDto siteSupervisor;
    private SimpleUserDto equipmentManager;
    private List<MainTaskResponseDto> mainTasks;
    private List<ProjectAssignmentDto> projectAssignments;
}
