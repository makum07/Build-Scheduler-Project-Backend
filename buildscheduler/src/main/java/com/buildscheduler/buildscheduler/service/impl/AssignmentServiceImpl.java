package com.buildscheduler.buildscheduler.service.impl;

import com.buildscheduler.buildscheduler.dto.site_supervisor.AssignmentDto;
import com.buildscheduler.buildscheduler.exception.ConflictException;
import com.buildscheduler.buildscheduler.exception.ResourceNotFoundException;
import com.buildscheduler.buildscheduler.mapper.AssignmentMapper;
import com.buildscheduler.buildscheduler.model.*;
import com.buildscheduler.buildscheduler.repository.*;
import com.buildscheduler.buildscheduler.service.custom.AssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AssignmentServiceImpl implements AssignmentService {
    private final AvailabilitySlotRepository slotRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final MainTaskRepository mainTaskRepository;
    private final AssignmentMapper assignmentMapper;

    public AssignmentServiceImpl(AvailabilitySlotRepository slotRepository,
                                 AssignmentRepository assignmentRepository,
                                 UserRepository userRepository,
                                 MainTaskRepository mainTaskRepository,
                                 AssignmentMapper assignmentMapper) {
        this.slotRepository = slotRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.mainTaskRepository = mainTaskRepository;
        this.assignmentMapper = assignmentMapper;
    }

    @Override
    public Assignment assignWorker(AssignmentDto dto) {
        User worker = userRepository.findById(dto.getWorkerId())
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found"));

        MainTask mainTask = mainTaskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        // Check for assignment conflicts
        List<Assignment> conflicts = assignmentRepository.findConflictingAssignments(
                worker, dto.getStartTime(), dto.getEndTime()
        );

        if (!conflicts.isEmpty()) {
            throw new ConflictException("Worker has conflicting assignments during this time");
        }

        // Find containing availability slot
        Optional<AvailabilitySlot> slotOpt = slotRepository.findContainingSlot(
                worker,
                dto.getStartTime().toLocalDate(),
                dto.getStartTime().toLocalTime(),
                dto.getEndTime().toLocalTime()
        );

        if (slotOpt.isEmpty()) {
            throw new ConflictException("Worker is not available during this time");
        }

        AvailabilitySlot slot = slotOpt.get();

        // Split the availability slot
        List<AvailabilitySlot> newSlots = slot.splitForAssignment(
                dto.getStartTime(), dto.getEndTime()
        );

        // Replace original slot with new split slots
        slotRepository.delete(slot);
        slotRepository.saveAll(newSlots);

        // Create and save assignment
        Assignment assignment = assignmentMapper.toEntity(dto);
        assignment.setWorker(worker);
        assignment.setMainTask(mainTask); // Use setMainTask instead of setMainTask
        return assignmentRepository.save(assignment);
    }

    @Override
    public List<Assignment> getWorkerAssignments(Long workerId) {
        User worker = userRepository.findById(workerId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found"));
        return assignmentRepository.findByWorker(worker);
    }

    @Override
    public void removeAssignment(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
        assignmentRepository.delete(assignment);
    }
}