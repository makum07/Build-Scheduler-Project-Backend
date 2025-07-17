package com.buildscheduler.buildscheduler.service.impl;

import com.buildscheduler.buildscheduler.dto.project_manager.ProjectRequestDto;
import com.buildscheduler.buildscheduler.dto.project_manager.ProjectResponseDto;
import com.buildscheduler.buildscheduler.exception.ResourceNotFoundException;
import com.buildscheduler.buildscheduler.mapper.UserMapper;
import com.buildscheduler.buildscheduler.model.Project;
import com.buildscheduler.buildscheduler.model.User;
import com.buildscheduler.buildscheduler.repository.ProjectRepository;
import com.buildscheduler.buildscheduler.repository.UserRepository;
import com.buildscheduler.buildscheduler.service.custom.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public ProjectResponseDto createProject(ProjectRequestDto dto, User manager) {
        Project project = new Project();
        mapDtoToEntity(dto, project);
        project.setProjectManager(manager);
        project.setStatus(Project.ProjectStatus.PLANNING);
        project = projectRepository.save(project);
        return mapEntityToDto(project);
    }

    @Override
    @Transactional
    public ProjectResponseDto updateProject(Long id, ProjectRequestDto dto) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        mapDtoToEntity(dto, project);
        project = projectRepository.save(project);
        return mapEntityToDto(project);
    }

    @Override
    @Transactional
    public void deleteProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        projectRepository.delete(project);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponseDto getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        return mapEntityToDto(project);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponseDto> getProjectsByManager(User manager, Pageable pageable) {
        return projectRepository.findByProjectManager(manager, pageable)
                .map(this::mapEntityToDto);
    }

    @Override
    @Transactional
    public void assignSupervisor(Long projectId, Long supervisorId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        User supervisor = userRepository.findById(supervisorId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", supervisorId));
        project.setSiteSupervisor(supervisor);
        projectRepository.save(project);
    }

    @Override
    @Transactional
    public void assignEquipmentManager(Long projectId, Long equipmentManagerId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        User equipmentManager = userRepository.findById(equipmentManagerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", equipmentManagerId));
        project.setEquipmentManager(equipmentManager);
        projectRepository.save(project);
    }

    // ─── DTO ↔ ENTITY Mapping ─────────────────────────────────────────────────

    private void mapDtoToEntity(ProjectRequestDto src, Project dest) {
        dest.setTitle(src.getTitle());
        dest.setDescription(src.getDescription());
        dest.setStartDate(src.getStartDate());
        dest.setEndDate(src.getEndDate());
        dest.setEstimatedBudget(src.getEstimatedBudget());
        dest.setLocation(src.getLocation());
        dest.setPriority(src.getPriority());
    }

    private ProjectResponseDto mapEntityToDto(Project entity) {
        ProjectResponseDto dto = new ProjectResponseDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setProjectManagerId(entity.getProjectManager().getId());
        dto.setProjectManagerName(entity.getProjectManager().getUsername());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setActualStartDate(entity.getActualStartDate());
        dto.setActualEndDate(entity.getActualEndDate());
        dto.setStatus(entity.getStatus());
        dto.setEstimatedBudget(entity.getEstimatedBudget());
        dto.setActualBudget(entity.getActualBudget());
        dto.setLocation(entity.getLocation());
        dto.setPriority(entity.getPriority());

        if (entity.getSiteSupervisor() != null) {
            dto.setSiteSupervisor(userMapper.toSimpleUserDto(entity.getSiteSupervisor()));
        }
        if (entity.getEquipmentManager() != null) {
            dto.setEquipmentManager(userMapper.toSimpleUserDto(entity.getEquipmentManager()));
        }
        return dto;
    }
}
