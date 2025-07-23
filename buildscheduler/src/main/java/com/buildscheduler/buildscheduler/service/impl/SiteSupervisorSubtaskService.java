package com.buildscheduler.buildscheduler.service.impl;

import com.buildscheduler.buildscheduler.dto.site_supervisor.SubtaskRequestDto;
import com.buildscheduler.buildscheduler.dto.site_supervisor.SubtaskResponseDto;
import com.buildscheduler.buildscheduler.dto.site_supervisor.SubtaskStatusUpdateDto; // Import the new DTO
import com.buildscheduler.buildscheduler.exception.ResourceNotFoundException;
import com.buildscheduler.buildscheduler.model.*;
import com.buildscheduler.buildscheduler.repository.*;
import com.buildscheduler.buildscheduler.service.NotificationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SiteSupervisorSubtaskService {

    private final UserRepository userRepository;
    private final MainTaskRepository mainTaskRepository;
    private final SubtaskRepository subtaskRepository;
    private final SkillRepository skillRepository;
    private final NotificationService notificationService;
    private final EquipmentRepository equipmentRepository;

    public SiteSupervisorSubtaskService(
            UserRepository userRepository,
            MainTaskRepository mainTaskRepository,
            SubtaskRepository subtaskRepository,
            SkillRepository skillRepository,
            NotificationService notificationService,
            EquipmentRepository equipmentRepository
    ) {
        this.userRepository = userRepository;
        this.mainTaskRepository = mainTaskRepository;
        this.subtaskRepository = subtaskRepository;
        this.skillRepository = skillRepository;
        this.notificationService = notificationService;
        this.equipmentRepository = equipmentRepository;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User)) {
            throw new ResourceNotFoundException("User not authenticated");
        }
        return (User) auth.getPrincipal();
    }

    //------------------------------------------------------------------------------------------------------------------

    @Transactional
    public SubtaskResponseDto createSubtask(SubtaskRequestDto dto, Long mainTaskId) {
        User currentUser = getCurrentUser();
        MainTask mainTask = mainTaskRepository.findById(mainTaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Main task not found"));

        if (!mainTask.getProject().getSiteSupervisor().equals(currentUser)) {
            throw new AccessDeniedException("Not authorized for this project");
        }

        Subtask subtask = new Subtask();
        subtask.setTitle(dto.getTitle());
        subtask.setDescription(dto.getDescription());
        subtask.setPlannedStart(dto.getPlannedStart());
        subtask.setPlannedEnd(dto.getPlannedEnd());
        subtask.setEstimatedHours(dto.getEstimatedHours());
        subtask.setRequiredWorkers(dto.getRequiredWorkers());
        subtask.setPriority(dto.getPriority());
        subtask.setStatus(Subtask.TaskStatus.PLANNED);
        subtask.setEquipmentRequestNotes(dto.getEquipmentRequestNotes());
        subtask.setMainTask(mainTask);
        subtask.setProject(mainTask.getProject());

        // Always work with a new mutable set when setting for a new entity
        subtask.setRequiredSkills(new HashSet<>(lookupSkills(dto.getRequiredSkills())));

        Set<Equipment> equipmentNeeds = new HashSet<>();
        if (dto.getEquipmentIds() != null) {
            for (Long equipmentId : dto.getEquipmentIds()) {
                Equipment equipment = equipmentRepository.findById(equipmentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Equipment not found: " + equipmentId));
                equipmentNeeds.add(equipment);
            }
        }
        // Always work with a new mutable set when setting for a new entity
        subtask.setEquipmentNeeds(new HashSet<>(equipmentNeeds));

        Subtask savedSubtask = subtaskRepository.save(subtask);
        notifyEquipmentManager(savedSubtask);
        return convertToDto(savedSubtask);
    }

    //------------------------------------------------------------------------------------------------------------------

    @Transactional
    public SubtaskResponseDto updateSubtask(Long subtaskId, SubtaskRequestDto dto) {
        User currentUser = getCurrentUser();
        Subtask existingSubtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtask not found"));

        if (!existingSubtask.getProject().getSiteSupervisor().equals(currentUser)) {
            throw new AccessDeniedException("Not authorized for this subtask");
        }

        // --- Defensive collection handling for ManyToMany relationships ---

        // 1. Capture current state for notification *before* any modification to the entity's collections.
        //    Ensure these are new, independent sets to avoid direct interaction with Hibernate's proxy
        //    during this capture. This also initializes the lazy collections.
        Set<Long> oldEquipmentIds = existingSubtask.getEquipmentNeeds().stream()
                .map(Equipment::getId)
                .collect(Collectors.toCollection(HashSet::new)); // Use toCollection for mutable set

        Set<String> oldSkillNames = existingSubtask.getRequiredSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.toCollection(HashSet::new)); // Use toCollection for mutable set

        // 2. Update basic fields first
        existingSubtask.setTitle(dto.getTitle());
        existingSubtask.setDescription(dto.getDescription());
        existingSubtask.setPlannedStart(dto.getPlannedStart());
        existingSubtask.setPlannedEnd(dto.getPlannedEnd());
        existingSubtask.setEstimatedHours(dto.getEstimatedHours());
        existingSubtask.setRequiredWorkers(dto.getRequiredWorkers());
        existingSubtask.setPriority(dto.getPriority());
        existingSubtask.setEquipmentRequestNotes(dto.getEquipmentRequestNotes());
        existingSubtask.setStatus(Subtask.TaskStatus.PLANNED); // Assuming status resets to PLANNED on update

        // 3. Prepare the NEW collections as completely separate, mutable HashSets
        //    This ensures we are not modifying Hibernate's internal proxy
        Set<Skill> newRequiredSkills = new HashSet<>(lookupSkills(dto.getRequiredSkills()));
        Set<Equipment> newEquipmentNeeds = new HashSet<>();
        if (dto.getEquipmentIds() != null) {
            for (Long equipmentId : dto.getEquipmentIds()) {
                Equipment equipment = equipmentRepository.findById(equipmentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Equipment not found: " + equipmentId));
                newEquipmentNeeds.add(equipment);
            }
        }

        // 4. Set the new collections on the entity. Hibernate will now detect the change
        //    from the old proxy to these new independent sets and manage the join table updates.
        existingSubtask.setRequiredSkills(newRequiredSkills);
        existingSubtask.setEquipmentNeeds(newEquipmentNeeds);

        // 5. Save the updated subtask. This is where Hibernate flushes the changes.
        Subtask savedSubtask = subtaskRepository.save(existingSubtask);

        // --- Notification logic using the old vs new states ---
        // Determine added and removed equipment for notifications from the *saved* state
        Set<Long> currentEquipmentIds = savedSubtask.getEquipmentNeeds().stream()
                .map(Equipment::getId)
                .collect(Collectors.toSet());

        Set<Long> trulyRemovedEquipmentIds = new HashSet<>(oldEquipmentIds);
        trulyRemovedEquipmentIds.removeAll(currentEquipmentIds);

        Set<Long> newlyAddedEquipmentIds = new HashSet<>(currentEquipmentIds);
        newlyAddedEquipmentIds.removeAll(oldEquipmentIds);

        if (!trulyRemovedEquipmentIds.isEmpty()) {
            notifyEquipmentRemoval(savedSubtask, trulyRemovedEquipmentIds);
        }
        if (!newlyAddedEquipmentIds.isEmpty()) {
            notifyEquipmentManager(savedSubtask); // Notify for overall new/updated request
        }

        return convertToDto(savedSubtask);
    }

    //------------------------------------------------------------------------------------------------------------------

    @Transactional
    public void deleteSubtask(Long subtaskId) {
        User currentUser = getCurrentUser();
        Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtask not found"));

        if (!subtask.getProject().getSiteSupervisor().equals(currentUser)) {
            throw new AccessDeniedException("Not authorized for this subtask");
        }

        // Ensure equipment needs are loaded before deletion for notification purposes.
        // This implicitly initializes the lazy collection.
        Set<Equipment> equipmentNeedsBeforeDeletion = new HashSet<>(subtask.getEquipmentNeeds());

        if (!equipmentNeedsBeforeDeletion.isEmpty()) {
            notifyEquipmentManagerOfDeletion(subtask);
        }

        subtaskRepository.delete(subtask);
    }

    //------------------------------------------------------------------------------------------------------------------
    // New method to update subtask status
    @Transactional
    public SubtaskResponseDto updateSubtaskStatus(Long subtaskId, SubtaskStatusUpdateDto dto) {
        User currentUser = getCurrentUser();
        Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtask not found with ID: " + subtaskId));

        if (!subtask.getProject().getSiteSupervisor().equals(currentUser)) {
            throw new AccessDeniedException("Not authorized to update this subtask's status");
        }

        try {
            Subtask.TaskStatus newStatus = Subtask.TaskStatus.valueOf(dto.getStatus().toUpperCase());
            subtask.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid subtask status: " + dto.getStatus());
        }

        Subtask updatedSubtask = subtaskRepository.save(subtask);
        return convertToDto(updatedSubtask);
    }

    //------------------------------------------------------------------------------------------------------------------

    @Transactional
    public SubtaskResponseDto removeSkillFromSubtask(Long subtaskId, String skillName) {
        User currentUser = getCurrentUser();
        Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtask not found"));

        if (!subtask.getProject().getSiteSupervisor().equals(currentUser)) {
            throw new AccessDeniedException("Not authorized for this subtask");
        }

        Skill skillToRemove = skillRepository.findByNameIgnoreCase(skillName)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));

        // Create a new mutable set from the current skills, remove the specific skill, then set it back.
        // This avoids modifying Hibernate's internal proxy directly during iteration/reconciliation.
        Set<Skill> currentSkills = new HashSet<>(subtask.getRequiredSkills());
        boolean removed = currentSkills.remove(skillToRemove);
        subtask.setRequiredSkills(currentSkills);

        Subtask updatedSubtask = subtaskRepository.save(subtask);

        return convertToDto(updatedSubtask);
    }

    //------------------------------------------------------------------------------------------------------------------

    @Transactional
    public SubtaskResponseDto removeEquipmentFromSubtask(Long subtaskId, Long equipmentId) {
        User currentUser = getCurrentUser();
        Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtask not found"));

        if (!subtask.getProject().getSiteSupervisor().equals(currentUser)) {
            throw new AccessDeniedException("Not authorized for this subtask");
        }

        Equipment equipmentToRemove = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment not found"));

        // Create a new mutable set from the current equipment needs, remove the specific equipment, then set it back.
        // This avoids modifying Hibernate's internal proxy directly during iteration/reconciliation.
        Set<Equipment> currentEquipmentNeeds = new HashSet<>(subtask.getEquipmentNeeds());
        boolean removed = currentEquipmentNeeds.remove(equipmentToRemove);
        subtask.setEquipmentNeeds(currentEquipmentNeeds);

        Subtask updatedSubtask = subtaskRepository.save(subtask);

        if (removed) { // Only notify if the equipment was actually removed
            notifyEquipmentRemoval(updatedSubtask, Set.of(equipmentId));
        }

        return convertToDto(updatedSubtask);
    }

    //------------------------------------------------------------------------------------------------------------------

    private Set<Skill> lookupSkills(Set<String> names) {
        if (names == null || names.isEmpty()) return Set.of();

        return names.stream()
                .map(String::toLowerCase)
                .distinct()
                .map(name -> skillRepository.findByNameIgnoreCase(name)
                        .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + name)))
                .collect(Collectors.toSet());
    }

    private void notifyEquipmentManager(Subtask subtask) {
        User manager = subtask.getProject().getEquipmentManager();
        if (manager != null) {
            String message = String.format(
                    "New or updated equipment needs requested for subtask: %s (Project: %s). Current equipment IDs: %s",
                    subtask.getTitle(),
                    subtask.getProject().getTitle(),
                    subtask.getEquipmentNeeds().stream().map(Equipment::getId).collect(Collectors.toSet())
            );
            notificationService.createNotification(
                    manager.getId(),
                    message,
                    Notification.NotificationType.EQUIPMENT_REQUEST
            );
        }
    }

    private void notifyEquipmentRemoval(Subtask subtask, Set<Long> removedEquipmentIds) {
        User manager = subtask.getProject().getEquipmentManager();
        if (manager != null) {
            String message = String.format(
                    "Equipment removed from subtask: %s (Project: %s). Removed IDs: %s",
                    subtask.getTitle(),
                    subtask.getProject().getTitle(),
                    removedEquipmentIds
            );
            notificationService.createNotification(
                    manager.getId(),
                    message,
                    Notification.NotificationType.EQUIPMENT_REMOVAL
            );
        }
    }

    private void notifyEquipmentManagerOfDeletion(Subtask subtask) {
        User manager = subtask.getProject().getEquipmentManager();
        if (manager != null) {
            String message = String.format(
                    "Subtask with equipment needs deleted: %s (Project: %s)",
                    subtask.getTitle(),
                    subtask.getProject().getTitle()
            );
            notificationService.createNotification(
                    manager.getId(),
                    message,
                    Notification.NotificationType.EQUIPMENT_REQUEST_CANCELLED
            );
        }
    }

    private SubtaskResponseDto convertToDto(Subtask s) {
        SubtaskResponseDto dto = new SubtaskResponseDto();
        dto.setId(s.getId());
        dto.setTitle(s.getTitle());
        dto.setDescription(s.getDescription());
        dto.setPlannedStart(s.getPlannedStart());
        dto.setPlannedEnd(s.getPlannedEnd());
        dto.setEstimatedHours(s.getEstimatedHours());
        dto.setRequiredWorkers(s.getRequiredWorkers());
        dto.setPriority(s.getPriority());
        dto.setStatus(s.getStatus().name());
        dto.setMainTaskId(s.getMainTask().getId());
        dto.setProjectId(s.getProject().getId());
        dto.setEquipmentRequestNotes(s.getEquipmentRequestNotes());

        dto.setRequiredSkills(
                s.getRequiredSkills() == null ? Set.of() :
                        s.getRequiredSkills().stream()
                                .map(Skill::getName)
                                .collect(Collectors.toSet())
        );

        dto.setEquipmentIds(
                s.getEquipmentNeeds() == null ? Set.of() :
                        s.getEquipmentNeeds().stream()
                                .map(Equipment::getId)
                                .collect(Collectors.toSet())
        );

        return dto;
    }
}