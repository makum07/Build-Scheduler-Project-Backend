package com.buildscheduler.buildscheduler.dto.worker;

import com.buildscheduler.buildscheduler.model.Project;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectForWorkerDto {
    private Long id;
    private String title;
    private String description;
    private Project.ProjectStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private Integer priority;
    private double completionPercentage;
}