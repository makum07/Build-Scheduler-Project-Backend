package com.buildscheduler.buildscheduler.controller.equipment_manager;

import com.buildscheduler.buildscheduler.dto.equipment_manager.EquipmentAssignmentRequestDto;
import com.buildscheduler.buildscheduler.dto.equipment_manager.EquipmentAssignmentResponseDto;
import com.buildscheduler.buildscheduler.dto.equipment_manager.EquipmentNonAvailableSlotRequestDto;
import com.buildscheduler.buildscheduler.dto.equipment_manager.EquipmentNonAvailableSlotResponseDto;
import com.buildscheduler.buildscheduler.dto.equipment_manager.EquipmentResponseDto; // For the full details API
import com.buildscheduler.buildscheduler.response.ApiResponse;
import com.buildscheduler.buildscheduler.service.impl.EquipmentAvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set; // For full details DTO

@RestController
@RequestMapping("/api/equipment/{equipmentId}") // Base path includes equipment ID
@RequiredArgsConstructor
public class EquipmentAvailabilityController {

    private final EquipmentAvailabilityService equipmentAvailabilityService;

    // --- CRUD for Equipment Non-Available Slots ---

    @PostMapping("/non-available-slots")
    @PreAuthorize("hasRole('EQUIPMENT_MANAGER')")
    public ResponseEntity<ApiResponse<EquipmentNonAvailableSlotResponseDto>> addNonAvailableSlot(
            @PathVariable Long equipmentId,
            @Valid @RequestBody EquipmentNonAvailableSlotRequestDto dto) {
        EquipmentNonAvailableSlotResponseDto createdSlot = equipmentAvailabilityService.addNonAvailableSlot(equipmentId, dto);
        return new ResponseEntity<>(ApiResponse.ofSuccess("Non-available slot added successfully", createdSlot), HttpStatus.CREATED);
    }

    @PutMapping("/non-available-slots/{slotId}")
    @PreAuthorize("hasRole('EQUIPMENT_MANAGER')")
    public ResponseEntity<ApiResponse<EquipmentNonAvailableSlotResponseDto>> updateNonAvailableSlot(
            @PathVariable Long equipmentId,
            @PathVariable Long slotId,
            @Valid @RequestBody EquipmentNonAvailableSlotRequestDto dto) {
        EquipmentNonAvailableSlotResponseDto updatedSlot = equipmentAvailabilityService.updateNonAvailableSlot(equipmentId, slotId, dto);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Non-available slot updated successfully", updatedSlot));
    }

    @DeleteMapping("/non-available-slots/{slotId}")
    @PreAuthorize("hasRole('EQUIPMENT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteNonAvailableSlot(
            @PathVariable Long equipmentId,
            @PathVariable Long slotId) {
        equipmentAvailabilityService.deleteNonAvailableSlot(equipmentId, slotId);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Non-available slot deleted successfully", null));
    }

    @GetMapping("/non-available-slots")
    @PreAuthorize("hasAnyRole('EQUIPMENT_MANAGER', 'PROJECT_MANAGER', 'ADMIN')") // Broader access for viewing
    public ResponseEntity<ApiResponse<List<EquipmentNonAvailableSlotResponseDto>>> getNonAvailableSlotsForEquipment(
            @PathVariable Long equipmentId) {
        List<EquipmentNonAvailableSlotResponseDto> slots = equipmentAvailabilityService.getNonAvailableSlotsForEquipment(equipmentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Non-available slots fetched successfully", slots));
    }

    // --- Equipment Assignment to Subtask ---

    @PostMapping("/assignments")
    @PreAuthorize("hasRole('EQUIPMENT_MANAGER')")
    public ResponseEntity<ApiResponse<EquipmentAssignmentResponseDto>> assignEquipmentToSubtask(
            @PathVariable Long equipmentId,
            @Valid @RequestBody EquipmentAssignmentRequestDto dto) {
        EquipmentAssignmentResponseDto assignment = equipmentAvailabilityService.assignEquipmentToSubtask(equipmentId, dto);
        return new ResponseEntity<>(ApiResponse.ofSuccess("Equipment assigned to subtask successfully", assignment), HttpStatus.CREATED);
    }

    @DeleteMapping("/assignments/{assignmentId}")
    @PreAuthorize("hasRole('EQUIPMENT_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteEquipmentAssignment(
            @PathVariable Long equipmentId,
            @PathVariable Long assignmentId) {
        equipmentAvailabilityService.deleteEquipmentAssignment(equipmentId, assignmentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Equipment assignment deleted successfully", null));
    }

    @GetMapping("/assignments")
    @PreAuthorize("hasAnyRole('EQUIPMENT_MANAGER', 'PROJECT_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<EquipmentAssignmentResponseDto>>> getAssignmentsForEquipment(
            @PathVariable Long equipmentId) {
        List<EquipmentAssignmentResponseDto> assignments = equipmentAvailabilityService.getAssignmentsForEquipment(equipmentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Equipment assignments fetched successfully", assignments));
    }

    // --- Get Full Equipment Details ---
    @GetMapping("/details") // e.g., /api/equipment/{equipmentId}/details
    @PreAuthorize("hasAnyRole('EQUIPMENT_MANAGER', 'PROJECT_MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<EquipmentResponseDto>> getFullEquipmentDetails(
            @PathVariable Long equipmentId) {
        EquipmentResponseDto fullDetails = equipmentAvailabilityService.getFullEquipmentDetails(equipmentId);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Full equipment details fetched successfully", fullDetails));
    }
}