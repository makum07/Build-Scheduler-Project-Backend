package com.buildscheduler.buildscheduler.service.impl;

import com.buildscheduler.buildscheduler.dto.project_manager.MainTaskRequestDto;
import com.buildscheduler.buildscheduler.dto.project_manager.MainTaskResponseDto;
import com.buildscheduler.buildscheduler.exception.ResourceNotFoundException;
import com.buildscheduler.buildscheduler.model.*;
import com.buildscheduler.buildscheduler.repository.MainTaskRepository;
import com.buildscheduler.buildscheduler.repository.ProjectRepository;
import com.buildscheduler.buildscheduler.repository.UserRepository;
import com.buildscheduler.buildscheduler.service.custom.MainTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MainTaskServiceImpl implements MainTaskService {

    private final MainTaskRepository mainTaskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

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

        return mapEntityToDto(mainTask);
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
        return mapEntityToDto(mainTask);
    }

    @Override
    @Transactional
    public void deleteMainTask(Long id) {
        MainTask mainTask = mainTaskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MainTask", "id", id));
        mainTaskRepository.delete(mainTask);
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
    public Page<MainTaskResponseDto> getMainTasksByProject(Long projectId, Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        return mainTaskRepository.findByProject(project, pageable)
                .map(this::mapEntityToDto);
    }

    private MainTaskResponseDto mapEntityToDto(MainTask entity) {
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
        dto.setCompletionPercentage(entity.getCompletionPercentage());
        dto.setOverdue(entity.isOverdue());
        return dto;
    }
}