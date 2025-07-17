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

import java.math.BigDecimal;
import java.time.LocalDate;
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
    @Transactional(readOnly = true)
    public ProjectResponseDto getProjectById(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        List<MainTask> mainTasks = mainTaskRepository.findByProject(project);
        return mapEntityToDto(project, mainTasks);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectResponseDto> getProjectsByManager(User manager, Pageable pageable) {
        return projectRepository.findByProjectManager(manager, pageable)
                .map(project -> {
                    List<MainTask> mainTasks = mainTaskRepository.findByProject(project);
                    return mapEntityToDto(project, mainTasks);
                });
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
    @Transactional(readOnly = true)
    public ProjectStructureResponse getProjectStructure(Long id) {
        Project project = projectRepository.findByIdWithTasksAndSubtasks(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        FullProjectResponseDto projectDto = mapToFullProjectDto(project);

        // Calculate project completion and overdue status
        projectDto.setCompletionPercentage(calculateProjectCompletion(project));
        projectDto.setOverdue(isProjectOverdue(project));

        // Map main tasks with their completion percentages
        List<MainTaskResponseDto> mainTaskDtos = project.getMainTasks().stream()
                .map(this::mapMainTaskToDto)
                .collect(Collectors.toList());

        return new ProjectStructureResponse(projectDto, mainTaskDtos);
    }

    private double calculateProjectCompletion(Project project) {
        if (project.getMainTasks().isEmpty()) return 0.0;

        double totalCompletion = project.getMainTasks().stream()
                .mapToDouble(this::calculateMainTaskCompletion)
                .average()
                .orElse(0.0);

        return Math.round(totalCompletion * 100.0) / 100.0;
    }

    private double calculateMainTaskCompletion(MainTask mainTask) {
        if (mainTask.getSubtasks().isEmpty()) return 0.0;

        double totalCompletion = mainTask.getSubtasks().stream()
                .mapToDouble(this::calculateSubtaskCompletion)
                .average()
                .orElse(0.0);

        return Math.round(totalCompletion * 100.0) / 100.0;
    }

    private double calculateSubtaskCompletion(Subtask subtask) {
        switch (subtask.getStatus()) {
            case COMPLETED: return 100.0;
            case IN_PROGRESS: return 50.0;
            case ASSIGNED: return 25.0;
            case ON_HOLD: return 10.0;
            case DELAYED: return 5.0;
            default: return 0.0;
        }
    }

    private boolean isProjectOverdue(Project project) {
        return project.getEndDate() != null &&
                project.getEndDate().isBefore(LocalDate.now()) &&
                project.getStatus() != Project.ProjectStatus.COMPLETED &&
                project.getStatus() != Project.ProjectStatus.CANCELLED;
    }

    private boolean isMainTaskOverdue(MainTask mainTask) {
        return mainTask.getPlannedEndDate() != null &&
                mainTask.getPlannedEndDate().isBefore(LocalDate.now()) &&
                mainTask.getStatus() != MainTask.TaskStatus.COMPLETED &&
                mainTask.getStatus() != MainTask.TaskStatus.CANCELLED;
    }

    private ProjectResponseDto mapEntityToDto(Project entity, List<MainTask> mainTasks) {
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

        // Include completionPercentage and overdue
        double completion = 0.0;
        if (!mainTasks.isEmpty()) {
            completion = mainTasks.stream()
                    .mapToDouble(this::calculateMainTaskCompletion)
                    .average()
                    .orElse(0.0);
        }
        dto.setCompletionPercentage(Math.round(completion * 100.0) / 100.0);
        dto.setOverdue(isProjectOverdue(entity));

        return dto;
    }

    private ProjectResponseDto mapEntityToDto(Project entity) {
        // Fallback mapper (no main task data)
        return mapEntityToDto(entity, List.of());
    }

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
        dto.setCompletionPercentage(calculateMainTaskCompletion(entity));
        dto.setOverdue(isMainTaskOverdue(entity));

        return dto;
    }

    private FullProjectResponseDto mapToFullProjectDto(Project entity) {
        FullProjectResponseDto dto = new FullProjectResponseDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setActualStartDate(entity.getActualStartDate());
        dto.setActualEndDate(entity.getActualEndDate());
        dto.setStatus(entity.getStatus());
        dto.setEstimatedBudget(entity.getEstimatedBudget());
        dto.setActualBudget(entity.getActualBudget());
        dto.setLocation(entity.getLocation());
        dto.setPriority(entity.getPriority());
        dto.setCompletionPercentage(0.0);
        dto.setOverdue(false);

        if (entity.getProjectManager() != null) {
            dto.setProjectManager(userMapper.toSimpleUserDto(entity.getProjectManager()));
        }
        if (entity.getSiteSupervisor() != null) {
            dto.setSiteSupervisor(userMapper.toSimpleUserDto(entity.getSiteSupervisor()));
        }
        if (entity.getEquipmentManager() != null) {
            dto.setEquipmentManager(userMapper.toSimpleUserDto(entity.getEquipmentManager()));
        }

        return dto;
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
}
