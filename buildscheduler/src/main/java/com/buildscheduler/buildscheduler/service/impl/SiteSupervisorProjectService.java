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

import java.util.List;
import java.util.Set;
import java.util.HashSet;
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

        return projects.stream().map(project -> {
            ProjectResponseDto dto = new ProjectResponseDto();
            dto.setId(project.getId());
            dto.setTitle(project.getTitle());
            dto.setDescription(project.getDescription());
            dto.setStatus(project.getStatus());  // Use enum directly
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

            // You can enhance this later
            dto.setCompletionPercentage(0.0);
            dto.setOverdue(false);

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

        User currentUser = getCurrentUser();
        if (!mainTask.getProject().getSiteSupervisor().equals(currentUser)) {
            throw new AccessDeniedException("You are not authorized for this main task");
        }

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
}
