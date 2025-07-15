package com.buildscheduler.buildscheduler.mapper;

import com.buildscheduler.buildscheduler.dto.site_supervisor.AssignmentDto;
import com.buildscheduler.buildscheduler.model.Assignment;
import org.springframework.stereotype.Component;

@Component
public class AssignmentMapper implements Mapper<Assignment, AssignmentDto> {
    @Override
    public AssignmentDto toDto(Assignment entity) {
        AssignmentDto dto = new AssignmentDto();
        dto.setId(entity.getId());
        if (entity.getWorker() != null) {
            dto.setWorkerId(entity.getWorker().getId());
        }
        if (entity.getMainTask() != null) {
            dto.setTaskId(entity.getMainTask().getId());
        }
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        if (entity.getStatus() != null) {
            dto.setStatus(entity.getStatus().name());
        }
        return dto;
    }

    @Override
    public Assignment toEntity(AssignmentDto dto) {
        Assignment entity = new Assignment();
        entity.setId(dto.getId());
        entity.setStartTime(dto.getStartTime());
        entity.setEndTime(dto.getEndTime());
        if (dto.getStatus() != null) {
            entity.setStatus(Assignment.AssignmentStatus.valueOf(dto.getStatus()));
        }
        return entity;
    }
}