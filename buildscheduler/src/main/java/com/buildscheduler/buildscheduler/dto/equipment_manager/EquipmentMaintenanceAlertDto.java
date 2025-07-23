package com.buildscheduler.buildscheduler.dto.equipment_manager;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EquipmentMaintenanceAlertDto {
    private Long equipmentId;
    private String equipmentName;
    private String serialNumber;
    private LocalDate lastMaintenanceDate;
    private Integer maintenanceIntervalDays;
    private LocalDate nextExpectedMaintenanceDate;
    private String alertMessage;
}