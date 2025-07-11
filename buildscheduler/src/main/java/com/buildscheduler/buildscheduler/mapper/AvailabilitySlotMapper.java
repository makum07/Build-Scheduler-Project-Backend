package com.buildscheduler.buildscheduler.mapper;

import com.buildscheduler.buildscheduler.dto.AvailabilitySlotDto;
import com.buildscheduler.buildscheduler.model.AvailabilitySlot;
import org.springframework.stereotype.Component;

@Component
public class AvailabilitySlotMapper implements Mapper<AvailabilitySlot, AvailabilitySlotDto> {
    @Override
    public AvailabilitySlotDto toDto(AvailabilitySlot entity) {
        AvailabilitySlotDto dto = new AvailabilitySlotDto();
        dto.setId(entity.getId());
        dto.setDate(entity.getDate());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        return dto;
    }

    @Override
    public AvailabilitySlot toEntity(AvailabilitySlotDto dto) {
        AvailabilitySlot entity = new AvailabilitySlot();
        entity.setId(dto.getId());
        entity.setDate(dto.getDate());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        return entity;
    }
}