package com.buildscheduler.buildscheduler.controller;

import com.buildscheduler.buildscheduler.dto.AssignmentDto;
import com.buildscheduler.buildscheduler.model.Assignment;
import com.buildscheduler.buildscheduler.response.ApiResponse;
import com.buildscheduler.buildscheduler.service.custom.AssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {
    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Assignment>> assignWorker(@RequestBody AssignmentDto dto) {
        Assignment assignment = assignmentService.assignWorker(dto);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Worker assigned successfully", assignment));
    }

    @GetMapping("/worker/{workerId}")
    public ResponseEntity<ApiResponse<List<Assignment>>> getWorkerAssignments(@PathVariable Long workerId) {
        List<Assignment> assignments = assignmentService.getWorkerAssignments(workerId);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Worker assignments retrieved", assignments));
    }

    @DeleteMapping("/{assignmentId}")
    public ResponseEntity<ApiResponse<Void>> removeAssignment(@PathVariable Long assignmentId) {
        assignmentService.removeAssignment(assignmentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Assignment removed successfully", null));
    }
}