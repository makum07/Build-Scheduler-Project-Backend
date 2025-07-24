package com.buildscheduler.buildscheduler.service.impl;

import com.buildscheduler.buildscheduler.dto.project_manager.ProjectResponseDto;
import com.buildscheduler.buildscheduler.dto.site_supervisor.SimpleEquipmentDto;
import com.buildscheduler.buildscheduler.dto.site_supervisor.SubtaskDetailDto;
import com.buildscheduler.buildscheduler.dto.site_supervisor.EquipmentAssignmentDto;
import com.buildscheduler.buildscheduler.dto.site_supervisor.WorkerAssignmentDto;

import com.buildscheduler.buildscheduler.dto.worker.SimpleUserDto;
import com.buildscheduler.buildscheduler.exception.ResourceNotFoundException;
import com.buildscheduler.buildscheduler.model.*;
import com.buildscheduler.buildscheduler.repository.*;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate; // Import for LocalDate (used in isProjectOverdue)
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map; // Import for Map
import java.util.stream.Collectors;

@Service
public class SiteSupervisorProjectService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final MainTaskRepository mainTaskRepository;
    private final SubtaskRepository subtaskRepository;

    public SiteSupervisorProjectService(
            UserRepository userRepository,
            ProjectRepository projectRepository,
            MainTaskRepository mainTaskRepository,
            SubtaskRepository subtaskRepository
    ) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.mainTaskRepository = mainTaskRepository;
        this.subtaskRepository = subtaskRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new ResourceNotFoundException("User not authenticated");
        }
        return (User) authentication.getPrincipal();
    }

    // ----------------------------------------------
    // Fetch All Projects Assigned to Site Supervisor
    // ----------------------------------------------
    @Transactional(readOnly = true)
    public List<ProjectResponseDto> getProjectsForSupervisor() {
        User currentUser = getCurrentUser();
        List<Project> projects = projectRepository.findBySiteSupervisor(currentUser);

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
            // NOTE: If you plan to use 'actual hours spent' for subtask completion,
            // this repository call (or a subsequent one) needs to ensure workerAssignments
            // are fetched (e.g., using @EntityGraph in SubtaskRepository) to avoid LazyInitializationException.
            // For now, calculateSubtaskCompletion uses status-based percentages.
            allSubtasks = subtaskRepository.findByMainTaskIdIn(allMainTaskIds);
        }
        Map<Long, List<Subtask>> subtasksByMainTaskId = allSubtasks.stream()
                .collect(Collectors.groupingBy(st -> st.getMainTask().getId()));

        return projects.stream().map(project -> {
            ProjectResponseDto dto = new ProjectResponseDto();
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
            dto.setEquipmentManager(convertToSimpleUserDto(project.getEquipmentManager()));

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

    // --------------------------------------------------------
    // Fetch All Subtasks under a MainTask for This Supervisor
    // --------------------------------------------------------
    @Transactional(readOnly = true)
    public List<SubtaskDetailDto> getSubtasksByMainTaskId(Long mainTaskId) {
        MainTask mainTask = mainTaskRepository.findById(mainTaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Main task not found with ID: " + mainTaskId));

        // Ensure workerAssignments, requiredSkills, equipmentNeeds, etc., are fetched if convertToDetailDto
        // accesses lazy collections outside a transactional context.
        // For simplicity, assuming default fetching or calling from transactional context.
        // A better approach might be: subtaskRepository.findSubtasksWithDetailsByMainTaskId(mainTaskId);
        List<Subtask> subtasks = subtaskRepository.findByMainTaskId(mainTaskId);

        return subtasks.stream()
                .map(this::convertToDetailDto)
                .collect(Collectors.toList());
    }

    // ------------------------
    // Utility Conversion Methods
    // ------------------------
    private SubtaskDetailDto convertToDetailDto(Subtask subtask) {
        SubtaskDetailDto dto = new SubtaskDetailDto();
        dto.setId(subtask.getId());
        dto.setTitle(subtask.getTitle());
        dto.setDescription(subtask.getDescription());
        dto.setPlannedStart(subtask.getPlannedStart());
        dto.setPlannedEnd(subtask.getPlannedEnd());
        dto.setStatus(subtask.getStatus().name());
        dto.setEstimatedHours(subtask.getEstimatedHours());
        dto.setRequiredWorkers(subtask.getRequiredWorkers());
        dto.setPriority(subtask.getPriority());
        dto.setEquipmentRequestNotes(subtask.getEquipmentRequestNotes());

        dto.setRequiredSkills(subtask.getRequiredSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.toSet()));

        dto.setEquipmentNeeds(subtask.getEquipmentNeeds().stream()
                .map(this::convertToSimpleEquipmentDto)
                .collect(Collectors.toSet()));

        dto.setWorkerAssignments(subtask.getWorkerAssignments().stream()
                .map(this::convertToWorkerAssignmentDto)
                .collect(Collectors.toSet()));

        dto.setEquipmentAssignments(subtask.getEquipmentAssignments().stream()
                .map(this::convertToEquipmentAssignmentDto)
                .collect(Collectors.toSet()));

        return dto;
    }

    private SimpleUserDto convertToSimpleUserDto(User user) {
        if (user == null) return null;
        SimpleUserDto dto = new SimpleUserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        return dto;
    }

    private SimpleEquipmentDto convertToSimpleEquipmentDto(Equipment equipment) {
        SimpleEquipmentDto dto = new SimpleEquipmentDto();
        dto.setId(equipment.getId());
        dto.setName(equipment.getName());
        dto.setModel(equipment.getModel());
        dto.setSerialNumber(equipment.getSerialNumber());
        dto.setType(equipment.getType().name());
        dto.setStatus(equipment.getStatus().name());
        return dto;
    }

    private WorkerAssignmentDto convertToWorkerAssignmentDto(WorkerAssignment assignment) {
        WorkerAssignmentDto dto = new WorkerAssignmentDto();
        dto.setId(assignment.getId());
        dto.setAssignmentStart(assignment.getAssignmentStart());
        dto.setAssignmentEnd(assignment.getAssignmentEnd());
        dto.setNotes(assignment.getWorkerNotes());
        dto.setWorker(convertToSimpleUserDto(assignment.getWorker()));
        dto.setAssignedBy(convertToSimpleUserDto(assignment.getAssignedBy()));
        return dto;
    }

    private EquipmentAssignmentDto convertToEquipmentAssignmentDto(EquipmentAssignment assignment) {
        EquipmentAssignmentDto dto = new EquipmentAssignmentDto();
        dto.setId(assignment.getId());
        dto.setStartTime(assignment.getStartTime());
        dto.setEndTime(assignment.getEndTime());
        dto.setNotes(assignment.getEquipmentNotes());
        dto.setEquipment(convertToSimpleEquipmentDto(assignment.getEquipment()));
        dto.setAssignedBy(convertToSimpleUserDto(assignment.getAssignedBy()));
        return dto;
    }

    // ------------------------
    // Completion Calculation Methods (Duplicated for independent calculation)
    // These methods are taken from ProjectServiceImpl to allow SiteSupervisorProjectService
    // to calculate project completion directly without depending on ProjectServiceImpl.
    // ------------------------

    private double calculateSubtaskCompletion(Subtask subtask) {
        if (subtask == null || subtask.getStatus() == null) return 0.0;

        // This uses the simplified status-based completion.
        // If you need actual hours, ensure workerAssignments are eagerly fetched
        // or accessible within a transaction when this method is called.
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
            // Fallback to MainTask status if no subtasks
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