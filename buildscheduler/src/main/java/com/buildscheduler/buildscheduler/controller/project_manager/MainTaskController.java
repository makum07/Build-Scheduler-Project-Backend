package com.buildscheduler.buildscheduler.controller.project_manager;

import com.buildscheduler.buildscheduler.dto.project_manager.MainTaskRequestDto;
import com.buildscheduler.buildscheduler.dto.project_manager.MainTaskResponseDto;
import com.buildscheduler.buildscheduler.response.ApiResponse;
import com.buildscheduler.buildscheduler.service.custom.MainTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pm/projects/{projectId}/main-tasks")
@RequiredArgsConstructor
public class MainTaskController {

    private final MainTaskService mainTaskService;

    @PostMapping
    public ResponseEntity<ApiResponse<MainTaskResponseDto>> createMainTask(
            @PathVariable Long projectId,
            @RequestBody MainTaskRequestDto dto) {
        MainTaskResponseDto response = mainTaskService.createMainTask(dto, projectId);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Main task created successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MainTaskResponseDto>> updateMainTask(
            @PathVariable Long id,
            @RequestBody MainTaskRequestDto dto) {
        MainTaskResponseDto updatedTask = mainTaskService.updateMainTask(id, dto);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Main task updated successfully", updatedTask));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMainTask(@PathVariable Long id) {
        mainTaskService.deleteMainTask(id);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Main task deleted successfully", null));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<MainTaskResponseDto>>> getMainTasksByProject(
            @PathVariable Long projectId,
            Pageable pageable) {
        Page<MainTaskResponseDto> tasks = mainTaskService.getMainTasksByProject(projectId, pageable);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Main tasks fetched successfully", tasks));
    }
}
