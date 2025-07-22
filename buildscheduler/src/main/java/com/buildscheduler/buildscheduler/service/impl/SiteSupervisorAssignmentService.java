package com.buildscheduler.buildscheduler.service.impl;

import com.buildscheduler.buildscheduler.dto.site_supervisor.WorkerSearchResultDto;
import com.buildscheduler.buildscheduler.model.Subtask;
import com.buildscheduler.buildscheduler.model.User;
import com.buildscheduler.buildscheduler.repository.SubtaskRepository;
import com.buildscheduler.buildscheduler.repository.UserRepository;
import com.buildscheduler.buildscheduler.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SiteSupervisorAssignmentService {

    private final UserRepository userRepository;
    private final SubtaskRepository subtaskRepository;

    @Transactional(readOnly = true)
    public List<WorkerSearchResultDto> findBestMatchedWorkers(Long subtaskId) {
        Subtask subtask = subtaskRepository.findById(subtaskId)
                .orElseThrow(() -> new ResourceNotFoundException("Subtask not found with ID: " + subtaskId));

        Set<String> requiredSkills = subtask.getRequiredSkills().stream()
                .map(skill -> skill.getName().toLowerCase())
                .collect(Collectors.toSet());

        LocalDateTime plannedStart = subtask.getPlannedStart();
        LocalDateTime plannedEnd = subtask.getPlannedEnd();

        // Fetch all users who have the 'WORKER' role
        List<User> allWorkers = userRepository.findByRoles_Name("ROLE_WORKER");

        // ... after fetching allWorkers
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
                .map(skill -> skill.getName())
                .collect(Collectors.toSet()));
        return dto;
    }
}