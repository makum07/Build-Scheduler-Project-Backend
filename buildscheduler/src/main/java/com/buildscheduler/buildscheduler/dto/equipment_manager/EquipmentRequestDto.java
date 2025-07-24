package com.buildscheduler.buildscheduler.dto.equipment_manager;

import com.buildscheduler.buildscheduler.model.Equipment.EquipmentStatus;
import com.buildscheduler.buildscheduler.model.Equipment.EquipmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EquipmentRequestDto {
    @NotBlank(message = "Equipment name is required")
    private String name;

    @NotBlank(message = "Model is required")
    private String model;

    private String serialNumber;

    @NotNull(message = "Equipment type is required")
    private EquipmentType type;

    // Status can be omitted for creation, as it defaults to AVAILABLE
    private EquipmentStatus status;

    @PositiveOrZero(message = "Purchase price cannot be negative")
    private BigDecimal purchasePrice;

    @Positive(message = "Warranty months must be positive")
    @NotNull(message = "Warranty months is required")
    private Integer warrantyMonths;

    @Positive(message = "Maintenance interval days must be positive")
    @NotNull(message = "Maintenance interval days is required")
    private Integer maintenanceIntervalDays;

    private LocalDate lastMaintenanceDate; // Optional for new equipment

    private String location;
    private String notes;
}