package com.buildscheduler.buildscheduler.controller.site_supervisor;

import com.buildscheduler.buildscheduler.dto.project_manager.MainTaskResponseDto;
import com.buildscheduler.buildscheduler.dto.project_manager.ProjectResponseDto;
import com.buildscheduler.buildscheduler.dto.project_manager.ProjectStructureResponse;
import com.buildscheduler.buildscheduler.dto.site_supervisor.*;
import com.buildscheduler.buildscheduler.exception.ResourceNotFoundException;
import com.buildscheduler.buildscheduler.model.User;
import com.buildscheduler.buildscheduler.repository.UserRepository;
import com.buildscheduler.buildscheduler.response.ApiResponse;
import com.buildscheduler.buildscheduler.service.impl.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/site-supervisor")
@RequiredArgsConstructor
public class SiteSupervisorController {

    private final SiteSupervisorProjectService projectService;
    private final SiteSupervisorSubtaskService subtaskService;
//    private final SiteSupervisorAssignmentService assignmentService;
//    private final SiteSupervisorEquipmentService equipmentService;
//    private final ConflictDetectionService conflictService;
//    private final UserRepository userRepository;



    @GetMapping("/projects")
    @PreAuthorize("hasRole('SITE_SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<ProjectResponseDto>>> getAssignedProjects() {
        List<ProjectResponseDto> projects = projectService.getProjectsForSupervisor();
        return ResponseEntity.ok(ApiResponse.ofSuccess("Projects fetched successfully", projects));
    }



    @PostMapping("/main-tasks/{mainTaskId}/subtasks")
    @PreAuthorize("hasRole('SITE_SUPERVISOR')")
    public ResponseEntity<ApiResponse<SubtaskResponseDto>> createSubtask(
            @PathVariable Long mainTaskId,
            @Valid @RequestBody SubtaskRequestDto dto
    ) {
        SubtaskResponseDto subtask = subtaskService.createSubtask(dto, mainTaskId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ofSuccess("Subtask created successfully", subtask));
    }

    @PutMapping("/subtasks/{subtaskId}")
    @PreAuthorize("hasRole('SITE_SUPERVISOR')")
    public ResponseEntity<ApiResponse<SubtaskResponseDto>> updateSubtask(
            @PathVariable Long subtaskId,
            @Valid @RequestBody SubtaskRequestDto dto
    ) {
        SubtaskResponseDto updatedSubtask = subtaskService.updateSubtask(subtaskId, dto);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Subtask updated successfully", updatedSubtask));
    }


    @DeleteMapping("/subtasks/{subtaskId}")
    @PreAuthorize("hasRole('SITE_SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> deleteSubtask(
            @PathVariable Long subtaskId
    ) {
        subtaskService.deleteSubtask(subtaskId);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Subtask deleted successfully", null));
    }

    // Add endpoints for skill/equipment removal
    @DeleteMapping("/subtasks/{subtaskId}/skills/{skillName}")
    @PreAuthorize("hasRole('SITE_SUPERVISOR')")
    public ResponseEntity<ApiResponse<SubtaskResponseDto>> removeSkillFromSubtask(
            @PathVariable Long subtaskId,
            @PathVariable String skillName
    ) {
        SubtaskResponseDto updatedSubtask = subtaskService.removeSkillFromSubtask(subtaskId, skillName);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Skill removed successfully", updatedSubtask));
    }

    @DeleteMapping("/subtasks/{subtaskId}/equipment/{equipmentId}")
    @PreAuthorize("hasRole('SITE_SUPERVISOR')")
    public ResponseEntity<ApiResponse<SubtaskResponseDto>> removeEquipmentFromSubtask(
            @PathVariable Long subtaskId,
            @PathVariable Long equipmentId
    ) {
        SubtaskResponseDto updatedSubtask = subtaskService.removeEquipmentFromSubtask(subtaskId, equipmentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Equipment removed successfully", updatedSubtask));
    }
//    @PostMapping("/assignments")
//    @PreAuthorize("hasRole('SITE_SUPERVISOR')")
//    public ResponseEntity<ApiResponse<AssignmentDto>> assignWorker(
//            @Valid @RequestBody AssignmentRequestDto dto
//    ) {
//        User supervisor = getCurrentUser();
//        AssignmentDto assignment = assignmentService.assignWorker(dto, supervisor);
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(ApiResponse.ofSuccess("Worker assigned successfully", assignment));
//    }
//
//    @DeleteMapping("/assignments/{assignmentId}")
//    @PreAuthorize("hasRole('SITE_SUPERVISOR')")
//    public ResponseEntity<ApiResponse<Void>> removeWorkerAssignment(
//            @PathVariable Long assignmentId
//    ) {
//        User supervisor = getCurrentUser();
//        assignmentService.removeWorkerAssignment(assignmentId, supervisor);
//        return ResponseEntity.ok(ApiResponse.ofSuccess("Assignment removed successfully", null));
//    }
//
//    @DeleteMapping("/equipment-assignments/{assignmentId}")
//    @PreAuthorize("hasRole('SITE_SUPERVISOR')")
//    public ResponseEntity<ApiResponse<Void>> removeEquipmentAssignment(
//            @PathVariable Long assignmentId
//    ) {
//        User supervisor = getCurrentUser();
//        equipmentService.removeEquipmentAssignment(assignmentId, supervisor);
//        return ResponseEntity.ok(ApiResponse.ofSuccess("Equipment assignment removed successfully", null));
//    }
//
//    @GetMapping("/subtasks/{subtaskId}/qualified-workers")
//    @PreAuthorize("hasRole('SITE_SUPERVISOR')")
//    public ResponseEntity<ApiResponse<List<WorkerAvailabilityDto>>> getQualifiedWorkers(
//            @PathVariable Long subtaskId
//    ) {
//        User supervisor = getCurrentUser();
//        List<WorkerAvailabilityDto> workers = assignmentService.getQualifiedAvailableWorkers(subtaskId, supervisor);
//        return ResponseEntity.ok(ApiResponse.ofSuccess("Qualified workers fetched", workers));
//    }
//
//    @GetMapping("/subtasks/{subtaskId}/conflicts")
//    @PreAuthorize("hasRole('SITE_SUPERVISOR')")
//    public ResponseEntity<ApiResponse<Map<String, List<String>>>> checkSubtaskConflicts(
//            @PathVariable Long subtaskId
//    ) {
//        User supervisor = getCurrentUser();
//        Map<String, List<String>> conflicts = conflictService.detectSubtaskConflicts(subtaskId, supervisor);
//        return ResponseEntity.ok(ApiResponse.ofSuccess("Conflict analysis completed", conflicts));
//    }
}