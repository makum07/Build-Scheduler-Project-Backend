package com.buildscheduler.buildscheduler.mapper;

import com.buildscheduler.buildscheduler.dto.worker.AvailabilitySlotDto;
import com.buildscheduler.buildscheduler.model.WorkerAvailabilitySlot;
import org.springframework.stereotype.Component;

@Component
public class AvailabilitySlotMapper implements Mapper<WorkerAvailabilitySlot, AvailabilitySlotDto> {
    @Override
    public AvailabilitySlotDto toDto(WorkerAvailabilitySlot entity) {
        AvailabilitySlotDto dto = new AvailabilitySlotDto();
        dto.setId(entity.getId());
        dto.setDate(entity.getDate());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        return dto;
    }

    @Override
    public WorkerAvailabilitySlot toEntity(AvailabilitySlotDto dto) {
        WorkerAvailabilitySlot entity = new WorkerAvailabilitySlot();
        entity.setId(dto.getId());
        entity.setDate(dto.getDate());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        return entity;
    }
}