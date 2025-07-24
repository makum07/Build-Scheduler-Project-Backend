package com.buildscheduler.buildscheduler.dto.equipment_manager;

import com.buildscheduler.buildscheduler.model.EquipmentNonAvailableSlot;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EquipmentNonAvailableSlotRequestDto {
    @NotNull(message = "Start time is required")
    @FutureOrPresent(message = "Start time cannot be in the past")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    @NotNull(message = "Non-availability type is required")
    private EquipmentNonAvailableSlot.NonAvailabilityType type;

    private String notes;
}