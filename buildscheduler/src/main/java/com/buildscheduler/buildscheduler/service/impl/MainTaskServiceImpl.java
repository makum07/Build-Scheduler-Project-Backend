package com.buildscheduler.buildscheduler.service.impl;

import com.buildscheduler.buildscheduler.dto.project_manager.MainTaskRequestDto;
import com.buildscheduler.buildscheduler.dto.project_manager.MainTaskResponseDto;
import com.buildscheduler.buildscheduler.exception.ResourceNotFoundException;
import com.buildscheduler.buildscheduler.model.*;
import com.buildscheduler.buildscheduler.repository.MainTaskRepository;
import com.buildscheduler.buildscheduler.repository.ProjectRepository;
import com.buildscheduler.buildscheduler.repository.UserRepository;
import com.buildscheduler.buildscheduler.repository.SubtaskRepository;
import com.buildscheduler.buildscheduler.service.custom.MainTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MainTaskServiceImpl implements MainTaskService {

    private final MainTaskRepository mainTaskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final SubtaskRepository subtaskRepository;

    @Override
    @Transactional
    public MainTaskResponseDto createMainTask(MainTaskRequestDto dto, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        User supervisor = null;
        if (dto.getSupervisorId() != null) {
            supervisor = userRepository.findById(dto.getSupervisorId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getSupervisorId()));
        }

        MainTask mainTask = new MainTask();
        mapDtoToEntity(dto, mainTask);
        mainTask.setProject(project);
        mainTask.setSiteSupervisor(supervisor);
        mainTask.setStatus(MainTask.TaskStatus.PLANNED);
        mainTask = mainTaskRepository.save(mainTask);

        return mapEntityToDto(mainTask, Collections.emptyList());
    }

    @Override
    @Transactional
    public MainTaskResponseDto updateMainTask(Long id, MainTaskRequestDto dto) {
        MainTask mainTask = mainTaskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MainTask", "id", id));

        if (dto.getSupervisorId() != null) {
            User supervisor = userRepository.findById(dto.getSupervisorId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", dto.getSupervisorId()));
            mainTask.setSiteSupervisor(supervisor);
        } else {
            mainTask.setSiteSupervisor(null);
        }

        mapDtoToEntity(dto, mainTask);
        mainTask = mainTaskRepository.save(mainTask);
        return mapEntityToDto(mainTask, Collections.emptyList());
    }

    @Override
    @Transactional
    public void deleteMainTask(Long id) {
        MainTask mainTask = mainTaskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MainTask", "id", id));
        // Delete subtasks first
        subtaskRepository.deleteByMainTaskId(id);

        // Then delete main task
        mainTaskRepository.deleteById(id);
    }

    private void mapDtoToEntity(MainTaskRequestDto dto, MainTask entity) {
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setPlannedStartDate(dto.getPlannedStartDate());
        entity.setPlannedEndDate(dto.getPlannedEndDate());
        entity.setPriority(dto.getPriority());
        entity.setEstimatedHours(dto.getEstimatedHours());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MainTaskResponseDto> getMainTasksByProject(Long projectId, Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        Page<MainTask> page = mainTaskRepository.findByProject(project, pageable);

        List<Long> mainTaskIds = page.getContent().stream().map(MainTask::getId).toList();
        Map<Long, List<Subtask>> subtasksMap = subtaskRepository.findByMainTaskIdIn(mainTaskIds).stream()
                .collect(Collectors.groupingBy(sub -> sub.getMainTask().getId()));

        return page.map(mainTask -> {
            List<Subtask> subtasks = subtasksMap.getOrDefault(mainTask.getId(), Collections.emptyList());
            return mapEntityToDto(mainTask, subtasks);
        });
    }

    private MainTaskResponseDto mapEntityToDto(MainTask entity, List<Subtask> subtasks) {
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

        dto.setCompletionPercentage(roundToTwoDecimalPlaces(calculateMainTaskCompletion(subtasks, entity)));
        dto.setOverdue(isMainTaskOverdue(entity));

        return dto;
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

    private boolean isMainTaskOverdue(MainTask mainTask) {
        if (mainTask == null || mainTask.getStatus() == MainTask.TaskStatus.COMPLETED) return false;
        LocalDate today = LocalDate.now();
        return mainTask.getPlannedEndDate() != null && mainTask.getPlannedEndDate().isBefore(today);
    }

    private double roundToTwoDecimalPlaces(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
