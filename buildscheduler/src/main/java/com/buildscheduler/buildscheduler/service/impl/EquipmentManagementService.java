package com.buildscheduler.buildscheduler.service.impl;

import com.buildscheduler.buildscheduler.dto.equipment_manager.EquipmentRequestDto;
import com.buildscheduler.buildscheduler.dto.equipment_manager.EquipmentResponseDto;
import com.buildscheduler.buildscheduler.dto.worker.SimpleUserDto;
import com.buildscheduler.buildscheduler.exception.ResourceNotFoundException;
import com.buildscheduler.buildscheduler.model.Equipment;
import com.buildscheduler.buildscheduler.model.EquipmentAssignment; // Import EquipmentAssignment
import com.buildscheduler.buildscheduler.model.EquipmentNonAvailableSlot; // Import EquipmentNonAvailableSlot
import com.buildscheduler.buildscheduler.model.Notification; // Import Notification (if using NotificationService)
import com.buildscheduler.buildscheduler.model.User;
import com.buildscheduler.buildscheduler.repository.EquipmentRepository;
import com.buildscheduler.buildscheduler.repository.UserRepository;
import com.buildscheduler.buildscheduler.service.NotificationService; // Import NotificationService (if using it here)
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime; // Import LocalDateTime
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set; // Import Set

@Service
@RequiredArgsConstructor
public class EquipmentManagementService {

    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;
    // If you plan to consolidate maintenance alerts here, uncomment the next line:
    // private final NotificationService notificationService;


    // Helper method to get the current authenticated user
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new ResourceNotFoundException("User not authenticated");
        }
        return userRepository.findById(((User) authentication.getPrincipal()).getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", ((User) authentication.getPrincipal()).getId()));
    }

    /**
     * Creates new equipment.
     * Only an EQUIPMENT_MANAGER can create equipment and it will be assigned to them.
     */
    @Transactional
    public EquipmentResponseDto createEquipment(EquipmentRequestDto dto) {
        User currentUser = getCurrentUser();

        Equipment equipment = new Equipment();
        mapDtoToEntity(dto, equipment);
        equipment.setEquipmentManager(currentUser); // Assign current user as manager
        equipment.setStatus(Equipment.EquipmentStatus.AVAILABLE); // Default base status for new equipment

        Equipment savedEquipment = equipmentRepository.save(equipment);
        // collections will be empty for newly created equipment, so dynamic status will be base status
        return mapEntityToDto(savedEquipment);
    }

    /**
     * Updates existing equipment.
     * Only the assigned Equipment Manager or an ADMIN can update equipment.
     */
    @Transactional
    public EquipmentResponseDto updateEquipment(Long equipmentId, EquipmentRequestDto dto) {
        User currentUser = getCurrentUser();
        Equipment existingEquipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));

        // Authorization check: Only the assigned manager or an admin can update
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        if (!isAdmin && (existingEquipment.getEquipmentManager() == null || !existingEquipment.getEquipmentManager().equals(currentUser))) {
            throw new AccessDeniedException("You are not authorized to update this equipment.");
        }

        mapDtoToEntity(dto, existingEquipment); // Update fields from DTO
        // Allow status update for the *base* status if provided in DTO
        if (dto.getStatus() != null) {
            existingEquipment.setStatus(dto.getStatus());
        }

        Equipment updatedEquipment = equipmentRepository.save(existingEquipment);
        // For update, ensure lazy collections are loaded before mapping
        // (if not using @EntityGraph on findById, this would be needed here)
        // updatedEquipment.getNonAvailableSlots().size();
        // updatedEquipment.getAssignments().size();
        return mapEntityToDto(updatedEquipment);
    }

    /**
     * Deletes equipment by ID.
     * Only the assigned Equipment Manager or an ADMIN can delete equipment.
     */
    @Transactional
    public void deleteEquipment(Long equipmentId) {
        User currentUser = getCurrentUser();
        Equipment equipmentToDelete = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));

        // Authorization check
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        if (!isAdmin && (equipmentToDelete.getEquipmentManager() == null || !equipmentToDelete.getEquipmentManager().equals(currentUser))) {
            throw new AccessDeniedException("You are not authorized to delete this equipment.");
        }

        equipmentRepository.delete(equipmentToDelete);
    }

    /**
     * Gets a single equipment by ID.
     * Accessible by EQUIPMENT_MANAGER (for their own), ADMIN, or PROJECT_MANAGER (for viewing).
     */
    @Transactional(readOnly = true)
    public EquipmentResponseDto getEquipmentById(Long equipmentId) {
        // @EntityGraph on findById in repository handles eager loading
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));

        return mapEntityToDto(equipment);
    }

    /**
     * Gets all equipment managed by the current user (Equipment Manager).
     */
    @Transactional(readOnly = true)
    public List<EquipmentResponseDto> getMyManagedEquipment() {
        User currentUser = getCurrentUser();
        // @EntityGraph on findByEquipmentManager in repository handles eager loading
        List<Equipment> equipment = equipmentRepository.findByEquipmentManager(currentUser);

        return equipment.stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    /**
     * Gets all equipment in the system.
     * Typically for ADMIN or potentially PROJECT_MANAGER.
     */
    @Transactional(readOnly = true)
    public List<EquipmentResponseDto> getAllEquipment() {
        // @EntityGraph on findAll in repository handles eager loading
        List<Equipment> equipment = equipmentRepository.findAll();
        return equipment.stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    // --- Helper methods for DTO <-> Entity mapping ---
    private void mapDtoToEntity(EquipmentRequestDto dto, Equipment equipment) {
        equipment.setName(dto.getName());
        equipment.setModel(dto.getModel());
        equipment.setSerialNumber(dto.getSerialNumber());
        equipment.setType(dto.getType());
        equipment.setPurchasePrice(dto.getPurchasePrice());
        equipment.setWarrantyMonths(dto.getWarrantyMonths());
        equipment.setMaintenanceIntervalDays(dto.getMaintenanceIntervalDays());
        equipment.setLastMaintenanceDate(dto.getLastMaintenanceDate()); // Can be null
        equipment.setLocation(dto.getLocation());
        equipment.setNotes(dto.getNotes());
    }

    private EquipmentResponseDto mapEntityToDto(Equipment equipment) {
        EquipmentResponseDto dto = new EquipmentResponseDto();
        dto.setId(equipment.getId());
        dto.setName(equipment.getName());
        dto.setModel(equipment.getModel());
        dto.setSerialNumber(equipment.getSerialNumber());
        dto.setType(equipment.getType());

        // --- DYNAMIC STATUS CALCULATION ---
        dto.setCurrentOperationalStatus(calculateCurrentOperationalStatus(equipment));
        // --- END DYNAMIC STATUS CALCULATION ---

        dto.setPurchasePrice(equipment.getPurchasePrice());
        dto.setWarrantyMonths(equipment.getWarrantyMonths());
        dto.setMaintenanceIntervalDays(equipment.getMaintenanceIntervalDays());
        dto.setLastMaintenanceDate(equipment.getLastMaintenanceDate());
        dto.setLocation(equipment.getLocation());
        dto.setNotes(equipment.getNotes());
        dto.setMaintenanceDue(equipment.isMaintenanceDue()); // Expose maintenance alert status as a separate flag

        if (equipment.getEquipmentManager() != null) {
            SimpleUserDto managerDto = new SimpleUserDto();
            managerDto.setId(equipment.getEquipmentManager().getId());
            managerDto.setUsername(equipment.getEquipmentManager().getUsername());
            managerDto.setEmail(equipment.getEquipmentManager().getEmail());
            dto.setEquipmentManager(managerDto);
        }
        return dto;
    }

    /**
     * Calculates the current operational status of the equipment dynamically.
     * Prioritizes statuses that indicate immediate unavailability or specific states.
     * Order of precedence:
     * 1. DECOMMISSIONED (Highest priority as it's a permanent state)
     * 2. UNDER_MAINTENANCE (If currently in a maintenance non-available slot)
     * 3. IN_USE (If currently assigned to a task)
     * 4. AVAILABLE (Base status, or if it's maintenance due but not actively in maintenance)
     */
    private Equipment.EquipmentStatus calculateCurrentOperationalStatus(Equipment equipment) {
        // Get the base status from the entity (e.g., set during creation or manual update)
        Equipment.EquipmentStatus baseStatus = equipment.getStatus();

        // 1. Check for DECOMMISSIONED status (Highest priority override)
        if (baseStatus == Equipment.EquipmentStatus.DECOMMISSIONED) {
            return Equipment.EquipmentStatus.DECOMMISSIONED;
        }

        LocalDateTime now = LocalDateTime.now(); // Current time

        // 2. Check for Maintenance Non-Availability Slots
        Set<EquipmentNonAvailableSlot> nonAvailableSlots = equipment.getNonAvailableSlots();
        if (nonAvailableSlots != null) { // Defensive check
            boolean inMaintenanceSlot = nonAvailableSlots.stream()
                    .anyMatch(slot -> slot.getType() == EquipmentNonAvailableSlot.NonAvailabilityType.MAINTENANCE &&
                            slot.overlapsWith(now, now)); // Check for overlap with current moment

            if (inMaintenanceSlot) {
                return Equipment.EquipmentStatus.UNDER_MAINTENANCE;
            }
        }


        // 3. Check for Active Assignments (IN_USE)
        Set<EquipmentAssignment> assignments = equipment.getAssignments();
        if (assignments != null) { // Defensive check
            boolean currentlyAssigned = assignments.stream()
                    .anyMatch(assignment -> assignment.overlapsWith(now, now)); // Check for overlap with current moment

            if (currentlyAssigned) {
                return Equipment.EquipmentStatus.IN_USE;
            }
        }

        // 4. Fallback to the base status
        // At this point, it's not decommissioned, not in a maintenance slot, and not currently assigned.
        // So, return its base status, which could be AVAILABLE or some other static status.
        return baseStatus;
    }
}