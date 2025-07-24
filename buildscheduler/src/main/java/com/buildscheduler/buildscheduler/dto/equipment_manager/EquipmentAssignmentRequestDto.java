package com.buildscheduler.buildscheduler.dto.equipment_manager;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EquipmentAssignmentRequestDto {
    @NotNull(message = "Subtask ID is required")
    private Long subtaskId;

    @NotNull(message = "Start time is required")
    @FutureOrPresent(message = "Start time cannot be in the past")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    private String equipmentNotes;
}