package com.buildscheduler.buildscheduler.dto.user;

import com.buildscheduler.buildscheduler.dto.auth.RoleDto;
import com.buildscheduler.buildscheduler.dto.equipment_manager.EquipmentResponseDto;
import com.buildscheduler.buildscheduler.dto.project_manager.MainTaskResponseDto;
import com.buildscheduler.buildscheduler.dto.project_manager.FullProjectResponseDto; // <--- CHANGE THIS IMPORT
import com.buildscheduler.buildscheduler.dto.site_supervisor.WorkerAssignmentDto;
import com.buildscheduler.buildscheduler.dto.worker.AvailabilitySlotDto;
import com.buildscheduler.buildscheduler.dto.worker.SimpleUserDto;
import lombok.Data;

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

    private SimpleUserDto siteSupervisor;
    private SimpleUserDto projectManager;

    // For Project Managers:
    private List<SimpleUserDto> managedTeam;
    private List<FullProjectResponseDto> managedProjects; // <--- CHANGE THIS TYPE

    // For Site Supervisors:
    private List<SimpleUserDto> supervisedWorkers;
    private List<MainTaskResponseDto> supervisedTasks;

    // For Equipment Managers:
    private List<EquipmentResponseDto> managedEquipment;

    // For Workers:
    private List<WorkerAssignmentDto> workerAssignments;
    private List<AvailabilitySlotDto> workerAvailabilitySlots;
}