package com.buildscheduler.buildscheduler.mapper;

import com.buildscheduler.buildscheduler.dto.equipment_manager.EquipmentResponseDto;
import com.buildscheduler.buildscheduler.model.Equipment;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class EquipmentMapper {

    public EquipmentResponseDto toResponseDto(Equipment entity) {
        if (entity == null) {
            return null;
        }
        EquipmentResponseDto dto = new EquipmentResponseDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setModel(entity.getModel());
        dto.setSerialNumber(entity.getSerialNumber());
        dto.setType(entity.getType());
        dto.setPurchasePrice(entity.getPurchasePrice());
        dto.setWarrantyMonths(entity.getWarrantyMonths());
        dto.setMaintenanceIntervalDays(entity.getMaintenanceIntervalDays());
        dto.setLastMaintenanceDate(entity.getLastMaintenanceDate());
        dto.setMaintenanceDue(entity.isMaintenanceDue());
        dto.setLocation(entity.getLocation());
        dto.setNotes(entity.getNotes());
        return dto;
    }

    public List<EquipmentResponseDto> toResponseDtoList(Set<Equipment> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }
        return entities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }
}