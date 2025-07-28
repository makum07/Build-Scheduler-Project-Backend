package com.buildscheduler.buildscheduler.service.impl;

import com.buildscheduler.buildscheduler.dto.worker.*;
import com.buildscheduler.buildscheduler.exception.ConflictException;
import com.buildscheduler.buildscheduler.exception.ResourceNotFoundException;
import com.buildscheduler.buildscheduler.mapper.AvailabilitySlotMapper;
import com.buildscheduler.buildscheduler.mapper.ProfileMapper;
import com.buildscheduler.buildscheduler.mapper.SkillMapper;
import com.buildscheduler.buildscheduler.model.*;
import com.buildscheduler.buildscheduler.repository.*;

import com.buildscheduler.buildscheduler.service.custom.ProfileService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProfileServiceImpl implements ProfileService {
    private final SkillRepository skillRepository;
    private final WorkerAvailabilitySlotRepository slotRepository;
    private final UserRepository userRepository;
    private final SkillMapper skillMapper;
    private final AvailabilitySlotMapper slotMapper;
    private final ProfileMapper profileMapper;
    private final WorkerAssignmentRepository workerAssignmentRepository;

    public ProfileServiceImpl(SkillRepository skillRepository,
                              WorkerAvailabilitySlotRepository slotRepository,
                              UserRepository userRepository,
                              SkillMapper skillMapper,
                              AvailabilitySlotMapper slotMapper,
                              ProfileMapper profileMapper, WorkerAssignmentRepository workerAssignmentRepository) {
        this.skillRepository = skillRepository;
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
        this.skillMapper = skillMapper;
        this.slotMapper = slotMapper;
        this.profileMapper = profileMapper;
        this.workerAssignmentRepository = workerAssignmentRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userDetails = (User) authentication.getPrincipal();
        return userRepository.findByEmail(userDetails.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void updateProfileStatus(User user) {
        boolean hasSkills = !user.getSkills().isEmpty();
        boolean hasAvailability = !user.getWorkerAvailabilitySlots().isEmpty();
        user.setProfileStatus(hasSkills && hasAvailability ? "COMPLETE" : "INCOMPLETE");
        userRepository.save(user);
    }

    @Override
    public SkillDto addSkill(SkillDto dto) {
        User user = getCurrentUser();

        Skill skill;

        // Handle case when skill already exists by name
        if (dto.getId() == null) {
            // Check if skill with same name already exists (case-insensitive)
            skill = skillRepository.findByNameIgnoreCase(dto.getName())
                    .orElseGet(() -> {
                        Skill newSkill = new Skill();
                        newSkill.setName(dto.getName());
                        return skillRepository.save(newSkill);
                    });
        } else {
            // If skill ID is given, fetch it
            skill = skillRepository.findById(dto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Skill not found"));
        }

        // Avoid duplicate skill assignment
        if (!user.getSkills().contains(skill)) {
            user.getSkills().add(skill);
            userRepository.save(user);
            updateProfileStatus(user);
        }

        return skillMapper.toDto(skill);
    }


    @Override
    public void removeSkill(Long skillId) {
        User user = getCurrentUser();
        boolean removed = user.getSkills().removeIf(skill -> skill.getId().equals(skillId));
        if (!removed) {
            throw new ResourceNotFoundException("Skill not found in user profile");
        }
        userRepository.save(user);
        updateProfileStatus(user);
    }

    @Override
    public void addCertification(String certification) {
        User user = getCurrentUser();
        String clean = certification.replaceAll("^\"|\"$", ""); // removes leading/trailing quotes
        user.getCertifications().add(clean);
        userRepository.save(user);
    }


    @Override
    public AvailabilitySlotDto addAvailabilitySlot(AvailabilitySlotDto dto) {
        User user = getCurrentUser();
        Optional<WorkerAvailabilitySlot> existing = slotRepository.findByUserAndDate(user, dto.getDate());
        if (existing.isPresent()) {
            throw new ConflictException("Availability slot already exists for this date");
        }

        WorkerAvailabilitySlot slot = slotMapper.toEntity(dto);
        slot.setUser(user);
        slotRepository.save(slot);
        updateProfileStatus(user);
        return slotMapper.toDto(slot);
    }

    @Override
    public List<AvailabilitySlotDto> updateAvailabilitySlots(BulkAvailabilityDto dto) {
        User user = getCurrentUser();

        // Delete requested slots
        if (dto.getSlotIdsToDelete() != null) {
            dto.getSlotIdsToDelete().forEach(id -> {
                if (slotRepository.existsById(id)) {
                    slotRepository.deleteById(id);
                }
            });
        }

        // Update or create slots
        if (dto.getSlots() != null) {
            for (AvailabilitySlotDto slotDto : dto.getSlots()) {
                if (slotDto.getId() != null) {
                    // Update existing slot
                    slotRepository.findById(slotDto.getId()).ifPresent(slot -> {
                        slot.setDate(slotDto.getDate());
                        slot.setStartTime(slotDto.getStartTime());
                        slot.setEndTime(slotDto.getEndTime());
                        slotRepository.save(slot);
                    });
                } else {
                    // Create new slot
                    WorkerAvailabilitySlot newSlot = slotMapper.toEntity(slotDto);
                    newSlot.setUser(user);
                    slotRepository.save(newSlot);
                }
            }
        }

        updateProfileStatus(user);
        return slotRepository.findByUser(user).stream()
                .map(slotMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void removeAvailabilitySlot(Long id) {
        User user = getCurrentUser();

        WorkerAvailabilitySlot slotToRemove = slotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Availability slot not found"));

        // Remove from user's availabilitySlots set first (in-memory)
        user.getWorkerAvailabilitySlots().removeIf(slot -> slot.getId().equals(id));

        // Then delete from DB
        slotRepository.delete(slotToRemove);

        // Finally update profile status
        updateProfileStatus(user);
    }
    @Override
    public List<AvailabilitySlotDto> getAllAvailabilitySlots() {
        User user = getCurrentUser();
        List<WorkerAvailabilitySlot> slots = slotRepository.findByUser(user);
        return slots.stream()
                .map(slotMapper::toDto)
                .collect(Collectors.toList());
    }


    @Override
    public List<AvailabilitySlotDto> getAvailabilitySlots(LocalDate start, LocalDate end) {
        User user = getCurrentUser();
        List<WorkerAvailabilitySlot> slots = slotRepository.findByUserInDateRange(user, start, end);
        return slots.stream()
                .map(slotMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProfileDto getUserProfile() {
        User user = getCurrentUser();
        return profileMapper.toDto(user);
    }

    @Override
    public List<SkillDto> getAllSkills() {
        return skillRepository.findAll().stream()
                .map(skillMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SkillDto> getMySkills() {
        User user = getCurrentUser();
        return user.getSkills().stream()
                .map(skillMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Set<String> getMyCertifications() {
        User user = getCurrentUser();
        return user.getCertifications();
    }

    @Override
    public void removeCertification(String certification) {
        User user = getCurrentUser();
        String clean = certification.replaceAll("^\"|\"$", "");
        boolean removed = user.getCertifications().removeIf(c -> c.equalsIgnoreCase(clean));
        if (!removed) {
            throw new ResourceNotFoundException("Certification not found");
        }
        userRepository.save(user);
    }


    @Override
    @Transactional(readOnly = true) // Mark as read-only
    public Set<WorkerAssignmentDetailsDto> getMyAssignments() {
        User currentUser = getCurrentUser(); // Get the authenticated User entity

        // Find all worker assignments for the current user (worker)
        // Ensure that findByWorker is properly defined in WorkerAssignmentRepository
        Set<WorkerAssignment> assignments = workerAssignmentRepository.findByWorker(currentUser);

        // Map them to the DTO
        return assignments.stream()
                .map(this::convertToWorkerAssignmentDetailsDto)
                .collect(Collectors.toSet());
    }

    // Helper method to convert WorkerAssignment entity to DTO
    private WorkerAssignmentDetailsDto convertToWorkerAssignmentDetailsDto(WorkerAssignment assignment) {
        WorkerAssignmentDetailsDto dto = new WorkerAssignmentDetailsDto();
        dto.setAssignmentId(assignment.getId());
        dto.setAssignmentStart(assignment.getAssignmentStart());
        dto.setAssignmentEnd(assignment.getAssignmentEnd());
        dto.setWorkerNotes(assignment.getWorkerNotes());

        // Subtask details
        if (assignment.getSubtask() != null) {
            dto.setSubtaskId(assignment.getSubtask().getId());
            dto.setSubtaskTitle(assignment.getSubtask().getTitle());
            dto.setSubtaskDescription(assignment.getSubtask().getDescription());

            // MainTask details (accessed via Subtask)
            if (assignment.getSubtask().getMainTask() != null) {
                dto.setMainTaskId(assignment.getSubtask().getMainTask().getId());
                dto.setMainTaskTitle(assignment.getSubtask().getMainTask().getTitle());

                // Project details (accessed via MainTask's project)
                if (assignment.getSubtask().getMainTask().getProject() != null) {
                    dto.setProjectId(assignment.getSubtask().getMainTask().getProject().getId());
                    dto.setProjectTitle(assignment.getSubtask().getMainTask().getProject().getTitle());
                }
            }
        }

        // Assigned By user details
        if (assignment.getAssignedBy() != null) {
            dto.setAssignedById(assignment.getAssignedBy().getId());
            dto.setAssignedByName(assignment.getAssignedBy().getUsername()); // Or full name if available
        }

        return dto;
    }
}