package com.buildscheduler.buildscheduler.mapper;

import com.buildscheduler.buildscheduler.dto.project_manager.FullProjectResponseDto;
import com.buildscheduler.buildscheduler.dto.worker.SimpleUserDto;
import com.buildscheduler.buildscheduler.model.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public FullProjectResponseDto toFullProjectDto(Project project) {
        FullProjectResponseDto dto = new FullProjectResponseDto();

        dto.setId(project.getId());
        dto.setTitle(project.getTitle());
        dto.setDescription(project.getDescription());
        dto.setStartDate(project.getStartDate());
        dto.setEndDate(project.getEndDate());
        dto.setActualStartDate(project.getActualStartDate());
        dto.setActualEndDate(project.getActualEndDate());
        dto.setStatus(project.getStatus());
        dto.setEstimatedBudget(project.getEstimatedBudget());
        dto.setActualBudget(project.getActualBudget());
        dto.setLocation(project.getLocation());
        dto.setPriority(project.getPriority());

        if (project.getProjectManager() != null) {
            dto.setProjectManager(SimpleUserDto.builder()
                    .id(project.getProjectManager().getId())
                    .username(project.getProjectManager().getUsername())
                    .email(project.getProjectManager().getEmail())
                    .build());
        }

        if (project.getSiteSupervisor() != null) {
            dto.setSiteSupervisor(SimpleUserDto.builder()
                    .id(project.getSiteSupervisor().getId())
                    .username(project.getSiteSupervisor().getUsername())
                    .email(project.getSiteSupervisor().getEmail())
                    .build());
        }

        if (project.getEquipmentManager() != null) {
            dto.setEquipmentManager(SimpleUserDto.builder()
                    .id(project.getEquipmentManager().getId())
                    .username(project.getEquipmentManager().getUsername())
                    .email(project.getEquipmentManager().getEmail())
                    .build());
        }

        return dto;
    }
}
