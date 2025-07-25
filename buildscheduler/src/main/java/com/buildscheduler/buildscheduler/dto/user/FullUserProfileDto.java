package com.buildscheduler.buildscheduler.dto.user;

import com.buildscheduler.buildscheduler.dto.auth.RoleDto;
import com.buildscheduler.buildscheduler.dto.equipment_manager.EquipmentResponseDto;
import com.buildscheduler.buildscheduler.dto.project_manager.MainTaskResponseDto;
import com.buildscheduler.buildscheduler.dto.project_manager.FullProjectResponseDto;
import com.buildscheduler.buildscheduler.dto.site_supervisor.WorkerAssignmentDto;
import com.buildscheduler.buildscheduler.dto.worker.AvailabilitySlotDto;
import com.buildscheduler.buildscheduler.dto.worker.SimpleUserDto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
public class FullUserProfileDto {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private Set<RoleDto> roles;
    private Set<String> skills;
    private Set<String> certifications;
    private String profileStatus;

    // These represent who the user *reports to directly*
    private SimpleUserDto siteSupervisor; // Who this user's site supervisor is (if applicable)
    private SimpleUserDto projectManager; // Who this user's project manager is (if applicable)

    // New field: Who this user works under (can be multiple based on assignments)
    private List<UserInfoDto> worksUnder;

    // For Project Managers:
    private List<UserInfoDto> managedTeam; // List of SimpleUserDto for direct reports
    private List<FullProjectResponseDto> managedProjects;

    // For Site Supervisors:
    private List<UserInfoDto> supervisedWorkers; // List of SimpleUserDto for workers they supervise
    private List<MainTaskResponseDto> supervisedTasks;

    // For Equipment Managers:
    private List<EquipmentResponseDto> managedEquipment;

    // For Workers:
    private List<WorkerAssignmentDto> workerAssignments;
    private List<AvailabilitySlotDto> workerAvailabilitySlots;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfoDto {
        private Long id;
        private String username;
        private String email;
        private Set<String> roles; // Changed to Set<String> for roles names
    }
}