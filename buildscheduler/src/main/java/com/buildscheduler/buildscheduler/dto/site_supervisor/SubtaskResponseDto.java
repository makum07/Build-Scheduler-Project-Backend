package com.buildscheduler.buildscheduler.dto.site_supervisor;

import com.buildscheduler.buildscheduler.model.Equipment;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class SubtaskResponseDto {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime plannedStartTime;
    private LocalDateTime plannedEndTime;
    private Integer estimatedHours;
    private Integer requiredWorkers;
    private Integer priority;
    private String status;
    private Long mainTaskId;
    private Long projectId;
    private Set<String> requiredSkills;


    private Set<EquipmentNeedDto> equipmentNeeds;
}