package com.buildscheduler.buildscheduler.dto.site_supervisor;

import com.buildscheduler.buildscheduler.model.Equipment;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EquipmentNeedDto {
    private Equipment.EquipmentType requiredType;
    private LocalDateTime requiredStartTime;
    private LocalDateTime requiredEndTime;
    private String requestNotes;
    private Integer priority;
}