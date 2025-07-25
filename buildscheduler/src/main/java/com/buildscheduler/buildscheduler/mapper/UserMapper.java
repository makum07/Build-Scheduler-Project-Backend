package com.buildscheduler.buildscheduler.mapper;

import com.buildscheduler.buildscheduler.dto.auth.RoleDto;
import com.buildscheduler.buildscheduler.dto.auth.UserDto;
import com.buildscheduler.buildscheduler.dto.equipment_manager.EquipmentResponseDto;
import com.buildscheduler.buildscheduler.dto.project_manager.MainTaskResponseDto;
import com.buildscheduler.buildscheduler.dto.project_manager.FullProjectResponseDto; // Corrected import to FullProjectResponseDto
import com.buildscheduler.buildscheduler.dto.project_manager.UserTableDto;
import com.buildscheduler.buildscheduler.dto.site_supervisor.WorkerAssignmentDto;
import com.buildscheduler.buildscheduler.dto.user.FullUserProfileDto;

import com.buildscheduler.buildscheduler.dto.worker.SimpleUserDto;
import com.buildscheduler.buildscheduler.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper implements Mapper<User, UserDto> {

    private final ProjectMapper projectMapper;
    private final MainTaskMapper mainTaskMapper;
    private final AssignmentMapper assignmentMapper;
    private final AvailabilitySlotMapper availabilitySlotMapper;
    private final EquipmentMapper equipmentMapper; // Assume you have this now, based on structure

    @Override
    public UserDto toDto(User entity) {
        if (entity == null) {
            return null;
        }
        UserDto dto = new UserDto();
        dto.setUsername(entity.getUsername());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());

        String roles = entity.getRoles().stream()
                .map(role -> role.getName().replace("ROLE_", ""))
                .map(this::toTitleCase)
                .collect(Collectors.joining(", "));

        dto.setRole(roles);
        return dto;
    }

    @Override
    public User toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        return user;
    }

    private String toTitleCase(String input) {
        if (input == null || input.isEmpty()) return input;

        StringBuilder titleCase = new StringBuilder();
        boolean convertNext = true;

        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                convertNext = true;
            } else if (convertNext) {
                c = Character.toTitleCase(c);
                convertNext = false;
            } else {
                c = Character.toLowerCase(c);
            }
            titleCase.append(c);
        }
        return titleCase.toString();
    }

    public UserTableDto toUserTableDto(User user) {
        if (user == null) {
            return null;
        }
        String role = user.getRoles().stream()
                .map(Role::getName)
                .findFirst()
                .orElse("UNKNOWN");

        return new UserTableDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                role
        );
    }

    public List<UserTableDto> toUserTableDtos(List<User> users) {
        if (users == null) {
            return Collections.emptyList();
        }
        return users.stream()
                .map(this::toUserTableDto)
                .collect(Collectors.toList());
    }

    public SimpleUserDto toSimpleUserDto(User user) {
        if (user == null) {
            return null;
        }
        return SimpleUserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    // New method to convert a Set<Role> to Set<String> of role names
    public Set<String> toRoleNameSet(Set<Role> roles) {
        if (roles == null) {
            return Collections.emptySet();
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    // New method to convert a Set<Role> to Set<RoleDto>
    public Set<RoleDto> toRoleDtoSet(Set<Role> roles) {
        if (roles == null) {
            return Collections.emptySet();
        }
        return roles.stream()
                .map(role -> {
                    RoleDto roleDto = new RoleDto();
                    roleDto.setId(role.getId());
                    roleDto.setName(role.getName());
                    return roleDto;
                })
                .collect(Collectors.toSet());
    }

    public FullUserProfileDto toFullProfileDto(User user) {
        if (user == null) {
            return null;
        }
        FullUserProfileDto dto = new FullUserProfileDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setProfileStatus(user.getProfileStatus());

        dto.setRoles(toRoleDtoSet(user.getRoles()));
        dto.setSkills(user.getSkills() != null ?
                user.getSkills().stream().map(Skill::getName).collect(Collectors.toSet()) :
                Collections.emptySet());
        dto.setCertifications(user.getCertifications() != null ? user.getCertifications() : Collections.emptySet());

        dto.setSiteSupervisor(toSimpleUserDto(user.getSiteSupervisor()));
        dto.setProjectManager(toSimpleUserDto(user.getProjectManager()));

        // The following lists are populated conditionally in UserProfileService
        // They are initialized as empty lists here, or the service can override them.
        dto.setManagedTeam(Collections.emptyList()); // Will be set in service
        dto.setManagedProjects(Collections.emptyList()); // Will be set in service
        dto.setSupervisedWorkers(Collections.emptyList()); // Will be set in service
        dto.setSupervisedTasks(Collections.emptyList()); // Will be set in service
        dto.setManagedEquipment(Collections.emptyList()); // Will be set in service
        dto.setWorkerAssignments(Collections.emptyList()); // Will be set in service
        dto.setWorkerAvailabilitySlots(Collections.emptyList()); // Will be set in service
        dto.setWorksUnder(Collections.emptyList()); // Will be set in service

        return dto;
    }
}