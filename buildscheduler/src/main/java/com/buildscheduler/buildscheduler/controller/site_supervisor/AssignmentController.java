package com.buildscheduler.buildscheduler.controller.site_supervisor;

import com.buildscheduler.buildscheduler.dto.site_supervisor.AssignmentDto;
import com.buildscheduler.buildscheduler.model.Assignment;
import com.buildscheduler.buildscheduler.response.ApiResponse;
import com.buildscheduler.buildscheduler.service.custom.AssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/supervisor/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('SITE_SUPERVISOR')")
    public ResponseEntity<ApiResponse<Assignment>> assignWorker(@RequestBody AssignmentDto dto) {
        Assignment assignment = assignmentService.assignWorker(dto);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Worker assigned successfully", assignment));
    }

    @DeleteMapping("/{assignmentId}")
    @PreAuthorize("hasRole('SITE_SUPERVISOR')")
    public ResponseEntity<ApiResponse<Void>> removeAssignment(@PathVariable Long assignmentId) {
        assignmentService.removeAssignment(assignmentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Assignment removed successfully", null));
    }
}