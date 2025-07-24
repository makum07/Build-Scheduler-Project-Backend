package com.buildscheduler.buildscheduler.dto.equipment_manager;

import com.buildscheduler.buildscheduler.dto.worker.SimpleUserDto;
import com.buildscheduler.buildscheduler.model.Equipment.EquipmentType;
// Import the actual EquipmentStatus enum, but we'll calculate it
import com.buildscheduler.buildscheduler.model.Equipment.EquipmentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime; // New import
import java.util.Set; // New import for relevant slots/assignments

// Assuming these are needed for dynamic status calculation, you might pass them in the constructor
// or fetch them in the service layer before mapping.
// For simplicity, we'll assume the service fetches them.

@Data
public class EquipmentResponseDto {
    private Long id;
    private String name;
    private String model;
    private String serialNumber;
    private EquipmentType type;
    // We will no longer set 'status' directly from the entity here.
    // Instead, we'll calculate it in the service or provide a getter logic.
    private EquipmentStatus currentOperationalStatus; // This will be the dynamic status

    private SimpleUserDto equipmentManager;
    private BigDecimal purchasePrice;
    private Integer warrantyMonths;
    private Integer maintenanceIntervalDays;
    private LocalDate lastMaintenanceDate;
    private String location;
    private String notes;

    // We might also include these if the client needs to inspect them
    // private Set<EquipmentNonAvailableSlot> nonAvailableSlots;
    // private Set<EquipmentAssignment> assignments;

    // You might also expose if it's maintenance due as a separate flag,
    // as it's a "warning" rather than a strict "operational status"
    private boolean maintenanceDue;
    private Set<EquipmentNonAvailableSlotResponseDto> nonAvailableSlots;
    private Set<EquipmentAssignmentResponseDto> assignments;
}