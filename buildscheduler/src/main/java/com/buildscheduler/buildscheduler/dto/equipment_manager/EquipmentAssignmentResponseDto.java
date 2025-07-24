package com.buildscheduler.buildscheduler.dto.equipment_manager;

import com.buildscheduler.buildscheduler.dto.equipment_manager.SubtaskForEquipmentAssignmentDto;
import com.buildscheduler.buildscheduler.dto.worker.SimpleUserDto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EquipmentAssignmentResponseDto {
    private Long id;
    private Long equipmentId;
    private SubtaskForEquipmentAssignmentDto subtask;
    private SimpleUserDto assignedBy;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String equipmentNotes;
}