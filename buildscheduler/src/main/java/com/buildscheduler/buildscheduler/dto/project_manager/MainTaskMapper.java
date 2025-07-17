package com.buildscheduler.buildscheduler.dto.project_manager;

import com.buildscheduler.buildscheduler.dto.project_manager.MainTaskResponseDto;
import com.buildscheduler.buildscheduler.dto.worker.SimpleUserDto;
import com.buildscheduler.buildscheduler.model.MainTask;

import org.springframework.stereotype.Component;

@Component
public class MainTaskMapper {

    public MainTaskResponseDto toMainTaskResponseDto(MainTask mainTask) {
        if (mainTask == null) {
            return null;
        }

        MainTaskResponseDto dto = new MainTaskResponseDto();
        dto.setId(mainTask.getId());
        dto.setTitle(mainTask.getTitle());
        dto.setDescription(mainTask.getDescription());
        dto.setProjectId(mainTask.getProject() != null ? mainTask.getProject().getId() : null);
        dto.setPlannedStartDate(mainTask.getPlannedStartDate());
        dto.setPlannedEndDate(mainTask.getPlannedEndDate());
        dto.setActualStartDate(mainTask.getActualStartDate());
        dto.setActualEndDate(mainTask.getActualEndDate());
        dto.setStatus(mainTask.getStatus());
        dto.setPriority(mainTask.getPriority());
        dto.setEstimatedHours(mainTask.getEstimatedHours());
        dto.setActualHours(mainTask.getActualHours());

        // Map supervisor info
        if (mainTask.getSiteSupervisor() != null) {
            SimpleUserDto supervisorDto = new SimpleUserDto();
            supervisorDto.setId(mainTask.getSiteSupervisor().getId());
            supervisorDto.setUsername(mainTask.getSiteSupervisor().getUsername());
            supervisorDto.setEmail(mainTask.getSiteSupervisor().getEmail());
            dto.setSupervisorId(supervisorDto.getId());
            dto.setSupervisorName(supervisorDto.getUsername());
        } else {
            dto.setSupervisorId(null);
            dto.setSupervisorName(null);
        }

        // These will be set later in service logic
        dto.setCompletionPercentage(0.0); // Will be updated dynamically
        dto.setOverdue(false); // Will be updated dynamically

        return dto;
    }
}