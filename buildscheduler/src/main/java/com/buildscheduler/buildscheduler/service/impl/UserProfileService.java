package com.buildscheduler.buildscheduler.service.impl;

import com.buildscheduler.buildscheduler.dto.notification.NotificationDto;
import com.buildscheduler.buildscheduler.dto.user.FullUserProfileDto;
import com.buildscheduler.buildscheduler.dto.user.FullUserProfileDto.UserInfoDto; // Correct import for inner class
// import com.buildscheduler.buildscheduler.dto.user.SimpleUserDto; // We won't use SimpleUserDto for managedTeam/supervisedWorkers directly in DTO
import com.buildscheduler.buildscheduler.exception.ResourceNotFoundException;
import com.buildscheduler.buildscheduler.mapper.*;
import com.buildscheduler.buildscheduler.model.*;
import com.buildscheduler.buildscheduler.repository.NotificationRepository;
import com.buildscheduler.buildscheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final UserMapper userMapper;
    private final NotificationMapper notificationMapper;
    private final ProjectMapper projectMapper;
    private final MainTaskMapper mainTaskMapper;
    private final EquipmentMapper equipmentMapper; // Now present
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
        return (User) authentication.getPrincipal();
    }

    /**
     * Maps the User entity to FullUserProfileDto, populating specific fields
     * based on the user's roles to avoid showing irrelevant empty lists.
     * This is where the business logic for role-based data visibility resides.
     */
    private FullUserProfileDto mapUserToRoleSpecificFullProfileDto(User user) {
        // Start with a basic mapping from the mapper. This handles common fields and direct managers.
        FullUserProfileDto dto = userMapper.toFullProfileDto(user);

        // Clear all list fields initially, as they will be conditionally populated
        dto.setManagedTeam(Collections.emptyList());
        dto.setManagedProjects(Collections.emptyList());
        dto.setSupervisedWorkers(Collections.emptyList());
        dto.setSupervisedTasks(Collections.emptyList());
        dto.setManagedEquipment(Collections.emptyList());
        dto.setWorkerAssignments(Collections.emptyList());
        dto.setWorkerAvailabilitySlots(Collections.emptyList());
        dto.setWorksUnder(Collections.emptyList());


        // Populate role-specific fields based on the fetched 'user' object
        if (user.hasRole("ROLE_PROJECT_MANAGER")) {
            if (user.getManagedProjects() != null && !user.getManagedProjects().isEmpty()) {
                dto.setManagedProjects(user.getManagedProjects().stream()
                        .map(projectMapper::toFullProjectDto)
                        .collect(Collectors.toList()));
            }
            if (user.getManagedTeam() != null && !user.getManagedTeam().isEmpty()) {
                dto.setManagedTeam(user.getManagedTeam().stream()
                        .map(teamMember -> new UserInfoDto(teamMember.getId(), teamMember.getUsername(), teamMember.getEmail(), userMapper.toRoleNameSet(teamMember.getRoles())))
                        .collect(Collectors.toList()));
            }
        }

        if (user.hasRole("ROLE_SITE_SUPERVISOR")) {
            if (user.getSupervisedTasks() != null && !user.getSupervisedTasks().isEmpty()) {
                dto.setSupervisedTasks(user.getSupervisedTasks().stream()
                        .map(mainTaskMapper::toResponseDto)
                        .collect(Collectors.toList()));
            }
            if (user.getSupervisedWorkers() != null && !user.getSupervisedWorkers().isEmpty()) {
                dto.setSupervisedWorkers(user.getSupervisedWorkers().stream()
                        .map(worker -> new UserInfoDto(worker.getId(), worker.getUsername(), worker.getEmail(), userMapper.toRoleNameSet(worker.getRoles())))
                        .collect(Collectors.toList()));
            }
        }

        if (user.hasRole("ROLE_EQUIPMENT_MANAGER")) {
            if (user.getManagedEquipment() != null && !user.getManagedEquipment().isEmpty()) {
                dto.setManagedEquipment(equipmentMapper.toResponseDtoList(user.getManagedEquipment()));
            }
        }

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
        }

        // --- Populate 'worksUnder' ---
        List<UserInfoDto> worksUnderList = new java.util.ArrayList<>();

        // Case 1: If current user is a WORKER
        if (user.hasRole("ROLE_WORKER")) {
            List<Object[]> managers = userRepository.findProjectManagersAndSiteSupervisorsForWorker(user.getId());
            for (Object[] row : managers) {
                User pm = (User) row[0];
                User ss = (User) row[1];
                if (pm != null) {
                    worksUnderList.add(new UserInfoDto(pm.getId(), pm.getUsername(), pm.getEmail(), userMapper.toRoleNameSet(pm.getRoles())));
                }
                if (ss != null) {
                    worksUnderList.add(new UserInfoDto(ss.getId(), ss.getUsername(), ss.getEmail(), userMapper.toRoleNameSet(ss.getRoles())));
                }
            }
        }
        // Case 2: If current user is a SITE_SUPERVISOR or EQUIPMENT_MANAGER
        else if (user.hasRole("ROLE_SITE_SUPERVISOR") || user.hasRole("ROLE_EQUIPMENT_MANAGER")) {
            if (user.getProjectManager() != null) {
                worksUnderList.add(new UserInfoDto(user.getProjectManager().getId(),
                        user.getProjectManager().getUsername(),
                        user.getProjectManager().getEmail(),
                        userMapper.toRoleNameSet(user.getProjectManager().getRoles())));
            }
        }

        dto.setWorksUnder(worksUnderList.stream().distinct().collect(Collectors.toList()));

        return dto;
    }
}