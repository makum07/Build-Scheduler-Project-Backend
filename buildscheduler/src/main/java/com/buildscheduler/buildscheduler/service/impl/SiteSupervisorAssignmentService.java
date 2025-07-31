package com.buildscheduler.buildscheduler.service.impl;

import com.buildscheduler.buildscheduler.dto.site_supervisor.AssignmentRequestDto;
import com.buildscheduler.buildscheduler.dto.site_supervisor.WorkerSearchResultDto;
import com.buildscheduler.buildscheduler.exception.ConflictException;
import com.buildscheduler.buildscheduler.exception.ResourceNotFoundException;
import com.buildscheduler.buildscheduler.model.*;
import com.buildscheduler.buildscheduler.repository.*;
import com.buildscheduler.buildscheduler.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SiteSupervisorAssignmentService {

    private final UserRepository userRepository;
    private final SubtaskRepository subtaskRepository;
    private final WorkerAssignmentRepository workerAssignmentRepository;
    private final WorkerAvailabilitySlotRepository workerAvailabilitySlotRepository;
    private final NotificationService notificationService;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public List<WorkerSearchResultDto> findBestMatchedWorkers(Long subtaskId) {
        Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtask not found with ID: " + subtaskId));

        Set<String> requiredSkills = subtask.getRequiredSkills().stream()
                .map(skill -> skill.getName().toLowerCase())
                .collect(Collectors.toSet());

        LocalDateTime plannedStart = subtask.getPlannedStart();
        LocalDateTime plannedEnd = subtask.getPlannedEnd();

        List<User> allWorkers = userRepository.findByRoles_Name("ROLE_WORKER");

        System.out.println("Total workers fetched with 'WORKER' role: " + allWorkers.size());

        List<WorkerSearchResultDto> matchedWorkers = allWorkers.stream()
                .filter(worker -> {
                    // Check skills
                    boolean hasSkills = worker.getSkills().stream()
                            .anyMatch(skill -> requiredSkills.contains(skill.getName().toLowerCase()));
                    System.out.println("Worker " + worker.getUsername() + " (ID: " + worker.getId() + ") - Has required skills (" + requiredSkills + "): " + hasSkills + " (Worker skills: " + worker.getSkills().stream().map(s -> s.getName()).collect(Collectors.joining(", ")) + ")");
                    if (!hasSkills) {
                        return false; // No skills, filter out early
                    }

                    // *** FETCH ASSIGNMENTS EXPLICITLY FOR AVAILABILITY CHECK ***
                    Set<WorkerAssignment> currentWorkerAssignments = workerAssignmentRepository.findByWorker(worker);
                    worker.setWorkerAssignments(currentWorkerAssignments); // Temporarily set for the check

                    // Check availability using the updated method
                    boolean available = worker.isAvailable(plannedStart, plannedEnd);
                    System.out.println("Worker " + worker.getUsername() + " (ID: " + worker.getId() + ") - Is available for " + plannedStart + " to " + plannedEnd + ": " + available);
                    return available;
                })
                .map(this::mapUserToWorkerSearchResultDto)
                .collect(Collectors.toList());
        return matchedWorkers;
    }

    private WorkerSearchResultDto mapUserToWorkerSearchResultDto(User user) {
        WorkerSearchResultDto dto = new WorkerSearchResultDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setSkills(user.getSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.toSet()));
        return dto;
    }

    @Transactional
    public void assignWorkerToSubtask(Long subtaskId, AssignmentRequestDto requestDto) {
        Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtask not found with ID: " + subtaskId));

        User worker = userRepository.findById(requestDto.getWorkerId())
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found with ID: " + requestDto.getWorkerId()));

        User assignedBy = getCurrentUser(); // Assuming this method exists and works
        if (assignedBy.getRoles().stream().noneMatch(role -> role.getName().equals("ROLE_SITE_SUPERVISOR"))) {
            throw new IllegalArgumentException("Authenticated user is not a Site Supervisor and cannot assign workers.");
        }

        LocalDateTime assignmentStart = requestDto.getAssignmentStart() != null ? requestDto.getAssignmentStart() : subtask.getPlannedStart();
        LocalDateTime assignmentEnd = requestDto.getAssignmentEnd() != null ? requestDto.getAssignmentEnd() : subtask.getPlannedEnd();

        if (worker.getRoles().stream().noneMatch(role -> role.getName().equals("ROLE_WORKER"))) {
            throw new IllegalArgumentException("Assigned user is not a WORKER.");
        }

        // *** FETCH ASSIGNMENTS EXPLICITLY FOR AVAILABILITY CHECK ***
        // Ensure workerAssignments are loaded before calling isAvailable
        Set<WorkerAssignment> currentWorkerAssignments = workerAssignmentRepository.findByWorker(worker);
        worker.setWorkerAssignments(currentWorkerAssignments);

        if (!worker.isAvailable(assignmentStart, assignmentEnd)) {
            throw new ConflictException("Worker is not available for the specified time slot due to existing assignments or lack of availability.");
        }

        WorkerAssignment workerAssignment = new WorkerAssignment();
        workerAssignment.setWorker(worker);
        workerAssignment.setSubtask(subtask);
        workerAssignment.setAssignmentStart(assignmentStart);
        workerAssignment.setAssignmentEnd(assignmentEnd);
        workerAssignment.setAssignedBy(assignedBy);
        workerAssignment.setWorkerNotes(requestDto.getWorkerNotes());

        workerAssignmentRepository.save(workerAssignment);

        // --- NEW LOGIC TO UPDATE THE PROJECT'S WORKER LIST ---
        Project project = subtask.getMainTask().getProject();

        // This check prevents duplicate entries if the worker is already assigned to another subtask
        // within the same project.
        if (project.getWorkers().stream().noneMatch(pWorker -> pWorker.getId().equals(worker.getId()))) {
            project.getWorkers().add(worker);
            projectRepository.save(project); // Persist the change to the project and its join table
            System.out.println("Worker " + worker.getUsername() + " added to project " + project.getTitle() + " workers list.");
        }
        // --- END OF NEW LOGIC ---

        // --- Start of Availability Update Logic ---
        System.out.println("\n--- Starting Availability Update for Assignment ---");
        System.out.println("Worker: " + worker.getUsername() + " (ID: " + worker.getId() + ")");
        System.out.println("Assignment Time: " + assignmentStart + " to " + assignmentEnd);

        updateAvailabilityForAssignment(worker, assignmentStart, assignmentEnd);
        System.out.println("--- Finished Availability Update for Assignment ---\n");
        // --- End of Availability Update Logic ---

        String notificationMessage = String.format("You have been assigned to subtask '%s' (ID: %d) from %s to %s.",
                subtask.getTitle(), subtask.getId(),
                assignmentStart.format(DateTimeFormatter.ofPattern("MMM dd HH:mm")),
                assignmentEnd.format(DateTimeFormatter.ofPattern("MMM dd HH:mm")));
        notificationService.createNotification(worker.getId(), notificationMessage, Notification.NotificationType.ASSIGNMENT);
    }

    @Transactional
    public void removeWorkerAssignment(Long assignmentId) {
        WorkerAssignment assignment = workerAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker assignment not found with ID: " + assignmentId));

        User removerUser = getCurrentUser();
        if (removerUser.getRoles().stream().noneMatch(role -> role.getName().equals("ROLE_SITE_SUPERVISOR"))) {
            throw new IllegalArgumentException("Authenticated user is not a Site Supervisor and cannot remove assignments.");
        }

        // Re-fetch worker to ensure its collection is loaded within the current transaction context
        User worker = userRepository.findById(assignment.getWorker().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found for assignment ID: " + assignmentId));

        LocalDateTime assignmentStart = assignment.getAssignmentStart();
        LocalDateTime assignmentEnd = assignment.getAssignmentEnd();
        Subtask subtask = assignment.getSubtask();

        workerAssignmentRepository.delete(assignment);

        // --- Start of Availability Reconstruction Logic ---
        System.out.println("\n--- Starting Availability Reconstruction for Assignment Removal ---");
        System.out.println("Worker: " + worker.getUsername() + " (ID: " + worker.getId() + ")");
        System.out.println("Removed Assignment Time: " + assignmentStart + " to " + assignmentEnd);
        reconstructAvailabilityAfterRemoval(worker, assignmentStart, assignmentEnd);
        System.out.println("--- Finished Availability Reconstruction for Assignment Removal ---\n");
        // --- End of Availability Reconstruction Logic ---

        String notificationMessage = String.format("Your assignment to subtask '%s' (ID: %d) from %s to %s has been removed.",
                subtask.getTitle(), subtask.getId(),
                assignmentStart.format(DateTimeFormatter.ofPattern("MMM dd HH:mm")),
                assignmentEnd.format(DateTimeFormatter.ofPattern("MMM dd HH:mm")));
        notificationService.createNotification(worker.getId(), notificationMessage, Notification.NotificationType.ASSIGNMENT);
    }

    private void updateAvailabilityForAssignment(User worker, LocalDateTime assignmentStart, LocalDateTime assignmentEnd) {
        final LocalDate assignmentDate = assignmentStart.toLocalDate();
        final LocalTime assignmentTimeStart = assignmentStart.toLocalTime();
        final LocalTime assignmentTimeEnd = assignmentEnd.toLocalTime();

        // **IMPORTANT:** Create a temporary list of slots that will be modified or removed.
        // We'll iterate over this list, and then update the actual worker's collection.
        // Using a new list prevents ConcurrentModificationException and allows precise control.
        List<WorkerAvailabilitySlot> slotsOnDateCopy = worker.getWorkerAvailabilitySlots().stream()
                .filter(slot -> slot.getDate().equals(assignmentDate))
                .collect(Collectors.toList());

        System.out.println("  Initial managed availability slots for date " + assignmentDate + " for worker " + worker.getId() + ":");
        slotsOnDateCopy.forEach(s -> System.out.println("    Managed slot: ID=" + s.getId() + ", " + s.getStartTime() + " to " + s.getEndTime()));

        List<WorkerAvailabilitySlot> slotsToDeleteFromDb = new ArrayList<>(); // Store slots that need explicit DB deletion
        Set<WorkerAvailabilitySlot> newSlotsForUserCollection = new HashSet<>(); // Use a Set to avoid duplicates if any logic error (though shouldn't happen)

        // First, iterate and determine which slots are affected and what new slots should be created
        for (WorkerAvailabilitySlot slot : slotsOnDateCopy) {
            final LocalTime slotStart = slot.getStartTime();
            final LocalTime slotEnd = slot.getEndTime();

            System.out.println("  Analyzing existing slot (ID: " + slot.getId() + "): " + slotStart + " to " + slotEnd);

            boolean overlapsWithAssignment = slot.overlaps(assignmentTimeStart, assignmentTimeEnd);
            System.out.println("    Does it overlap with assignment (" + assignmentTimeStart + " - " + assignmentTimeEnd + ")? " + overlapsWithAssignment);

            if (!overlapsWithAssignment) {
                // No overlap, this slot remains untouched. Add it to the new collection.
                System.out.println("    No overlap. Slot " + slot.getId() + " remains as is in the collection.");
                newSlotsForUserCollection.add(slot);
                continue;
            }

            // If there's an overlap, this original slot will be deleted from the DB
            slotsToDeleteFromDb.add(slot);
            System.out.println("    Slot " + slot.getId() + " marked for explicit database deletion.");

            // Case 1: Part of the slot before the assignment remains
            if (assignmentTimeStart.isAfter(slotStart)) {
                WorkerAvailabilitySlot newSlot = new WorkerAvailabilitySlot();
                newSlot.setUser(worker);
                newSlot.setDate(assignmentDate);
                newSlot.setStartTime(slotStart);
                newSlot.setEndTime(assignmentTimeStart);
                newSlotsForUserCollection.add(newSlot);
                System.out.println("    Created new slot (pre-assignment): " + newSlot.getStartTime() + " to " + newSlot.getEndTime());
            } else if (assignmentTimeStart.equals(slotStart)) {
                System.out.println("    Assignment starts at the beginning of the availability slot. No pre-assignment split needed.");
            }

            // Case 2: Part of the slot after the assignment remains
            if (assignmentTimeEnd.isBefore(slotEnd)) {
                WorkerAvailabilitySlot newSlot = new WorkerAvailabilitySlot();
                newSlot.setUser(worker);
                newSlot.setDate(assignmentDate);
                newSlot.setStartTime(assignmentTimeEnd);
                newSlot.setEndTime(slotEnd);
                newSlotsForUserCollection.add(newSlot);
                System.out.println("    Created new slot (post-assignment): " + newSlot.getStartTime() + " to " + newSlot.getEndTime());
            } else if (assignmentTimeEnd.equals(slotEnd)) {
                System.out.println("    Assignment ends at the end of the availability slot. No post-assignment split needed.");
            }
        }

        // Now, perform the actual updates to the managed collection and database
        // 1. Remove all old slots from the worker's managed collection for the specific date.
        // This is safe because `slotsOnDateCopy` allowed us to iterate without ConcurrentModificationException.
        // It signals to Hibernate to delete these from DB if `orphanRemoval=true`.
        worker.getWorkerAvailabilitySlots().removeIf(slot -> slot.getDate().equals(assignmentDate));
        System.out.println("  Cleared all old slots for " + assignmentDate + " from worker's in-memory collection.");


        // 2. Add the newly calculated and unaffected slots to the worker's managed collection.
        // Hibernate will handle inserting new ones if `CascadeType.ALL` is set.
        worker.getWorkerAvailabilitySlots().addAll(newSlotsForUserCollection);
        System.out.println("  Added " + newSlotsForUserCollection.size() + " new/unaffected slots to worker's in-memory collection.");

        // 3. Explicitly delete the identified old slots from the database.
        // This acts as a safeguard even if orphanRemoval isn't working as expected or if the entities
        // aren't perfectly detached/managed as intended by `removeAll`.
        if (!slotsToDeleteFromDb.isEmpty()) {
            workerAvailabilitySlotRepository.deleteAllInBatch(slotsToDeleteFromDb);
            System.out.println("  Explicitly deleted " + slotsToDeleteFromDb.size() + " old availability slots from DB.");
        }

        // 4. Explicitly save the newly created slots.
        // While `addAll` to a cascaded collection usually handles this, an explicit `saveAll` ensures it.
        List<WorkerAvailabilitySlot> slotsToSaveExplicitly = newSlotsForUserCollection.stream()
                .filter(slot -> slot.getId() == null) // Only save new ones (those without an ID)
                .collect(Collectors.toList());
        if (!slotsToSaveExplicitly.isEmpty()) {
            workerAvailabilitySlotRepository.saveAll(slotsToSaveExplicitly);
            System.out.println("  Explicitly saved " + slotsToSaveExplicitly.size() + " newly created availability slots to DB.");
        }
    }

    private void reconstructAvailabilityAfterRemoval(User worker, LocalDateTime removedStart, LocalDateTime removedEnd) {
        final LocalDate removedDate = removedStart.toLocalDate();
        final LocalTime removedStartTime = removedStart.toLocalTime();
        final LocalTime removedEndTime = removedEnd.toLocalTime();

        System.out.println("  Reconstructing availability for date: " + removedDate);
        System.out.println("  Freed segment: " + removedStartTime + " to " + removedEndTime);

        // **IMPORTANT:** Get a temporary list of current slots for the specific date from the managed collection.
        List<WorkerAvailabilitySlot> currentManagedSlotsForDateCopy = worker.getWorkerAvailabilitySlots().stream()
                .filter(slot -> slot.getDate().equals(removedDate))
                .collect(Collectors.toList());

        System.out.println("  Found " + currentManagedSlotsForDateCopy.size() + " current managed slots before merging for removal reconstruction.");
        currentManagedSlotsForDateCopy.forEach(s -> System.out.println("    Current managed slot: ID=" + s.getId() + ", " + s.getStartTime() + " to " + s.getEndTime()));


        // 2. Prepare a list of all time segments to be considered for merging on this date.
        List<WorkerAvailabilitySlot> segmentsToMerge = new ArrayList<>();

        // Add the segment that was just freed by the assignment removal
        WorkerAvailabilitySlot freedSegment = new WorkerAvailabilitySlot();
        freedSegment.setUser(worker);
        freedSegment.setDate(removedDate);
        freedSegment.setStartTime(removedStartTime);
        freedSegment.setEndTime(removedEndTime);
        segmentsToMerge.add(freedSegment);
        System.out.println("  Added freed segment to list for merging.");

        // Add all current existing availability segments for that date as *new* copies for merging.
        // Using copies ensures original entities are not affected by sorting or merging logic
        for (WorkerAvailabilitySlot existingSlot : currentManagedSlotsForDateCopy) {
            WorkerAvailabilitySlot copy = new WorkerAvailabilitySlot();
            copy.setUser(worker);
            copy.setDate(existingSlot.getDate());
            copy.setStartTime(existingSlot.getStartTime());
            copy.setEndTime(existingSlot.getEndTime());
            segmentsToMerge.add(copy);
        }
        System.out.println("  Total segments to consider for merging: " + segmentsToMerge.size());

        // 3. Sort all segments by start time
        segmentsToMerge.sort((s1, s2) -> s1.getStartTime().compareTo(s2.getStartTime()));
        System.out.println("  Segments sorted by start time.");
        segmentsToMerge.forEach(s -> System.out.println("    Sorted segment: " + s.getStartTime() + " to " + s.getEndTime()));


        // 4. Perform the merge operation, creating entirely new WorkerAvailabilitySlot objects for the results
        List<WorkerAvailabilitySlot> finalMergedSlots = new ArrayList<>();
        if (!segmentsToMerge.isEmpty()) {
            WorkerAvailabilitySlot currentMergedSegment = new WorkerAvailabilitySlot();
            currentMergedSegment.setUser(worker);
            currentMergedSegment.setDate(segmentsToMerge.get(0).getDate());
            currentMergedSegment.setStartTime(segmentsToMerge.get(0).getStartTime());
            currentMergedSegment.setEndTime(segmentsToMerge.get(0).getEndTime());
            System.out.println("  Initial merged segment: " + currentMergedSegment.getStartTime() + " to " + currentMergedSegment.getEndTime());

            for (int i = 1; i < segmentsToMerge.size(); i++) {
                WorkerAvailabilitySlot nextSegment = segmentsToMerge.get(i);
                System.out.println("    Comparing current merged (" + currentMergedSegment.getStartTime() + "-" + currentMergedSegment.getEndTime() + ") with next segment (" + nextSegment.getStartTime() + "-" + nextSegment.getEndTime() + ")");

                // Check for overlap or immediate contiguity
                if (currentMergedSegment.overlaps(nextSegment.getStartTime(), nextSegment.getEndTime()) ||
                        currentMergedSegment.getEndTime().equals(nextSegment.getStartTime())) {
                    // Merge: extend the end time of the current merged segment
                    currentMergedSegment.setEndTime(
                            currentMergedSegment.getEndTime().isAfter(nextSegment.getEndTime()) ?
                                    currentMergedSegment.getEndTime() : nextSegment.getEndTime()
                    );
                    System.out.println("      Merged. New current merged segment: " + currentMergedSegment.getStartTime() + " to " + currentMergedSegment.getEndTime());
                } else {
                    // No overlap/contiguity, so add the current merged segment to the final list
                    finalMergedSlots.add(currentMergedSegment);
                    System.out.println("      No merge. Added " + currentMergedSegment.getStartTime() + " to " + currentMergedSegment.getEndTime() + " to final list.");
                    // Create a NEW instance for the next segment to start a new merged block
                    currentMergedSegment = new WorkerAvailabilitySlot();
                    currentMergedSegment.setUser(worker);
                    currentMergedSegment.setDate(nextSegment.getDate());
                    currentMergedSegment.setStartTime(nextSegment.getStartTime());
                    currentMergedSegment.setEndTime(nextSegment.getEndTime());
                    System.out.println("      Starting new merged segment: " + currentMergedSegment.getStartTime() + " to " + currentMergedSegment.getEndTime());
                }
            }
            finalMergedSlots.add(currentMergedSegment); // Add the last merged segment
            System.out.println("  Added final merged segment: " + currentMergedSegment.getStartTime() + " to " + currentMergedSegment.getEndTime() + " to final list.");
        }
        System.out.println("  Finished merging. Final merged slots count: " + finalMergedSlots.size());
        finalMergedSlots.forEach(s -> System.out.println("    Final merged slot: " + s.getStartTime() + " to " + s.getEndTime()));


        // Now, update the worker's managed collection directly
        // 1. Remove all old slots for this date from the worker's managed collection.
        // This signals to Hibernate that these are 'orphaned' and should be deleted if orphanRemoval=true.
        worker.getWorkerAvailabilitySlots().removeIf(slot -> slot.getDate().equals(removedDate));
        System.out.println("  Removed all old slots for " + removedDate + " from worker's in-memory collection.");

        // 2. Add the newly merged slots to the worker's managed collection.
        // Hibernate will handle inserting these new entities.
        worker.getWorkerAvailabilitySlots().addAll(finalMergedSlots);
        System.out.println("  Added " + finalMergedSlots.size() + " merged slots to worker's in-memory collection.");

        // 3. Explicitly delete the old slots from the database (those that were in currentManagedSlotsForDateCopy).
        // This is a safety net.
        if (!currentManagedSlotsForDateCopy.isEmpty()) {
            workerAvailabilitySlotRepository.deleteAllInBatch(currentManagedSlotsForDateCopy);
            System.out.println("  Explicitly deleted " + currentManagedSlotsForDateCopy.size() + " original availability slots from DB.");
        }

        // 4. Explicitly save the newly created slots.
        List<WorkerAvailabilitySlot> slotsToSaveExplicitly = finalMergedSlots.stream()
                .filter(slot -> slot.getId() == null) // Only save new ones (those without an ID)
                .collect(Collectors.toList());
        if (!slotsToSaveExplicitly.isEmpty()) {
            workerAvailabilitySlotRepository.saveAll(slotsToSaveExplicitly);
            System.out.println("  Explicitly saved " + slotsToSaveExplicitly.size() + " newly merged availability slots to DB.");
        }

        // At the end of the @Transactional method, Hibernate will flush the changes made
        // to the managed `worker` entity and its `workerAvailabilitySlots` collection.
        // This includes deleting "orphaned" slots and inserting new ones.
        // The explicit repository calls act as an additional guarantee.
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new ResourceNotFoundException("User not authenticated or principal is not a User object.");
        }
        return (User) authentication.getPrincipal();
    }
}