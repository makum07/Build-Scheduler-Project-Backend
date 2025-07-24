package com.buildscheduler.buildscheduler.service.impl;

import com.buildscheduler.buildscheduler.dto.equipment_manager.EquipmentAssignmentRequestDto;
import com.buildscheduler.buildscheduler.dto.equipment_manager.EquipmentAssignmentResponseDto;
import com.buildscheduler.buildscheduler.dto.equipment_manager.EquipmentNonAvailableSlotRequestDto;
import com.buildscheduler.buildscheduler.dto.equipment_manager.EquipmentNonAvailableSlotResponseDto;
import com.buildscheduler.buildscheduler.dto.equipment_manager.EquipmentResponseDto;
import com.buildscheduler.buildscheduler.dto.equipment_manager.SubtaskForEquipmentAssignmentDto;
import com.buildscheduler.buildscheduler.dto.worker.SimpleUserDto;
import com.buildscheduler.buildscheduler.exception.ConflictException;
import com.buildscheduler.buildscheduler.exception.ResourceNotFoundException;
import com.buildscheduler.buildscheduler.model.Equipment;
import com.buildscheduler.buildscheduler.model.EquipmentAssignment;
import com.buildscheduler.buildscheduler.model.EquipmentNonAvailableSlot;
import com.buildscheduler.buildscheduler.model.Subtask;
import com.buildscheduler.buildscheduler.model.User;
import com.buildscheduler.buildscheduler.repository.EquipmentAssignmentRepository;
import com.buildscheduler.buildscheduler.repository.EquipmentNonAvailableSlotRepository;
import com.buildscheduler.buildscheduler.repository.EquipmentRepository;
import com.buildscheduler.buildscheduler.repository.SubtaskRepository;
import com.buildscheduler.buildscheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
// Ensure Set is imported
import java.util.Set; // <--- ADD THIS IMPORT
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EquipmentAvailabilityService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentNonAvailableSlotRepository nonAvailableSlotRepository;
    private final EquipmentAssignmentRepository equipmentAssignmentRepository;
    private final SubtaskRepository subtaskRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new ResourceNotFoundException("User not authenticated");
        }
        return userRepository.findById(((User) authentication.getPrincipal()).getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", ((User) authentication.getPrincipal()).getId()));
    }

    private void checkEquipmentManagerAuthorization(Equipment equipment, User currentUser) {
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));
        if (!isAdmin && (equipment.getEquipmentManager() == null || !equipment.getEquipmentManager().equals(currentUser))) {
            throw new AccessDeniedException("You are not authorized to manage this equipment.");
        }
    }

    // --- Equipment Non-Available Slot CRUD ---

    @Transactional
    public EquipmentNonAvailableSlotResponseDto addNonAvailableSlot(Long equipmentId, EquipmentNonAvailableSlotRequestDto dto) {
        User currentUser = getCurrentUser();
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));
        checkEquipmentManagerAuthorization(equipment, currentUser);

        if (dto.getStartTime().isAfter(dto.getEndTime())) {
            throw new IllegalArgumentException("Start time cannot be after end time.");
        }

        EquipmentNonAvailableSlot newSlot = new EquipmentNonAvailableSlot();
        newSlot.setEquipment(equipment);
        newSlot.setStartTime(dto.getStartTime());
        newSlot.setEndTime(dto.getEndTime());
        newSlot.setType(dto.getType());
        newSlot.setNotes(dto.getNotes());

        // Ensure lazy collections are loaded for checks
        equipment.getNonAvailableSlots().size();
        equipment.getAssignments().size();
        if (!equipment.isAvailable(newSlot.getStartTime(), newSlot.getEndTime())) {
            throw new ConflictException("The equipment is not available for the specified non-availability slot due to existing assignments or other non-availability periods.");
        }

        equipment.getNonAvailableSlots().add(newSlot); // Add to entity's collection for cascade persist
        EquipmentNonAvailableSlot savedSlot = nonAvailableSlotRepository.save(newSlot);

        return mapNonAvailableSlotToDto(savedSlot);
    }

    @Transactional
    public EquipmentNonAvailableSlotResponseDto updateNonAvailableSlot(Long equipmentId, Long slotId, EquipmentNonAvailableSlotRequestDto dto) {
        User currentUser = getCurrentUser();
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));
        checkEquipmentManagerAuthorization(equipment, currentUser);

        EquipmentNonAvailableSlot existingSlot = nonAvailableSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Non-Available Slot", "id", slotId));

        if (!existingSlot.getEquipment().getId().equals(equipmentId)) {
            throw new ConflictException("Non-available slot " + slotId + " does not belong to equipment " + equipmentId);
        }

        if (dto.getStartTime().isAfter(dto.getEndTime())) {
            throw new IllegalArgumentException("Start time cannot be after end time.");
        }

        // Temporarily remove the existing slot for overlap check against itself
        equipment.getNonAvailableSlots().remove(existingSlot);
        equipment.getAssignments().size(); // Ensure assignments are loaded

        existingSlot.setStartTime(dto.getStartTime());
        existingSlot.setEndTime(dto.getEndTime());
        existingSlot.setType(dto.getType());
        existingSlot.setNotes(dto.getNotes());

        // Now check for overlaps with other existing slots/assignments *excluding* itself
        if (!equipment.isAvailable(existingSlot.getStartTime(), existingSlot.getEndTime())) {
            // Re-add the slot if there's a conflict before throwing
            equipment.getNonAvailableSlots().add(existingSlot);
            throw new ConflictException("The updated slot conflicts with existing assignments or other non-availability periods.");
        }
        equipment.getNonAvailableSlots().add(existingSlot); // Re-add updated slot

        EquipmentNonAvailableSlot updatedSlot = nonAvailableSlotRepository.save(existingSlot);
        return mapNonAvailableSlotToDto(updatedSlot);
    }

    @Transactional
    public void deleteNonAvailableSlot(Long equipmentId, Long slotId) {
        User currentUser = getCurrentUser();
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));
        checkEquipmentManagerAuthorization(equipment, currentUser);

        EquipmentNonAvailableSlot slotToDelete = nonAvailableSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Non-Available Slot", "id", slotId));

        if (!slotToDelete.getEquipment().getId().equals(equipmentId)) {
            throw new ConflictException("Non-available slot " + slotId + " does not belong to equipment " + equipmentId);
        }

        // Remove from the equipment's collection to trigger orphan removal if configured on Equipment entity
        equipment.getNonAvailableSlots().remove(slotToDelete);
        nonAvailableSlotRepository.delete(slotToDelete);
    }

    @Transactional(readOnly = true)
    public List<EquipmentNonAvailableSlotResponseDto> getNonAvailableSlotsForEquipment(Long equipmentId) {
        equipmentRepository.findById(equipmentId) // Check if equipment exists
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));

        return nonAvailableSlotRepository.findByEquipmentId(equipmentId).stream()
                .map(this::mapNonAvailableSlotToDto)
                .collect(Collectors.toList());
    }

    // --- Equipment Assignment ---

    @Transactional
    public EquipmentAssignmentResponseDto assignEquipmentToSubtask(Long equipmentId, EquipmentAssignmentRequestDto dto) {
        User currentUser = getCurrentUser();
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));
        checkEquipmentManagerAuthorization(equipment, currentUser); // Manager assigns their own equipment

        Subtask subtask = subtaskRepository.findById(dto.getSubtaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Subtask", "id", dto.getSubtaskId()));

        if (dto.getStartTime().isAfter(dto.getEndTime())) {
            throw new IllegalArgumentException("Start time cannot be after end time.");
        }

        // IMPORTANT: Check availability using the Equipment's `isAvailable` method
        // This method automatically checks both nonAvailableSlots and assignments
        equipment.getNonAvailableSlots().size(); // Ensure lazy collections are loaded for checks
        equipment.getAssignments().size();       // Ensure lazy collections are loaded for checks
        if (!equipment.isAvailable(dto.getStartTime(), dto.getEndTime())) {
            throw new ConflictException("Equipment " + equipment.getName() + " (ID: " + equipmentId + ") is not available during the requested period (" + dto.getStartTime() + " to " + dto.getEndTime() + "). It's either in use, under maintenance, or decommissioned.");
        }

        EquipmentAssignment newAssignment = new EquipmentAssignment();
        newAssignment.setEquipment(equipment);
        newAssignment.setSubtask(subtask);
        newAssignment.setAssignedBy(currentUser); // The equipment manager making the assignment
        newAssignment.setStartTime(dto.getStartTime());
        newAssignment.setEndTime(dto.getEndTime());
        newAssignment.setEquipmentNotes(dto.getEquipmentNotes());

        // The @PostPersist in EquipmentAssignment will automatically create the non-available slot.
        EquipmentAssignment savedAssignment = equipmentAssignmentRepository.save(newAssignment);

        return mapEquipmentAssignmentToDto(savedAssignment);
    }

    @Transactional
    public void deleteEquipmentAssignment(Long equipmentId, Long assignmentId) {
        User currentUser = getCurrentUser();

        // Fetch equipment eagerly to ensure collections are loaded for checks and manipulation
        // or ensure @EntityGraph on findById in repository loads them
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));
        checkEquipmentManagerAuthorization(equipment, currentUser);

        EquipmentAssignment assignmentToDelete = equipmentAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment Assignment", "id", assignmentId));

        if (!assignmentToDelete.getEquipment().getId().equals(equipmentId)) {
            throw new ConflictException("Equipment assignment " + assignmentId + " does not belong to equipment " + equipmentId);
        }

        // IMPORTANT: Remove the assignment from the equipment's collection
        // This is crucial when using orphanRemoval = true.
        // It tells Hibernate that this child is no longer associated with the parent and should be removed.
        // If the collection is lazy-loaded, ensure it's loaded before modification.
        equipment.getAssignments().remove(assignmentToDelete);

        // Manually find and delete the corresponding EquipmentNonAvailableSlot
        // This is still good as a direct deletion, but if you have a cascade/orphanRemoval on
        // the EquipmentNonAvailableSlot, removing it from the equipment.nonAvailableSlots
        // collection (after loading it) would also work.
        nonAvailableSlotRepository.findByEquipmentAndTypeAndStartTimeAndEndTime(
                assignmentToDelete.getEquipment(),
                EquipmentNonAvailableSlot.NonAvailabilityType.ASSIGNED,
                assignmentToDelete.getStartTime(),
                assignmentToDelete.getEndTime()
        ).ifPresent(nonAvailableSlot -> {
            // Also remove from the equipment's nonAvailableSlots collection
            equipment.getNonAvailableSlots().remove(nonAvailableSlot);
            nonAvailableSlotRepository.delete(nonAvailableSlot);
        });

        // Although removing from the collection with orphanRemoval=true should trigger delete,
        // explicitly calling delete on the repository ensures it.
        equipmentAssignmentRepository.delete(assignmentToDelete);
    }
    @Transactional(readOnly = true)
    public List<EquipmentAssignmentResponseDto> getAssignmentsForEquipment(Long equipmentId) {
        equipmentRepository.findById(equipmentId) // Check if equipment exists
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));

        return equipmentAssignmentRepository.findByEquipmentId(equipmentId).stream()
                .map(this::mapEquipmentAssignmentToDto)
                .collect(Collectors.toList());
    }

    // --- Get Full Equipment Details (with nested assignments and slots) ---
    @Transactional(readOnly = true)
    public EquipmentResponseDto getFullEquipmentDetails(Long equipmentId) {
        // @EntityGraph on findById in repository handles eager loading for nonAvailableSlots and assignments
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));

        // No need for .size() calls if @EntityGraph is properly configured on findById.
        // If not, uncomment these:
        // equipment.getNonAvailableSlots().size();
        // equipment.getAssignments().size();

        EquipmentResponseDto dto = mapEntityToDto(equipment); // This calculates dynamic status
        // Populate nested collections in the response DTO using the specific DTOs
        dto.setNonAvailableSlots(equipment.getNonAvailableSlots().stream()
                .map(this::mapNonAvailableSlotToDto)
                .collect(Collectors.toSet()));
        dto.setAssignments(equipment.getAssignments().stream()
                .map(this::mapEquipmentAssignmentToDto)
                .collect(Collectors.toSet()));
        return dto;
    }


    // --- Mappers ---

    private EquipmentNonAvailableSlotResponseDto mapNonAvailableSlotToDto(EquipmentNonAvailableSlot slot) {
        EquipmentNonAvailableSlotResponseDto dto = new EquipmentNonAvailableSlotResponseDto();
        dto.setId(slot.getId());
        dto.setEquipmentId(slot.getEquipment().getId());
        dto.setStartTime(slot.getStartTime());
        dto.setEndTime(slot.getEndTime());
        dto.setType(slot.getType());
        dto.setNotes(slot.getNotes());
        return dto;
    }

    private EquipmentAssignmentResponseDto mapEquipmentAssignmentToDto(EquipmentAssignment assignment) {
        EquipmentAssignmentResponseDto dto = new EquipmentAssignmentResponseDto();
        dto.setId(assignment.getId());
        dto.setEquipmentId(assignment.getEquipment().getId());
        dto.setStartTime(assignment.getStartTime());
        dto.setEndTime(assignment.getEndTime());
        dto.setEquipmentNotes(assignment.getEquipmentNotes());

        // Map Subtask details using the new DTO
        if (assignment.getSubtask() != null) {
            SubtaskForEquipmentAssignmentDto subtaskDto = new SubtaskForEquipmentAssignmentDto();
            subtaskDto.setId(assignment.getSubtask().getId());
            subtaskDto.setTitle(assignment.getSubtask().getTitle());
            subtaskDto.setDescription(assignment.getSubtask().getDescription()); // Use description
            subtaskDto.setPlannedStart(assignment.getSubtask().getPlannedStart()); // Use plannedStart
            subtaskDto.setPlannedEnd(assignment.getSubtask().getPlannedEnd());     // Use plannedEnd
            subtaskDto.setStatus(assignment.getSubtask().getStatus());             // Use status
            dto.setSubtask(subtaskDto);
        }

        // Map AssignedBy User details
        if (assignment.getAssignedBy() != null) {
            SimpleUserDto userDto = new SimpleUserDto();
            userDto.setId(assignment.getAssignedBy().getId());
            userDto.setUsername(assignment.getAssignedBy().getUsername());
            userDto.setEmail(assignment.getAssignedBy().getEmail());
            dto.setAssignedBy(userDto);
        }
        return dto;
    }

    // This method is correctly implemented as per previous instructions
    private EquipmentResponseDto mapEntityToDto(Equipment equipment) {
        EquipmentResponseDto dto = new EquipmentResponseDto();
        dto.setId(equipment.getId());
        dto.setName(equipment.getName());
        dto.setModel(equipment.getModel());
        dto.setSerialNumber(equipment.getSerialNumber());
        dto.setType(equipment.getType());

        dto.setCurrentOperationalStatus(calculateCurrentOperationalStatus(equipment));

        dto.setPurchasePrice(equipment.getPurchasePrice());
        dto.setWarrantyMonths(equipment.getWarrantyMonths());
        dto.setMaintenanceIntervalDays(equipment.getMaintenanceIntervalDays());
        dto.setLastMaintenanceDate(equipment.getLastMaintenanceDate());
        dto.setLocation(equipment.getLocation());
        dto.setNotes(equipment.getNotes());
        dto.setMaintenanceDue(equipment.isMaintenanceDue());

        if (equipment.getEquipmentManager() != null) {
            SimpleUserDto managerDto = new SimpleUserDto();
            managerDto.setId(equipment.getEquipmentManager().getId());
            managerDto.setUsername(equipment.getEquipmentManager().getUsername());
            managerDto.setEmail(equipment.getEquipmentManager().getEmail());
            dto.setEquipmentManager(managerDto);
        }
        return dto;
    }

    private Equipment.EquipmentStatus calculateCurrentOperationalStatus(Equipment equipment) {
        Equipment.EquipmentStatus baseStatus = equipment.getStatus();

        if (baseStatus == Equipment.EquipmentStatus.DECOMMISSIONED) {
            return Equipment.EquipmentStatus.DECOMMISSIONED;
        }

        LocalDateTime now = LocalDateTime.now();

        Set<EquipmentNonAvailableSlot> nonAvailableSlots = equipment.getNonAvailableSlots();
        if (nonAvailableSlots != null) {
            boolean inMaintenanceSlot = nonAvailableSlots.stream()
                    .anyMatch(slot -> slot.getType() == EquipmentNonAvailableSlot.NonAvailabilityType.MAINTENANCE &&
                            slot.overlapsWith(now, now));
            if (inMaintenanceSlot) {
                return Equipment.EquipmentStatus.UNDER_MAINTENANCE;
            }
        }

        Set<EquipmentAssignment> assignments = equipment.getAssignments();
        if (assignments != null) {
            boolean currentlyAssigned = assignments.stream()
                    .anyMatch(assignment -> assignment.overlapsWith(now, now));
            if (currentlyAssigned) {
                return Equipment.EquipmentStatus.IN_USE;
            }
        }

        return baseStatus;
    }
}