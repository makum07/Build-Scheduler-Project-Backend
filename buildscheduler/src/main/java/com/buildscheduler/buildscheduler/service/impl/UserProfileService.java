package com.buildscheduler.buildscheduler.service.impl;

import com.buildscheduler.buildscheduler.dto.notification.NotificationDto;
import com.buildscheduler.buildscheduler.dto.user.FullUserProfileDto;
import com.buildscheduler.buildscheduler.dto.user.FullUserProfileDto.UserInfoDto;
import com.buildscheduler.buildscheduler.exception.ResourceNotFoundException;
import com.buildscheduler.buildscheduler.mapper.*;
import com.buildscheduler.buildscheduler.model.*;
import com.buildscheduler.buildscheduler.repository.NotificationRepository;
import com.buildscheduler.buildscheduler.repository.ProjectRepository; // Import ProjectRepository
import com.buildscheduler.buildscheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final ProjectRepository projectRepository; // Inject ProjectRepository
    private final UserMapper userMapper;
    private final NotificationMapper notificationMapper;
    private final ProjectMapper projectMapper;
    private final MainTaskMapper mainTaskMapper;
    private final EquipmentMapper equipmentMapper;
    private final AssignmentMapper assignmentMapper;
    private final AvailabilitySlotMapper availabilitySlotMapper;

    @Transactional(readOnly = true)
    public FullUserProfileDto getFullUserProfileById(Long userId) {
        User user = userRepository.findFullProfileById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return mapUserToRoleSpecificFullProfileDto(user);
    }

    @Transactional(readOnly = true)
    public FullUserProfileDto getMyFullProfile() {
        User currentUser = getCurrentUser();
        return getFullUserProfileById(currentUser.getId());
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getMyNotifications() {
        User currentUser = getCurrentUser();
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
        return notificationMapper.toDtoList(notifications);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new ResourceNotFoundException("User not authenticated");
        }
        // Fetch full profile, including eager loads defined in userRepository.findFullProfileById
        return userRepository.findFullProfileById(((User) authentication.getPrincipal()).getId())
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated User", "id", ((User) authentication.getPrincipal()).getId()));
    }

    private FullUserProfileDto mapUserToRoleSpecificFullProfileDto(User user) {
        FullUserProfileDto dto = userMapper.toFullProfileDto(user);

        // Ensure all lists are initialized as empty if not populated later
        dto.setManagedTeam(Collections.emptyList());
        dto.setManagedProjects(Collections.emptyList());
        dto.setSupervisedWorkers(Collections.emptyList());
        dto.setSupervisedTasks(Collections.emptyList());
        dto.setManagedEquipment(Collections.emptyList());
        dto.setWorkerAssignments(Collections.emptyList());
        dto.setWorkerAvailabilitySlots(Collections.emptyList());
        dto.setWorksUnder(Collections.emptyList());

        // --- Logic for Project Managers ---
        if (user.hasRole("ROLE_PROJECT_MANAGER")) {
            // Fetch projects managed by this Project Manager using ProjectRepository
            Set<Project> managedProjects = projectRepository.findProjectsByProjectManagerId(user.getId());

            if (managedProjects != null && !managedProjects.isEmpty()) {
                dto.setManagedProjects(managedProjects.stream()
                        .map(projectMapper::toFullProjectDto)
                        .collect(Collectors.toList()));

                // Populate managedTeam from managedProjects
                Set<UserInfoDto> managedTeamSet = new HashSet<>();
                managedProjects.forEach(project -> {
                    // Add Site Supervisor of the project
                    if (project.getSiteSupervisor() != null) {
                        managedTeamSet.add(new UserInfoDto(project.getSiteSupervisor().getId(),
                                project.getSiteSupervisor().getUsername(),
                                project.getSiteSupervisor().getEmail(),
                                userMapper.toRoleNameSet(project.getSiteSupervisor().getRoles())));
                    }
                    // Add Equipment Manager of the project
                    if (project.getEquipmentManager() != null) {
                        managedTeamSet.add(new UserInfoDto(project.getEquipmentManager().getId(),
                                project.getEquipmentManager().getUsername(),
                                project.getEquipmentManager().getEmail(),
                                userMapper.toRoleNameSet(project.getEquipmentManager().getRoles())));
                    }
                    // Add Workers directly assigned to the project
                    if (project.getWorkers() != null) {
                        project.getWorkers().forEach(worker ->
                                managedTeamSet.add(new UserInfoDto(worker.getId(),
                                        worker.getUsername(),
                                        worker.getEmail(),
                                        userMapper.toRoleNameSet(worker.getRoles())))
                        );
                    }
                    // Add Workers indirectly via MainTasks -> Subtasks -> WorkerAssignments
                    if (project.getMainTasks() != null) {
                        project.getMainTasks().forEach(mainTask -> {
                            if (mainTask.getSubtasks() != null) {
                                mainTask.getSubtasks().forEach(subtask -> {
                                    if (subtask.getWorkerAssignments() != null) {
                                        subtask.getWorkerAssignments().forEach(assignment -> {
                                            if (assignment.getWorker() != null) {
                                                User worker = assignment.getWorker();
                                                managedTeamSet.add(new UserInfoDto(worker.getId(),
                                                        worker.getUsername(),
                                                        worker.getEmail(),
                                                        userMapper.toRoleNameSet(worker.getRoles())));
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
                dto.setManagedTeam(managedTeamSet.stream().sorted((u1, u2) -> u1.getUsername().compareTo(u2.getUsername())).collect(Collectors.toList()));
            }
        }

        // --- Logic for Site Supervisors ---
        if (user.hasRole("ROLE_SITE_SUPERVISOR")) {
            // Fetch projects where this user is the Site Supervisor using ProjectRepository
            Set<Project> projectsSupervised = projectRepository.findProjectsBySiteSupervisorId(user.getId());

            Set<UserInfoDto> worksUnderSet = new HashSet<>();
            Set<UserInfoDto> supervisedWorkersSet = new HashSet<>();
            Set<MainTask> supervisedTasksSet = new HashSet<>(); // Collect supervised tasks here

            if (projectsSupervised != null && !projectsSupervised.isEmpty()) {
                projectsSupervised.forEach(project -> {
                    // Add Project Manager of the current project to worksUnder
                    if (project.getProjectManager() != null) {
                        worksUnderSet.add(new UserInfoDto(project.getProjectManager().getId(),
                                project.getProjectManager().getUsername(),
                                project.getProjectManager().getEmail(),
                                userMapper.toRoleNameSet(project.getProjectManager().getRoles())));
                    }
                    // Add Workers directly assigned to this project to supervisedWorkers
                    if (project.getWorkers() != null) {
                        project.getWorkers().forEach(worker ->
                                supervisedWorkersSet.add(new UserInfoDto(worker.getId(),
                                        worker.getUsername(),
                                        worker.getEmail(),
                                        userMapper.toRoleNameSet(worker.getRoles())))
                        );
                    }
                    // Add Workers from tasks within this project to supervisedWorkers
                    if (project.getMainTasks() != null) {
                        project.getMainTasks().forEach(mainTask -> {
                            // If the main task itself has this site supervisor, add it to supervisedTasks
                            if (mainTask.getSiteSupervisor() != null && mainTask.getSiteSupervisor().equals(user)) {
                                supervisedTasksSet.add(mainTask);
                            }
                            if (mainTask.getSubtasks() != null) {
                                mainTask.getSubtasks().forEach(subtask -> {
                                    if (subtask.getWorkerAssignments() != null) {
                                        subtask.getWorkerAssignments().forEach(assignment -> {
                                            if (assignment.getWorker() != null) {
                                                User worker = assignment.getWorker();
                                                supervisedWorkersSet.add(new UserInfoDto(worker.getId(),
                                                        worker.getUsername(),
                                                        worker.getEmail(),
                                                        userMapper.toRoleNameSet(worker.getRoles())));
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }

            // Also check the user's direct projectManager link (if exists and not already captured by project)
            if (user.getProjectManager() != null) {
                worksUnderSet.add(new UserInfoDto(user.getProjectManager().getId(),
                        user.getProjectManager().getUsername(),
                        user.getProjectManager().getEmail(),
                        userMapper.toRoleNameSet(user.getProjectManager().getRoles())));
            }

            dto.setWorksUnder(worksUnderSet.stream().sorted((u1, u2) -> u1.getUsername().compareTo(u2.getUsername())).collect(Collectors.toList()));
            dto.setSupervisedWorkers(supervisedWorkersSet.stream().sorted((u1, u2) -> u1.getUsername().compareTo(u2.getUsername())).collect(Collectors.toList()));

            // Convert collected supervised tasks to DTOs
            dto.setSupervisedTasks(supervisedTasksSet.stream()
                    .map(mainTaskMapper::toResponseDto)
                    .sorted((t1, t2) -> t1.getTitle().compareTo(t2.getTitle())) // Optional: sort tasks by title
                    .collect(Collectors.toList()));
        }

        // --- Logic for Equipment Managers ---
        if (user.hasRole("ROLE_EQUIPMENT_MANAGER")) {
            if (user.getManagedEquipment() != null && !user.getManagedEquipment().isEmpty()) {
                dto.setManagedEquipment(equipmentMapper.toResponseDtoList(user.getManagedEquipment()));
            }
            // Populate worksUnder for Equipment Manager based on their direct Project Manager (if set)
            Set<UserInfoDto> worksUnderSet = new HashSet<>();
            if (user.getProjectManager() != null) {
                worksUnderSet.add(new UserInfoDto(user.getProjectManager().getId(),
                        user.getProjectManager().getUsername(),
                        user.getProjectManager().getEmail(),
                        userMapper.toRoleNameSet(user.getProjectManager().getRoles())));
            }
            dto.setWorksUnder(worksUnderSet.stream().distinct().collect(Collectors.toList()));
        }

        // --- Logic for Workers ---
        if (user.hasRole("ROLE_WORKER")) {
            if (user.getWorkerAssignments() != null && !user.getWorkerAssignments().isEmpty()) {
                dto.setWorkerAssignments(user.getWorkerAssignments().stream()
                        .map(assignmentMapper::toWorkerAssignmentDto)
                        .collect(Collectors.toList()));
            }
            if (user.getWorkerAvailabilitySlots() != null && !user.getWorkerAvailabilitySlots().isEmpty()) {
                dto.setWorkerAvailabilitySlots(user.getWorkerAvailabilitySlots().stream()
                        .map(availabilitySlotMapper::toDto)
                        .collect(Collectors.toList()));
            }

            // Populate worksUnder for Workers (Project Managers and Site Supervisors of their assigned tasks/projects)
            List<Object[]> managers = userRepository.findProjectManagersAndSiteSupervisorsForWorker(user.getId());
            Set<UserInfoDto> worksUnderSet = new HashSet<>();
            for (Object[] row : managers) {
                User pm = (User) row[0];
                User ss = (User) row[1];
                if (pm != null) {
                    worksUnderSet.add(new UserInfoDto(pm.getId(), pm.getUsername(), pm.getEmail(), userMapper.toRoleNameSet(pm.getRoles())));
                }
                if (ss != null) {
                    worksUnderSet.add(new UserInfoDto(ss.getId(), ss.getUsername(), ss.getEmail(), userMapper.toRoleNameSet(ss.getRoles())));
                }
            }
            dto.setWorksUnder(worksUnderSet.stream().distinct().collect(Collectors.toList()));
        }

        return dto;
    }
}