package com.buildscheduler.buildscheduler.controller.equipment_manager; // Adjust package as necessary

import com.buildscheduler.buildscheduler.dto.equipment_manager.EquipmentMaintenanceAlertDto;
import com.buildscheduler.buildscheduler.response.ApiResponse; // Assuming your ApiResponse class
import com.buildscheduler.buildscheduler.service.impl.EquipmentService; // Your equipment service

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment-manager") // Or your general equipment endpoint
@RequiredArgsConstructor
public class EquipmentManagerController { // Rename if this is a general EquipmentController

    private final EquipmentService equipmentService; // Inject your new service

    // Endpoint for equipment managers to see alerts for their equipment
    @GetMapping("/maintenance-alerts")
    @PreAuthorize("hasRole('EQUIPMENT_MANAGER')") // Restrict access to Equipment Managers
    public ResponseEntity<ApiResponse<List<EquipmentMaintenanceAlertDto>>> getMaintenanceAlerts() {
        List<EquipmentMaintenanceAlertDto> alerts = equipmentService.getMaintenanceAlertsForManager();
        return ResponseEntity.ok(ApiResponse.ofSuccess("Maintenance alerts fetched successfully", alerts));
    }

    // Optional: Endpoint for an admin or central dashboard to see all alerts
    @GetMapping("/maintenance-alerts/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')") // Adjust roles as needed
    public ResponseEntity<ApiResponse<List<EquipmentMaintenanceAlertDto>>> getAllMaintenanceAlerts() {
        List<EquipmentMaintenanceAlertDto> alerts = equipmentService.getAllMaintenanceAlerts();
        return ResponseEntity.ok(ApiResponse.ofSuccess("All maintenance alerts fetched successfully", alerts));
    }

    // Endpoint to record maintenance (e.g., after a technician performs it)
    @PatchMapping("/equipment/{equipmentId}/record-maintenance")
    @PreAuthorize("hasAnyRole('EQUIPMENT_MANAGER', 'ADMIN')") // Adjust roles
    public ResponseEntity<ApiResponse<String>> recordEquipmentMaintenance(@PathVariable Long equipmentId) {
        try {
            equipmentService.recordMaintenance(equipmentId);
            return ResponseEntity.ok(ApiResponse.ofSuccess("Maintenance recorded successfully for equipment " + equipmentId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.ofError("Failed to record maintenance: " + e.getMessage()));
        }
    }
}