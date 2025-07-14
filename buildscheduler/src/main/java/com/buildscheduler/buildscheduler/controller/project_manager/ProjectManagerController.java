package com.buildscheduler.buildscheduler.controller.project_manager;

import com.buildscheduler.buildscheduler.dto.project_manager.RoleUpdateDto;
import com.buildscheduler.buildscheduler.dto.auth.UserDto;
import com.buildscheduler.buildscheduler.response.ApiResponse;
import com.buildscheduler.buildscheduler.service.custom.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pm")
public class ProjectManagerController {

    private final UserService userService;

    public ProjectManagerController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/update-role")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<UserDto>> updateUserRole(@Valid @RequestBody RoleUpdateDto roleUpdateDto) {
        UserDto updatedUser = userService.updateUserRole(roleUpdateDto);
        return ResponseEntity.ok(ApiResponse.ofSuccess("User role updated successfully", updatedUser));
    }
}
