package com.buildscheduler.buildscheduler.service.impl;

import com.buildscheduler.buildscheduler.dto.equipment_manager.EquipmentRequestDto;
import com.buildscheduler.buildscheduler.dto.equipment_manager.EquipmentResponseDto;
import com.buildscheduler.buildscheduler.dto.worker.SimpleUserDto; // Ensure this DTO exists
import com.buildscheduler.buildscheduler.exception.ResourceNotFoundException;
import com.buildscheduler.buildscheduler.model.Equipment;
import com.buildscheduler.buildscheduler.model.User;
import com.buildscheduler.buildscheduler.repository.EquipmentRepository;
import com.buildscheduler.buildscheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor // Lombok annotation for constructor injection
public class EquipmentManagementService {

    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository; // To fetch User details if needed

    // Helper method to get the current authenticated user
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new ResourceNotFoundException("User not authenticated");
        }
        // It's good practice to re-fetch the user from the DB to ensure it's managed
        // and avoid LazyInitializationException if you access collections.
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
        // Ensure the current user is an EQUIPMENT_MANAGER if you have specific role checks here
        // (though @PreAuthorize in controller will handle primary check)
        // if (!currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_EQUIPMENT_MANAGER"))) {
        //     throw new AccessDeniedException("Only Equipment Managers can add equipment.");
        // }

        Equipment equipment = new Equipment();
        mapDtoToEntity(dto, equipment);
        equipment.setEquipmentManager(currentUser); // Assign current user as manager
        equipment.setStatus(Equipment.EquipmentStatus.AVAILABLE); // Default status for new equipment

        Equipment savedEquipment = equipmentRepository.save(equipment);
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
        // Allow status update if provided in DTO
        if (dto.getStatus() != null) {
            existingEquipment.setStatus(dto.getStatus());
        }

        Equipment updatedEquipment = equipmentRepository.save(existingEquipment);
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
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));

        // Consider implementing an authorization check here if not already handled by @PreAuthorize
        // e.g., if only equipment managers should see their own equipment, or project managers
        // only for projects they are associated with.
        return mapEntityToDto(equipment);
    }

    /**
     * Gets all equipment managed by the current user (Equipment Manager).
     */
    @Transactional(readOnly = true)
    public List<EquipmentResponseDto> getMyManagedEquipment() {
        User currentUser = getCurrentUser();
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
        // Status is handled separately as it might be null in request DTO
    }

    private EquipmentResponseDto mapEntityToDto(Equipment equipment) {
        EquipmentResponseDto dto = new EquipmentResponseDto();
        dto.setId(equipment.getId());
        dto.setName(equipment.getName());
        dto.setModel(equipment.getModel());
        dto.setSerialNumber(equipment.getSerialNumber());
        dto.setType(equipment.getType());
        dto.setStatus(equipment.getStatus());
        dto.setPurchasePrice(equipment.getPurchasePrice());
        dto.setWarrantyMonths(equipment.getWarrantyMonths());
        dto.setMaintenanceIntervalDays(equipment.getMaintenanceIntervalDays());
        dto.setLastMaintenanceDate(equipment.getLastMaintenanceDate());
        dto.setLocation(equipment.getLocation());
        dto.setNotes(equipment.getNotes());

        if (equipment.getEquipmentManager() != null) {
            SimpleUserDto managerDto = new SimpleUserDto();
            managerDto.setId(equipment.getEquipmentManager().getId());
            managerDto.setUsername(equipment.getEquipmentManager().getUsername());
            managerDto.setEmail(equipment.getEquipmentManager().getEmail());
            dto.setEquipmentManager(managerDto);
        }
        return dto;
    }
}