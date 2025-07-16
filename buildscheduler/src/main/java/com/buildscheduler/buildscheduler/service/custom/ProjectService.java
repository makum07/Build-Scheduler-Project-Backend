package com.buildscheduler.buildscheduler.service.custom;

import com.buildscheduler.buildscheduler.dto.project_manager.ProjectRequestDto;
import com.buildscheduler.buildscheduler.dto.project_manager.ProjectResponseDto;
import com.buildscheduler.buildscheduler.dto.project_manager.ProjectStructureResponse;
import com.buildscheduler.buildscheduler.model.Project;
import com.buildscheduler.buildscheduler.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectService {
    ProjectResponseDto createProject(ProjectRequestDto dto, User manager);
    ProjectResponseDto updateProject(Long id, ProjectRequestDto dto);
    void deleteProject(Long id);
    ProjectResponseDto getProjectById(Long id);
    Page<ProjectResponseDto> getProjectsByManager(User manager, Pageable pageable);
    void assignSupervisor(Long projectId, Long supervisorId);
    void assignEquipmentManager(Long projectId, Long equipmentManagerId);
    ProjectStructureResponse getProjectStructure(Long id);
}