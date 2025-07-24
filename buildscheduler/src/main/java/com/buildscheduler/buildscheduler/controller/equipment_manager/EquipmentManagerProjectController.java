package com.buildscheduler.buildscheduler.controller.equipment_manager;

import com.buildscheduler.buildscheduler.dto.equipment_manager.EquipmentManagerProjectResponseDto;
import com.buildscheduler.buildscheduler.response.ApiResponse; // Import your ApiResponse class
import com.buildscheduler.buildscheduler.service.impl.EquipmentManagerProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/equipment-manager/projects")
@RequiredArgsConstructor
public class EquipmentManagerProjectController {

    private final EquipmentManagerProjectService equipmentManagerProjectService;

    @GetMapping
    @PreAuthorize("hasRole('EQUIPMENT_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<EquipmentManagerProjectResponseDto>>> getProjectsForEquipmentManager() {
        List<EquipmentManagerProjectResponseDto> projects = equipmentManagerProjectService.getProjectsForEquipmentManager();

        // Use ApiResponse.ofSuccess to wrap the response
        if (projects.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.ofSuccess("No projects found for the current equipment manager.", projects));
        } else {
            return ResponseEntity.ok(ApiResponse.ofSuccess("Projects fetched successfully for equipment manager", projects));
        }
    }
}