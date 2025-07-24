package com.buildscheduler.buildscheduler.service.impl;

import com.buildscheduler.buildscheduler.dto.equipment_manager.EquipmentManagerProjectResponseDto;
import com.buildscheduler.buildscheduler.dto.worker.SimpleUserDto;
import com.buildscheduler.buildscheduler.exception.ResourceNotFoundException;
import com.buildscheduler.buildscheduler.model.*;
import com.buildscheduler.buildscheduler.repository.MainTaskRepository;
import com.buildscheduler.buildscheduler.repository.ProjectRepository;
import com.buildscheduler.buildscheduler.repository.SubtaskRepository;
import com.buildscheduler.buildscheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipmentManagerProjectService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final MainTaskRepository mainTaskRepository;
    private final SubtaskRepository subtaskRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new ResourceNotFoundException("User not authenticated");
        }
        // Fetch full user details from DB to ensure roles and other lazy-loaded data are available
        return userRepository.findById(((User) authentication.getPrincipal()).getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", ((User) authentication.getPrincipal()).getId()));
    }

    // ----------------------------------------------
    // Fetch All Projects Assigned to Equipment Manager
    // ----------------------------------------------
    @Transactional(readOnly = true)
    public List<EquipmentManagerProjectResponseDto> getProjectsForEquipmentManager() {
        User currentUser = getCurrentUser();

        // Ensure the current user is an EQUIPMENT_MANAGER
        boolean isEquipmentManager = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_EQUIPMENT_MANAGER"));
        if (!isEquipmentManager) {
            // Or throw a more specific AccessDeniedException if this method should only be called by EM roles
            // throw new AccessDeniedException("User is not an Equipment Manager.");
            return Collections.emptyList(); // Or throw a more descriptive error if needed
        }

        List<Project> projects = projectRepository.findByEquipmentManager(currentUser);

        if (projects.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Collect all project IDs to fetch related main tasks and subtasks efficiently
        List<Long> projectIds = projects.stream().map(Project::getId).toList();

        // 2. Fetch all relevant main tasks for these projects in one go
        List<MainTask> allMainTasks = mainTaskRepository.findByProjectIdIn(projectIds);
        Map<Long, List<MainTask>> mainTasksByProjectId = allMainTasks.stream()
                .collect(Collectors.groupingBy(mt -> mt.getProject().getId()));

        // 3. Fetch all relevant subtasks for these main tasks in one go
        List<Long> allMainTaskIds = allMainTasks.stream().map(MainTask::getId).toList();
        List<Subtask> allSubtasks = Collections.emptyList();
        if (!allMainTaskIds.isEmpty()) {
            allSubtasks = subtaskRepository.findByMainTaskIdIn(allMainTaskIds);
        }
        Map<Long, List<Subtask>> subtasksByMainTaskId = allSubtasks.stream()
                .collect(Collectors.groupingBy(st -> st.getMainTask().getId()));

        return projects.stream().map(project -> {
            EquipmentManagerProjectResponseDto dto = new EquipmentManagerProjectResponseDto();
            dto.setId(project.getId());
            dto.setTitle(project.getTitle());
            dto.setDescription(project.getDescription());
            dto.setStatus(project.getStatus());
            dto.setStartDate(project.getStartDate());
            dto.setEndDate(project.getEndDate());
            dto.setEstimatedBudget(project.getEstimatedBudget());
            dto.setLocation(project.getLocation());
            dto.setPriority(project.getPriority());

            // Convert Users
            dto.setSiteSupervisor(convertToSimpleUserDto(project.getSiteSupervisor()));
            dto.setEquipmentManager(convertToSimpleUserDto(project.getEquipmentManager())); // This will be the current EM

            if (project.getProjectManager() != null) {
                dto.setProjectManagerId(project.getProjectManager().getId());
                dto.setProjectManagerName(project.getProjectManager().getUsername());
            }

            // Calculate and set completion percentage for each project
            List<MainTask> currentProjectMainTasks = mainTasksByProjectId.getOrDefault(project.getId(), Collections.emptyList());
            double projectCompletion = calculateProjectCompletion(currentProjectMainTasks, subtasksByMainTaskId);
            dto.setCompletionPercentage(roundToTwoDecimalPlaces(projectCompletion));
            dto.setOverdue(isProjectOverdue(project));

            return dto;
        }).collect(Collectors.toList());
    }

    // ------------------------
    // Utility Conversion and Calculation Methods (Copied from SiteSupervisorProjectService)
    // ------------------------
    private SimpleUserDto convertToSimpleUserDto(User user) {
        if (user == null) return null;
        SimpleUserDto dto = new SimpleUserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        return dto;
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
        if (mainTasks == null || mainTasks.isEmpty()) {
            return 0.0;
        }
        return mainTasks.stream()
                .mapToDouble(mainTask -> calculateMainTaskCompletion(subtasksByMainTask.getOrDefault(mainTask.getId(), Collections.emptyList()), mainTask))
                .average()
                .orElse(0.0);
    }

    private boolean isProjectOverdue(Project project) {
        if (project.getStatus() == Project.ProjectStatus.COMPLETED) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return project.getEndDate() != null && project.getEndDate().isBefore(today);
    }

    private double roundToTwoDecimalPlaces(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}