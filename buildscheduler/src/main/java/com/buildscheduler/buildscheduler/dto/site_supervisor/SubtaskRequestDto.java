package com.buildscheduler.buildscheduler.dto.site_supervisor;

import com.buildscheduler.buildscheduler.model.Equipment;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
public class SubtaskRequestDto {
    @NotBlank
    private String title;

    private String description;

    @NotNull
    private LocalDateTime plannedStartTime;

    @NotNull
    private LocalDateTime plannedEndTime;

    @Min(1)
    private Integer estimatedHours;

    @Min(1)
    private Integer requiredWorkers;

    @Min(1) @Max(5)
    private Integer priority;

    @NotEmpty
    private Set<String> requiredSkills;

    private Set<EquipmentNeedDto> equipmentNeeds = new HashSet<>();
}