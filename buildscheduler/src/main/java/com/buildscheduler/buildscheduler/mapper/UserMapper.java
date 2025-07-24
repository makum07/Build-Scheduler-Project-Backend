package com.buildscheduler.buildscheduler.mapper;

import com.buildscheduler.buildscheduler.dto.auth.RoleDto;
import com.buildscheduler.buildscheduler.dto.auth.UserDto;
import com.buildscheduler.buildscheduler.dto.equipment_manager.EquipmentResponseDto;
import com.buildscheduler.buildscheduler.dto.project_manager.MainTaskResponseDto;
import com.buildscheduler.buildscheduler.dto.project_manager.ProjectResponseDto; // Note: ProjectMapper maps to FullProjectResponseDto, but FullUserProfileDto expects ProjectResponseDto. Check your DTOs if this is intentional.
import com.buildscheduler.buildscheduler.dto.project_manager.UserTableDto;
import com.buildscheduler.buildscheduler.dto.site_supervisor.WorkerAssignmentDto;
import com.buildscheduler.buildscheduler.dto.user.FullUserProfileDto;
import com.buildscheduler.buildscheduler.dto.worker.AvailabilitySlotDto;
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
@RequiredArgsConstructor // Automatically creates constructor for final fields
public class UserMapper implements Mapper<User, UserDto> {

    // Inject other mappers that UserMapper depends on for complex mappings
    private final ProjectMapper projectMapper;
    private final MainTaskMapper mainTaskMapper;
    private final AssignmentMapper assignmentMapper;
    private final AvailabilitySlotMapper availabilitySlotMapper;
    // If you create a dedicated EquipmentMapper, uncomment and inject it:
    // private final EquipmentMapper equipmentMapper;

    /**
     * Converts a User entity to a basic UserDto.
     * Used for general user listings, not full profile.
     * @param entity The User entity.
     * @return The UserDto.
     */
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

    /**
     * Converts a UserDto to a User entity.
     * Note: This typically doesn't handle complex relationships (roles, skills, etc.)
     * and is often used for creating/updating basic user details.
     * @param dto The UserDto.
     * @return The User entity.
     */
    @Override
    public User toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }
        User user = new User();
        // ID is usually set by JPA upon creation or looked up for update
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        // Roles and other collections would be handled by a separate service logic.
        return user;
    }

    /**
     * Helper method to convert strings to Title Case.
     * @param input The string to convert.
     * @return The title-cased string.
     */
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

    /**
     * Converts a User entity to a UserTableDto for tabular display.
     * @param user The User entity.
     * @return The UserTableDto.
     */
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

    /**
     * Converts a list of User entities to a list of UserTableDto.
     * @param users The list of User entities.
     * @return The list of UserTableDto.
     */
    public List<UserTableDto> toUserTableDtos(List<User> users) {
        if (users == null) {
            return Collections.emptyList();
        }
        return users.stream()
                .map(this::toUserTableDto)
                .collect(Collectors.toList());
    }

    /**
     * Converts a User entity to a SimpleUserDto.
     * Used for nested relationships (e.g., manager, worker in assignments) to prevent recursion.
     * @param user The User entity.
     * @return The SimpleUserDto.
     */
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

    /**
     * Converts a User entity to a comprehensive FullUserProfileDto.
     * This method maps all relevant details and related entities (as DTOs).
     * It relies on other mappers for complex nested objects.
     * @param user The User entity with potentially lazy-loaded collections initialized.
     * @return The FullUserProfileDto.
     */
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

        // Map roles
        if (user.getRoles() != null) {
            dto.setRoles(user.getRoles().stream()
                    .map(role -> {
                        RoleDto roleDto = new RoleDto();
                        roleDto.setId(role.getId());
                        roleDto.setName(role.getName());
                        return roleDto;
                    })
                    .collect(Collectors.toSet()));
        } else {
            dto.setRoles(Collections.emptySet());
        }

        // Map skills (assuming you just want names as strings)
        if (user.getSkills() != null) {
            dto.setSkills(user.getSkills().stream()
                    .map(Skill::getName) // Assuming Skill entity has getName()
                    .collect(Collectors.toSet()));
        } else {
            dto.setSkills(Collections.emptySet());
        }

        // Map certifications (assumed to be a Set<String>)
        dto.setCertifications(user.getCertifications() != null ? user.getCertifications() : Collections.emptySet());

        // Map direct managers (using SimpleUserDto to prevent recursion)
        dto.setSiteSupervisor(toSimpleUserDto(user.getSiteSupervisor()));
        dto.setProjectManager(toSimpleUserDto(user.getProjectManager()));

        // --- Map direct reports/managed entities based on potential roles ---
        // These collections are @JsonIgnore in User, so they must be accessed within a transactional context
        // and mapped to DTOs here. They would have been initialized in the service layer if needed.

        // For Project Managers: Managed Team (Site Supervisors, Equipment Managers, Workers)
        if (user.getManagedTeam() != null && !user.getManagedTeam().isEmpty()) {
            dto.setManagedTeam(user.getManagedTeam().stream()
                    .map(this::toSimpleUserDto) // Map to SimpleUserDto
                    .collect(Collectors.toList()));
        } else {
            dto.setManagedTeam(Collections.emptyList());
        }

        // For Project Managers: Managed Projects
        if (user.getManagedProjects() != null && !user.getManagedProjects().isEmpty()) {
            // ProjectMapper maps to FullProjectResponseDto. Ensure FullUserProfileDto has a field of this type or adjust.
            // If FullProjectResponseDto is too heavy, consider creating a lighter ProjectSummaryDto.
            dto.setManagedProjects(user.getManagedProjects().stream()
                    .map(projectMapper::toFullProjectDto) // Using the correct method name from ProjectMapper
                    .collect(Collectors.toList()));
        } else {
            dto.setManagedProjects(Collections.emptyList());
        }

        // For Site Supervisors: Supervised Workers
        if (user.getSupervisedWorkers() != null && !user.getSupervisedWorkers().isEmpty()) {
            dto.setSupervisedWorkers(user.getSupervisedWorkers().stream()
                    .map(this::toSimpleUserDto) // Map to SimpleUserDto
                    .collect(Collectors.toList()));
        } else {
            dto.setSupervisedWorkers(Collections.emptyList());
        }

        // For Site Supervisors: Supervised Tasks (MainTasks)
        if (user.getSupervisedTasks() != null && !user.getSupervisedTasks().isEmpty()) {
            dto.setSupervisedTasks(user.getSupervisedTasks().stream()
                    .map(mainTaskMapper::toResponseDto) // Assuming MainTaskMapper has toResponseDto()
                    .collect(Collectors.toList()));
        } else {
            dto.setSupervisedTasks(Collections.emptyList());
        }

        // For Equipment Managers: Managed Equipment
        if (user.getManagedEquipment() != null && !user.getManagedEquipment().isEmpty()) {
            // If you have a dedicated EquipmentMapper, use it here: equipmentMapper::toResponseDto
            dto.setManagedEquipment(user.getManagedEquipment().stream()
                    .map(this::mapEquipmentToResponseDto) // Using local helper for now
                    .collect(Collectors.toList()));
        } else {
            dto.setManagedEquipment(Collections.emptyList());
        }

        // For Workers: Worker Assignments
        if (user.getWorkerAssignments() != null && !user.getWorkerAssignments().isEmpty()) {
            dto.setWorkerAssignments(user.getWorkerAssignments().stream()
                    .map(assignmentMapper::toWorkerAssignmentDto) // Assuming AssignmentMapper has toWorkerAssignmentDto()
                    .collect(Collectors.toList()));
        } else {
            dto.setWorkerAssignments(Collections.emptyList());
        }

        // For Workers: Worker Availability Slots
        if (user.getWorkerAvailabilitySlots() != null && !user.getWorkerAvailabilitySlots().isEmpty()) {
            dto.setWorkerAvailabilitySlots(user.getWorkerAvailabilitySlots().stream()
                    .map(availabilitySlotMapper::toDto) // Using the correct method name toDto()
                    .collect(Collectors.toList()));
        } else {
            dto.setWorkerAvailabilitySlots(Collections.emptyList());
        }

        return dto;
    }

    /**
     * Helper method to map an Equipment entity to an EquipmentResponseDto.
     * Consider moving this to a dedicated EquipmentMapper if it grows in complexity.
     * @param equipment The Equipment entity.
     * @return The EquipmentResponseDto.
     */
    private EquipmentResponseDto mapEquipmentToResponseDto(Equipment equipment) {
        if (equipment == null) return null;
        EquipmentResponseDto dto = new EquipmentResponseDto();
        dto.setId(equipment.getId());
        dto.setName(equipment.getName());
        dto.setModel(equipment.getModel());
        dto.setSerialNumber(equipment.getSerialNumber());
        dto.setType(equipment.getType());
        dto.setPurchasePrice(equipment.getPurchasePrice());
        dto.setWarrantyMonths(equipment.getWarrantyMonths());
        dto.setMaintenanceIntervalDays(equipment.getMaintenanceIntervalDays());
        dto.setLastMaintenanceDate(equipment.getLastMaintenanceDate());
        dto.setMaintenanceDue(equipment.isMaintenanceDue());
        dto.setLocation(equipment.getLocation());
        dto.setNotes(equipment.getNotes());
        return dto;
    }
}