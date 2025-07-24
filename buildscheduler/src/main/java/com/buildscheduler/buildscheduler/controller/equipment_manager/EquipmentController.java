package com.buildscheduler.buildscheduler.controller.equipment_manager;

import com.buildscheduler.buildscheduler.dto.equipment_manager.EquipmentRequestDto;
import com.buildscheduler.buildscheduler.dto.equipment_manager.EquipmentResponseDto;
import com.buildscheduler.buildscheduler.response.ApiResponse; // Your custom ApiResponse class
import com.buildscheduler.buildscheduler.service.impl.EquipmentManagementService; // The new service
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment") // Base path for all equipment APIs
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentManagementService equipmentManagementService;

    // --- Create Equipment ---
    @PostMapping
    @PreAuthorize("hasRole('EQUIPMENT_MANAGER')")
    public ResponseEntity<ApiResponse<EquipmentResponseDto>> createEquipment(
            @Valid @RequestBody EquipmentRequestDto dto) {
        EquipmentResponseDto createdEquipment = equipmentManagementService.createEquipment(dto);
        return new ResponseEntity<>(ApiResponse.ofSuccess("Equipment created successfully", createdEquipment), HttpStatus.CREATED);
    }

    // --- Update Equipment ---
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EQUIPMENT_MANAGER', 'ADMIN')") // Manager for their own, Admin for all
    public ResponseEntity<ApiResponse<EquipmentResponseDto>> updateEquipment(
            @PathVariable Long id,
            @Valid @RequestBody EquipmentRequestDto dto) {
        EquipmentResponseDto updatedEquipment = equipmentManagementService.updateEquipment(id, dto);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Equipment updated successfully", updatedEquipment));
    }

    // --- Delete Equipment ---
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EQUIPMENT_MANAGER', 'ADMIN')") // Manager for their own, Admin for all
    public ResponseEntity<ApiResponse<Void>> deleteEquipment(@PathVariable Long id) {
        equipmentManagementService.deleteEquipment(id);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Equipment deleted successfully", null));
    }

    // --- Get Equipment by ID ---
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EQUIPMENT_MANAGER', 'PROJECT_MANAGER', 'ADMIN')") // Broad access for viewing
    public ResponseEntity<ApiResponse<EquipmentResponseDto>> getEquipmentById(@PathVariable Long id) {
        EquipmentResponseDto equipment = equipmentManagementService.getEquipmentById(id);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Equipment fetched successfully", equipment));
    }

    // --- Get All Equipment managed by current Equipment Manager ---
    @GetMapping("/my-managed")
    @PreAuthorize("hasRole('EQUIPMENT_MANAGER')")
    public ResponseEntity<ApiResponse<List<EquipmentResponseDto>>> getMyManagedEquipment() {
        List<EquipmentResponseDto> equipmentList = equipmentManagementService.getMyManagedEquipment();
        return ResponseEntity.ok(ApiResponse.ofSuccess("Managed equipment fetched successfully", equipmentList));
    }

    // --- Get All Equipment (for Admin/Project Manager) ---
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")// Typically for higher-level roles
    public ResponseEntity<ApiResponse<List<EquipmentResponseDto>>> getAllEquipment() {
        List<EquipmentResponseDto> equipmentList = equipmentManagementService.getAllEquipment();
        return ResponseEntity.ok(ApiResponse.ofSuccess("All equipment fetched successfully", equipmentList));
    }
}