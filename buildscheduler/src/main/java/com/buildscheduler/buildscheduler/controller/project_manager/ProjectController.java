package com.buildscheduler.buildscheduler.controller.project_manager;

import com.buildscheduler.buildscheduler.dto.project_manager.ProjectRequestDto;
import com.buildscheduler.buildscheduler.dto.project_manager.ProjectResponseDto;
import com.buildscheduler.buildscheduler.dto.project_manager.ProjectStructureResponse;
import com.buildscheduler.buildscheduler.dto.project_manager.UserTableDto;
import com.buildscheduler.buildscheduler.mapper.UserMapper;
import com.buildscheduler.buildscheduler.model.User;
import com.buildscheduler.buildscheduler.repository.UserRepository;
import com.buildscheduler.buildscheduler.response.ApiResponse;
import com.buildscheduler.buildscheduler.security.CustomUserDetailsService;
import com.buildscheduler.buildscheduler.service.custom.ProjectService;
import com.buildscheduler.buildscheduler.service.impl.ProjectStructureService;  // <-- import your dedicated service
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pm/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectStructureService projectStructureService;  // <-- injected
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final CustomUserDetailsService userDetailsService;

    @PostMapping
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> createProject(
            @RequestBody ProjectRequestDto dto,
            @AuthenticationPrincipal User currentUser) {

        ProjectResponseDto response = projectService.createProject(dto, currentUser);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Project created successfully", response));
    }

    @GetMapping("/manager")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<Page<ProjectResponseDto>>> getProjectsByManager(
            @AuthenticationPrincipal User currentUser,
            Pageable pageable) {

        Page<ProjectResponseDto> projects = projectService.getProjectsByManager(currentUser, pageable);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Projects fetched successfully", projects));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'SITE_SUPERVISOR', 'EQUIPMENT_MANAGER')")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> getProjectById(@PathVariable Long id) {
        ProjectResponseDto project = projectService.getProjectById(id);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Project fetched successfully", project));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<ProjectResponseDto>> updateProject(
            @PathVariable Long id,
            @RequestBody ProjectRequestDto dto) {

        ProjectResponseDto updatedProject = projectService.updateProject(id, dto);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Project updated successfully", updatedProject));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Project deleted successfully", null));
    }

    @PostMapping("/{projectId}/assign-supervisor")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> assignSupervisor(
            @PathVariable Long projectId,
            @RequestParam Long supervisorId) {

        projectService.assignSupervisor(projectId, supervisorId);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Supervisor assigned successfully", null));
    }

    @PostMapping("/{projectId}/assign-equipment-manager")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> assignEquipmentManager(
            @PathVariable Long projectId,
            @RequestParam Long equipmentManagerId) {

        projectService.assignEquipmentManager(projectId, equipmentManagerId);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Equipment Manager assigned successfully", null));
    }

    @GetMapping("/{id}/structure")
    @PreAuthorize("hasAnyRole('PROJECT_MANAGER', 'SITE_SUPERVISOR', 'EQUIPMENT_MANAGER')")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<ProjectStructureResponse>> getProjectStructure(@PathVariable Long id) {
        ProjectStructureResponse structure = projectStructureService.getProjectStructure(id);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Project structure fetched successfully", structure));
    }

    @GetMapping("/supervisors")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<List<UserTableDto>>> getSupervisors() {
        List<User> supervisors = userRepository.findByRoles_Name("SITE_SUPERVISOR");
        List<UserTableDto> dtos = userMapper.toUserTableDtos(supervisors);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Supervisors fetched successfully", dtos));
    }

    @GetMapping("/equipment-managers")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<List<UserTableDto>>> getEquipmentManagers() {
        List<User> equipmentManagers = userRepository.findByRoles_Name("EQUIPMENT_MANAGER");
        List<UserTableDto> dtos = userMapper.toUserTableDtos(equipmentManagers);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Equipment Managers fetched successfully", dtos));
    }
}
