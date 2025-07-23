package com.buildscheduler.buildscheduler.controller.site_supervisor;

import com.buildscheduler.buildscheduler.dto.project_manager.MainTaskResponseDto;
import com.buildscheduler.buildscheduler.dto.project_manager.ProjectResponseDto;
import com.buildscheduler.buildscheduler.dto.project_manager.ProjectStructureResponse;
import com.buildscheduler.buildscheduler.dto.site_supervisor.*;
import com.buildscheduler.buildscheduler.exception.ConflictException;
import com.buildscheduler.buildscheduler.exception.ResourceNotFoundException;
import com.buildscheduler.buildscheduler.model.User;
import com.buildscheduler.buildscheduler.repository.UserRepository;
import com.buildscheduler.buildscheduler.response.ApiResponse; // Make sure this is the correct ApiResponse class
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
    private final SiteSupervisorAssignmentService assignmentService;

    @GetMapping("/projects")
    @PreAuthorize("hasRole('SITE_SUPERVISOR')")
    public ResponseEntity<ApiResponse<List<ProjectResponseDto>>> getAssignedProjects() {
        List<ProjectResponseDto> projects = projectService.getProjectsForSupervisor();
        return ResponseEntity.ok(ApiResponse.ofSuccess("Projects fetched successfully", projects));
    }

    @GetMapping("/main-tasks/{mainTaskId}/subtasks")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<SubtaskDetailDto>>> getSubtasksByMainTask(
            @PathVariable Long mainTaskId
    ) {
        List<SubtaskDetailDto> subtasks = projectService.getSubtasksByMainTaskId(mainTaskId);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Subtasks fetched successfully", subtasks));
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

    // New Endpoint to update Subtask Status
    //-------------------------------------------------------------------------
    @PatchMapping("/subtasks/{subtaskId}/status")
    @PreAuthorize("hasRole('SITE_SUPERVISOR')")
    public ResponseEntity<ApiResponse<SubtaskResponseDto>> updateSubtaskStatus(
            @PathVariable Long subtaskId,
            @Valid @RequestBody SubtaskStatusUpdateDto dto // Use the new DTO
    ) {
        try {
            SubtaskResponseDto updatedSubtask = subtaskService.updateSubtaskStatus(subtaskId, dto);
            return ResponseEntity.ok(ApiResponse.ofSuccess("Subtask status updated successfully", updatedSubtask));
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>(ApiResponse.ofError(ex.getMessage()), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>(ApiResponse.ofError(ex.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            // Log the exception
            return new ResponseEntity<>(ApiResponse.ofError("Failed to update subtask status: " + ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    //-------------------------------------------------------------------------


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

    @PreAuthorize("hasRole('SITE_SUPERVISOR')")
    @GetMapping("/subtasks/{subtaskId}/workers/search")
    public ResponseEntity<ApiResponse<List<WorkerSearchResultDto>>> searchWorkersForSubtask(@PathVariable Long subtaskId) {
        try {
            List<WorkerSearchResultDto> matchedWorkers = assignmentService.findBestMatchedWorkers(subtaskId);
            return ResponseEntity.ok(ApiResponse.ofSuccess("Best matched workers fetched successfully", matchedWorkers));
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>(ApiResponse.ofError(ex.getMessage()), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            return new ResponseEntity<>(ApiResponse.ofError("Failed to search workers: " + ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    // Worker Assignment Endpoints
    //-------------------------------------------------------------------------

    @PreAuthorize("hasRole('SITE_SUPERVISOR')")
    @PostMapping("/subtasks/{subtaskId}/workers/assign")
    public ResponseEntity<ApiResponse<Void>> assignWorkerToSubtask(
            @PathVariable Long subtaskId,
            @Valid @RequestBody AssignmentRequestDto assignmentRequest
    ) {
        try {
            assignmentService.assignWorkerToSubtask(subtaskId, assignmentRequest);
            // Return 201 Created for resource creation without a specific body
            return new ResponseEntity<>(ApiResponse.ofSuccess("Worker assigned successfully"), HttpStatus.CREATED);
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>(ApiResponse.ofError(ex.getMessage()), HttpStatus.NOT_FOUND);
        } catch (ConflictException ex) {
            return new ResponseEntity<>(ApiResponse.ofError(ex.getMessage()), HttpStatus.CONFLICT);
        } catch (Exception ex) {
            // Log the exception for debugging in production
            return new ResponseEntity<>(ApiResponse.ofError("Failed to assign worker: " + ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('SITE_SUPERVISOR')")
    @DeleteMapping("/assignments/{assignmentId}")
    public ResponseEntity<ApiResponse<Void>> removeWorkerAssignment(@PathVariable Long assignmentId) {
        try {
            assignmentService.removeWorkerAssignment(assignmentId);
            // Return 200 OK with a success message for successful deletion
            return new ResponseEntity<>(ApiResponse.ofSuccess("Worker assignment removed successfully"), HttpStatus.OK);
        } catch (ResourceNotFoundException ex) {
            return new ResponseEntity<>(ApiResponse.ofError(ex.getMessage()), HttpStatus.NOT_FOUND);
        } catch (Exception ex) {
            // Log the exception
            return new ResponseEntity<>(ApiResponse.ofError("Failed to remove worker assignment: " + ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}