package com.buildscheduler.buildscheduler.dto.equipment_manager;

import com.buildscheduler.buildscheduler.model.EquipmentNonAvailableSlot;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EquipmentNonAvailableSlotResponseDto {
    private Long id;
    private Long equipmentId; // To know which equipment this slot belongs to
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private EquipmentNonAvailableSlot.NonAvailabilityType type;
    private String notes;
}