package com.buildscheduler.buildscheduler.dto.equipment_manager;

import com.buildscheduler.buildscheduler.dto.worker.SimpleUserDto;
import com.buildscheduler.buildscheduler.model.Equipment.EquipmentStatus;
import com.buildscheduler.buildscheduler.model.Equipment.EquipmentType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EquipmentResponseDto {
    private Long id;
    private String name;
    private String model;
    private String serialNumber;
    private EquipmentType type;
    private EquipmentStatus status;
    private SimpleUserDto equipmentManager; // Use SimpleUserDto for manager details
    private BigDecimal purchasePrice;
    private Integer warrantyMonths;
    private Integer maintenanceIntervalDays;
    private LocalDate lastMaintenanceDate;
    private String location;
    private String notes;
}