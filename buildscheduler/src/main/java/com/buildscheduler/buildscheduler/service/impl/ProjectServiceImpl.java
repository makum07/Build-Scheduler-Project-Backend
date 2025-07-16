package com.buildscheduler.buildscheduler.service.impl;

import com.buildscheduler.buildscheduler.dto.project_manager.*;
import com.buildscheduler.buildscheduler.dto.worker.SimpleUserDto;
import com.buildscheduler.buildscheduler.exception.ResourceNotFoundException;
import com.buildscheduler.buildscheduler.mapper.UserMapper;
import com.buildscheduler.buildscheduler.model.*;
import com.buildscheduler.buildscheduler.repository.MainTaskRepository;
import com.buildscheduler.buildscheduler.repository.ProjectRepository;
import com.buildscheduler.buildscheduler.repository.UserRepository;
import com.buildscheduler.buildscheduler.service.custom.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final MainTaskRepository mainTaskRepository;
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
    @Transactional(readOnly = true) // Add this annotation
    public ProjectResponseDto getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        return mapEntityToDto(project);
    }

    @Override
    @Transactional(readOnly = true) // Add this annotation
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

    @Override
    public ProjectStructureResponse getProjectStructure(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        List<MainTask> mainTasks = mainTaskRepository.findByProject(project);

        ProjectStructureResponse response = new ProjectStructureResponse();
        response.setProject(mapEntityToDto(project)); // ✅ No mainTasks in DTO
        response.setMainTasks(mainTasks.stream()
                .map(this::mapMainTaskToDto)
                .collect(Collectors.toList()));

        return response;
    }

    private void mapDtoToEntity(ProjectRequestDto dto, Project entity) {
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setEstimatedBudget(dto.getEstimatedBudget());
        entity.setLocation(dto.getLocation());
        entity.setPriority(dto.getPriority());
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
//        dto.setCompletionPercentage(entity.getCompletionPercentage());
//        dto.setOverdue(entity.isOverdue());

        if (entity.getSiteSupervisor() != null) {
            dto.setSiteSupervisor(userMapper.toSimpleUserDto(entity.getSiteSupervisor()));
        }

        if (entity.getEquipmentManager() != null) {
            dto.setEquipmentManager(userMapper.toSimpleUserDto(entity.getEquipmentManager()));
        }

        return dto;
    }

//    private MainTaskResponseDto mapMainTaskToDto(MainTask entity) {
//        MainTaskResponseDto dto = new MainTaskResponseDto();
//        dto.setId(entity.getId());
//        dto.setTitle(entity.getTitle());
//        dto.setDescription(entity.getDescription());
//        dto.setProjectId(entity.getProject().getId());
//
//        if (entity.getSiteSupervisor() != null) {
//            dto.setSupervisorId(entity.getSiteSupervisor().getId());
//            dto.setSupervisorName(entity.getSiteSupervisor().getUsername());
//        }
//
//        dto.setPlannedStartDate(entity.getPlannedStartDate());
//        dto.setPlannedEndDate(entity.getPlannedEndDate());
//        dto.setActualStartDate(entity.getActualStartDate());
//        dto.setActualEndDate(entity.getActualEndDate());
//        dto.setStatus(entity.getStatus());
//        dto.setPriority(entity.getPriority());
//        dto.setEstimatedHours(entity.getEstimatedHours());
//        dto.setActualHours(entity.getActualHours());
//        dto.setCompletionPercentage(entity.getCompletionPercentage());
//        dto.setOverdue(entity.isOverdue());
//        return dto;
//    }

//    private ProjectResponseDto mapEntityToDto(Project entity) {
//        ProjectResponseDto dto = new ProjectResponseDto();
//        dto.setId(entity.getId());
//        dto.setTitle(entity.getTitle());
//        dto.setDescription(entity.getDescription());
//        dto.setProjectManagerId(entity.getProjectManager().getId());
//        dto.setProjectManagerName(entity.getProjectManager().getUsername());
//        dto.setStartDate(entity.getStartDate());
//        dto.setEndDate(entity.getEndDate());
//        dto.setActualStartDate(entity.getActualStartDate());
//        dto.setActualEndDate(entity.getActualEndDate());
//        dto.setStatus(entity.getStatus());
//        dto.setEstimatedBudget(entity.getEstimatedBudget());
//        dto.setActualBudget(entity.getActualBudget());
//        dto.setLocation(entity.getLocation());
//        dto.setPriority(entity.getPriority());
//
//        // Removed problematic calculations
//        // dto.setCompletionPercentage(entity.getCompletionPercentage());
//        // dto.setOverdue(entity.isOverdue());
//
//        if (entity.getSiteSupervisor() != null) {
//            dto.setSiteSupervisor(userMapper.toSimpleUserDto(entity.getSiteSupervisor()));
//        }
//
//        if (entity.getEquipmentManager() != null) {
//            dto.setEquipmentManager(userMapper.toSimpleUserDto(entity.getEquipmentManager()));
//        }
//
//        return dto;
//    }

    private MainTaskResponseDto mapMainTaskToDto(MainTask entity) {
        MainTaskResponseDto dto = new MainTaskResponseDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setProjectId(entity.getProject().getId());

        if (entity.getSiteSupervisor() != null) {
            dto.setSupervisorId(entity.getSiteSupervisor().getId());
            dto.setSupervisorName(entity.getSiteSupervisor().getUsername());
        }

        dto.setPlannedStartDate(entity.getPlannedStartDate());
        dto.setPlannedEndDate(entity.getPlannedEndDate());
        dto.setActualStartDate(entity.getActualStartDate());
        dto.setActualEndDate(entity.getActualEndDate());
        dto.setStatus(entity.getStatus());
        dto.setPriority(entity.getPriority());
        dto.setEstimatedHours(entity.getEstimatedHours());
        dto.setActualHours(entity.getActualHours());

        // Removed problematic calculations
        // dto.setCompletionPercentage(entity.getCompletionPercentage());
        // dto.setOverdue(entity.isOverdue());

        return dto;
    }


}
