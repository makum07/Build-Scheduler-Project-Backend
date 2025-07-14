package com.buildscheduler.buildscheduler.controller.worker;

import com.buildscheduler.buildscheduler.model.Assignment;
import com.buildscheduler.buildscheduler.response.ApiResponse;
import com.buildscheduler.buildscheduler.service.custom.AssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/worker/assignments")
public class WorkersAssignments {

    private final AssignmentService assignmentService;

    public WorkersAssignments(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping("/{workerId}")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<ApiResponse<List<Assignment>>> getWorkerAssignments(@PathVariable Long workerId) {
        List<Assignment> assignments = assignmentService.getWorkerAssignments(workerId);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Worker assignments retrieved", assignments));
    }
}