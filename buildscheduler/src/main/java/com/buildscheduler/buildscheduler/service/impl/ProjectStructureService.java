package com.buildscheduler.buildscheduler.service.impl;

import com.buildscheduler.buildscheduler.dto.project_manager.FullProjectResponseDto;
import com.buildscheduler.buildscheduler.dto.project_manager.MainTaskResponseDto;
import com.buildscheduler.buildscheduler.dto.project_manager.ProjectStructureResponse;
import com.buildscheduler.buildscheduler.dto.worker.SimpleUserDto;
import com.buildscheduler.buildscheduler.exception.ResourceNotFoundException;
import com.buildscheduler.buildscheduler.model.*;
import com.buildscheduler.buildscheduler.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectStructureService {

    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public ProjectStructureResponse getProjectStructure(Long id) {
        log.debug("Fetching project structure for project ID: {}", id);
        return getProjectStructureWithMultipleQueries(id);
    }

    @Transactional(readOnly = true)
    public ProjectStructureResponse getProjectStructureWithMultipleQueries(Long id) {
        log.debug("Using multiple queries approach for project ID: {}", id);

        Project project = projectRepository.findProjectWithManagers(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        List<MainTask> mainTasks = projectRepository.findMainTasksByProjectId(id);

        if (mainTasks.isEmpty()) {
            return buildProjectStructureResponse(project, mainTasks, Collections.emptyMap());
        }

        List<Long> mainTaskIds = mainTasks.stream().map(MainTask::getId).toList();
        List<Subtask> subtasks = projectRepository.findSubtasksByMainTaskIds(mainTaskIds);

        Map<Long, List<Subtask>> subtasksByMainTask = subtasks.stream()
                .collect(Collectors.groupingBy(st -> st.getMainTask().getId()));

        return buildProjectStructureResponse(project, mainTasks, subtasksByMainTask);
    }

    private ProjectStructureResponse buildProjectStructureResponse(Project project, List<MainTask> mainTasks, Map<Long, List<Subtask>> subtasksByMainTask) {
        FullProjectResponseDto projectDto = mapToFullProjectDto(project);
        double projectCompletion = calculateProjectCompletion(mainTasks, subtasksByMainTask);
        projectDto.setCompletionPercentage(roundToTwoDecimalPlaces(projectCompletion));
        projectDto.setOverdue(isProjectOverdue(project));

        List<MainTaskResponseDto> mainTaskDtos = mainTasks.stream()
                .map(mainTask -> mapMainTaskToDtoWithCalculations(mainTask, subtasksByMainTask.getOrDefault(mainTask.getId(), Collections.emptyList())))
                .sorted(Comparator.comparing(MainTaskResponseDto::getId))
                .toList();

        log.debug("Project structure built successfully for project ID: {} with {} main tasks", project.getId(), mainTaskDtos.size());
        return new ProjectStructureResponse(projectDto, mainTaskDtos);
    }

    private MainTaskResponseDto mapMainTaskToDtoWithCalculations(MainTask mainTask, List<Subtask> subtasks) {
        MainTaskResponseDto dto = mapMainTaskToDto(mainTask);
        dto.setCompletionPercentage(roundToTwoDecimalPlaces(calculateMainTaskCompletion(subtasks, mainTask)));
        dto.setOverdue(isMainTaskOverdue(mainTask));
        return dto;
    }

    private MainTaskResponseDto mapMainTaskToDto(MainTask mainTask) {
        MainTaskResponseDto dto = new MainTaskResponseDto();
        dto.setId(mainTask.getId());
        dto.setTitle(mainTask.getTitle());
        dto.setDescription(mainTask.getDescription());
        dto.setProjectId(mainTask.getProject() != null ? mainTask.getProject().getId() : null);

        if (mainTask.getSiteSupervisor() != null) {
            dto.setSupervisorId(mainTask.getSiteSupervisor().getId());
            dto.setSupervisorName(mainTask.getSiteSupervisor().getUsername());
        }

        if (mainTask.getEquipmentManager() != null) {
            dto.setEquipmentManagerId(mainTask.getEquipmentManager().getId());
            dto.setEquipmentManagerName(mainTask.getEquipmentManager().getUsername());
        }

        dto.setPlannedStartDate(mainTask.getPlannedStartDate());
        dto.setPlannedEndDate(mainTask.getPlannedEndDate());
        dto.setStatus(mainTask.getStatus());
        dto.setPriority(mainTask.getPriority());
        dto.setEstimatedHours(mainTask.getEstimatedHours());

        return dto;
    }

    private FullProjectResponseDto mapToFullProjectDto(Project project) {
        FullProjectResponseDto dto = new FullProjectResponseDto();
        dto.setId(project.getId());
        dto.setTitle(project.getTitle());
        dto.setDescription(project.getDescription());
        dto.setStartDate(project.getStartDate());
        dto.setEndDate(project.getEndDate());
        dto.setStatus(project.getStatus());
        dto.setEstimatedBudget(project.getEstimatedBudget());
        dto.setLocation(project.getLocation());
        dto.setPriority(project.getPriority());

        if (project.getProjectManager() != null)
            dto.setProjectManager(mapToSimpleUserDto(project.getProjectManager()));
        if (project.getSiteSupervisor() != null)
            dto.setSiteSupervisor(mapToSimpleUserDto(project.getSiteSupervisor()));
        if (project.getEquipmentManager() != null)
            dto.setEquipmentManager(mapToSimpleUserDto(project.getEquipmentManager()));

        return dto;
    }

    private SimpleUserDto mapToSimpleUserDto(User user) {
        return new SimpleUserDto(user.getId(), user.getUsername(), user.getEmail());
    }

    private double calculateSubtaskCompletion(Subtask subtask) {
        if (subtask == null || subtask.getStatus() == null) return 0.0;

        return switch (subtask.getStatus()) {
            case COMPLETED -> 100.0;
            case IN_PROGRESS -> 50.0;
            case ASSIGNED -> 25.0;
            case ON_HOLD -> 10.0;
            case DELAYED -> 5.0;
            default -> 0.0;
        };
    }

    private double calculateMainTaskCompletion(List<Subtask> subtasks, MainTask mainTask) {
        if (subtasks == null || subtasks.isEmpty()) {
            return switch (mainTask.getStatus()) {
                case COMPLETED -> 100.0;
                case IN_PROGRESS -> 50.0;
                case ON_HOLD -> 10.0;
                case DELAYED -> 5.0;
                default -> 0.0;
            };
        }

        return subtasks.stream()
                .mapToDouble(this::calculateSubtaskCompletion)
                .average()
                .orElse(0.0);
    }

    private double calculateProjectCompletion(List<MainTask> mainTasks, Map<Long, List<Subtask>> subtasksByMainTask) {
        if (mainTasks == null || mainTasks.isEmpty()) return 0.0;

        return mainTasks.stream()
                .mapToDouble(mainTask -> calculateMainTaskCompletion(subtasksByMainTask.getOrDefault(mainTask.getId(), Collections.emptyList()), mainTask))
                .average()
                .orElse(0.0);
    }

    private boolean isMainTaskOverdue(MainTask mainTask) {
        if (mainTask == null || mainTask.getStatus() == MainTask.TaskStatus.COMPLETED) return false;

        LocalDate today = LocalDate.now();
        return mainTask.getPlannedEndDate() != null && mainTask.getPlannedEndDate().isBefore(today);
    }

    private boolean isProjectOverdue(Project project) {
        if (project == null || project.getStatus() == Project.ProjectStatus.COMPLETED) return false;

        LocalDate today = LocalDate.now();
        return project.getEndDate() != null && project.getEndDate().isBefore(today);
    }

    private double roundToTwoDecimalPlaces(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
