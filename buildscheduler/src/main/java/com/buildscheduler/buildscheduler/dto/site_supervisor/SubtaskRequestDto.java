package com.buildscheduler.buildscheduler.dto.site_supervisor;

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
    private LocalDateTime plannedStart;
    private LocalDateTime plannedEnd;
    private Integer estimatedHours = 0;
    private Integer requiredWorkers = 1;
    private Integer priority = 1;
    private Set<String> requiredSkills = new HashSet<>();
    private Set<Long> equipmentIds = new HashSet<>();
    private String equipmentRequestNotes;
}