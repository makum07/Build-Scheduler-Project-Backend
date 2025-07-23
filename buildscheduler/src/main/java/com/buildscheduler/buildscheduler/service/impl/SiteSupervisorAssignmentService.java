package com.buildscheduler.buildscheduler.service.impl;

import com.buildscheduler.buildscheduler.dto.site_supervisor.AssignmentRequestDto;
import com.buildscheduler.buildscheduler.dto.site_supervisor.WorkerSearchResultDto;
import com.buildscheduler.buildscheduler.exception.ConflictException;
import com.buildscheduler.buildscheduler.exception.ResourceNotFoundException;
import com.buildscheduler.buildscheduler.model.*;
import com.buildscheduler.buildscheduler.repository.SubtaskRepository;
import com.buildscheduler.buildscheduler.repository.UserRepository;
import com.buildscheduler.buildscheduler.repository.WorkerAssignmentRepository;
import com.buildscheduler.buildscheduler.repository.WorkerAvailabilitySlotRepository;
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
                    boolean hasSkills = worker.getSkills().stream()
                            .anyMatch(skill -> requiredSkills.contains(skill.getName().toLowerCase()));
                    System.out.println("Worker " + worker.getUsername() + " (ID: " + worker.getId() + ") - Has required skills (" + requiredSkills + "): " + hasSkills + " (Worker skills: " + worker.getSkills().stream().map(s -> s.getName()).collect(Collectors.joining(", ")) + ")");
                    return hasSkills;
                })
                .filter(worker -> {
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

        User assignedBy = getCurrentUser();
        if (assignedBy.getRoles().stream().noneMatch(role -> role.getName().equals("ROLE_SITE_SUPERVISOR"))) {
            throw new IllegalArgumentException("Authenticated user is not a Site Supervisor and cannot assign workers.");
        }

        LocalDateTime assignmentStart = requestDto.getAssignmentStart() != null ? requestDto.getAssignmentStart() : subtask.getPlannedStart();
        LocalDateTime assignmentEnd = requestDto.getAssignmentEnd() != null ? requestDto.getAssignmentEnd() : subtask.getPlannedEnd();

        if (worker.getRoles().stream().noneMatch(role -> role.getName().equals("ROLE_WORKER"))) {
            throw new IllegalArgumentException("Assigned user is not a WORKER.");
        }

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

        updateAvailabilityForAssignment(worker, assignmentStart, assignmentEnd);

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

        User worker = assignment.getWorker();
        LocalDateTime assignmentStart = assignment.getAssignmentStart();
        LocalDateTime assignmentEnd = assignment.getAssignmentEnd();
        Subtask subtask = assignment.getSubtask();

        workerAssignmentRepository.delete(assignment);

        reconstructAvailabilityAfterRemoval(worker, assignmentStart, assignmentEnd);

        String notificationMessage = String.format("Your assignment to subtask '%s' (ID: %d) from %s to %s has been removed.",
                subtask.getTitle(), subtask.getId(),
                assignmentStart.format(DateTimeFormatter.ofPattern("MMM dd HH:mm")),
                assignmentEnd.format(DateTimeFormatter.ofPattern("MMM dd HH:mm")));
        notificationService.createNotification(worker.getId(), notificationMessage, Notification.NotificationType.ASSIGNMENT);
    }

    private void updateAvailabilityForAssignment(User worker, LocalDateTime assignmentStart, LocalDateTime assignmentEnd) {
        if (worker.getWorkerAvailabilitySlots() == null) {
            return;
        }

        // Fetch slots for the specific date of assignment
        List<WorkerAvailabilitySlot> slotsOnDate = workerAvailabilitySlotRepository.findByUserIdAndDate(worker.getId(), assignmentStart.toLocalDate());

        List<WorkerAvailabilitySlot> slotsToDelete = new ArrayList<>();
        List<WorkerAvailabilitySlot> slotsToSave = new ArrayList<>();

        LocalTime assignmentTimeStart = assignmentStart.toLocalTime();
        LocalTime assignmentTimeEnd = assignmentEnd.toLocalTime();

        for (WorkerAvailabilitySlot slot : slotsOnDate) {
            LocalTime slotStart = slot.getStartTime();
            LocalTime slotEnd = slot.getEndTime();

            if (!slot.overlaps(assignmentTimeStart, assignmentTimeEnd)) {
                // No overlap, keep the slot as is
                continue;
            }

            // Mark this original slot for deletion as it will either be fully consumed or split
            slotsToDelete.add(slot);

            if (assignmentTimeStart.isAfter(slotStart)) {
                // Part of the slot before the assignment remains
                WorkerAvailabilitySlot newSlot = new WorkerAvailabilitySlot();
                newSlot.setUser(worker);
                newSlot.setDate(slot.getDate());
                newSlot.setStartTime(slotStart);
                newSlot.setEndTime(assignmentTimeStart);
                slotsToSave.add(newSlot);
            }

            if (assignmentTimeEnd.isBefore(slotEnd)) {
                // Part of the slot after the assignment remains
                WorkerAvailabilitySlot newSlot = new WorkerAvailabilitySlot();
                newSlot.setUser(worker);
                newSlot.setDate(slot.getDate());
                newSlot.setStartTime(assignmentTimeEnd);
                newSlot.setEndTime(slotEnd);
                slotsToSave.add(newSlot);
            }
        }

        // Perform deletions and saves in bulk
        if (!slotsToDelete.isEmpty()) {
            workerAvailabilitySlotRepository.deleteAll(slotsToDelete);
        }
        if (!slotsToSave.isEmpty()) {
            workerAvailabilitySlotRepository.saveAll(slotsToSave);
        }
    }

    private void reconstructAvailabilityAfterRemoval(User worker, LocalDateTime removedStart, LocalDateTime removedEnd) {
        LocalDate removedDate = removedStart.toLocalDate();
        LocalTime removedStartTime = removedStart.toLocalTime();
        LocalTime removedEndTime = removedEnd.toLocalTime();

        // 1. Get all current availability slots for the worker on the specific date.
        // These are the "old" slots that might need to be merged or kept.
        List<WorkerAvailabilitySlot> currentAvailabilitySlots = workerAvailabilitySlotRepository.findByUserIdAndDate(worker.getId(), removedDate);

        // 2. Prepare a list of all time segments to be considered for merging on this date.
        // These will be conceptual segments (not managed entities initially), including the newly freed time.
        List<WorkerAvailabilitySlot> segmentsToMerge = new ArrayList<>();

        // Add the segment that was just freed by the assignment removal
        WorkerAvailabilitySlot freedSegment = new WorkerAvailabilitySlot();
        freedSegment.setUser(worker);
        freedSegment.setDate(removedDate);
        freedSegment.setStartTime(removedStartTime);
        freedSegment.setEndTime(removedEndTime);
        segmentsToMerge.add(freedSegment);

        // Add all current existing availability segments for that date as *new* copies
        // This prevents Hibernate from tracking them as 'managed' entities
        // that are about to be deleted. We just need their time data.
        for (WorkerAvailabilitySlot existingSlot : currentAvailabilitySlots) {
            WorkerAvailabilitySlot copy = new WorkerAvailabilitySlot();
            copy.setUser(worker);
            copy.setDate(existingSlot.getDate());
            copy.setStartTime(existingSlot.getStartTime());
            copy.setEndTime(existingSlot.getEndTime());
            segmentsToMerge.add(copy);
        }

        // 3. Sort all segments by start time
        segmentsToMerge.sort((s1, s2) -> s1.getStartTime().compareTo(s2.getStartTime()));

        // 4. Perform the merge operation, creating entirely new WorkerAvailabilitySlot objects for the results
        List<WorkerAvailabilitySlot> finalMergedSlots = new ArrayList<>();
        if (!segmentsToMerge.isEmpty()) {
            WorkerAvailabilitySlot currentMergedSegment = new WorkerAvailabilitySlot();
            currentMergedSegment.setUser(worker);
            currentMergedSegment.setDate(segmentsToMerge.get(0).getDate());
            currentMergedSegment.setStartTime(segmentsToMerge.get(0).getStartTime());
            currentMergedSegment.setEndTime(segmentsToMerge.get(0).getEndTime());

            for (int i = 1; i < segmentsToMerge.size(); i++) {
                WorkerAvailabilitySlot nextSegment = segmentsToMerge.get(i);
                if (currentMergedSegment.overlaps(nextSegment.getStartTime(), nextSegment.getEndTime()) ||
                        currentMergedSegment.getEndTime().equals(nextSegment.getStartTime())) {
                    currentMergedSegment.setEndTime(
                            currentMergedSegment.getEndTime().isAfter(nextSegment.getEndTime()) ?
                                    currentMergedSegment.getEndTime() : nextSegment.getEndTime()
                    );
                } else {
                    finalMergedSlots.add(currentMergedSegment);
                    // Create a NEW instance for the next segment
                    currentMergedSegment = new WorkerAvailabilitySlot();
                    currentMergedSegment.setUser(worker);
                    currentMergedSegment.setDate(nextSegment.getDate());
                    currentMergedSegment.setStartTime(nextSegment.getStartTime());
                    currentMergedSegment.setEndTime(nextSegment.getEndTime());
                }
            }
            finalMergedSlots.add(currentMergedSegment); // Add the last merged segment
        }

        // 5. Delete ALL original slots for this date in the database.
        // This is safe because `currentAvailabilitySlots` are the managed entities,
        // and we derived our `finalMergedSlots` from copies.
        if (!currentAvailabilitySlots.isEmpty()) {
            workerAvailabilitySlotRepository.deleteAll(currentAvailabilitySlots);
        }

        // 6. Save the completely new, merged availability slots.
        if (!finalMergedSlots.isEmpty()) { // Only save if there are slots to save
            workerAvailabilitySlotRepository.saveAll(finalMergedSlots);
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new ResourceNotFoundException("User not authenticated or principal is not a User object.");
        }
        return (User) authentication.getPrincipal();
    }
}