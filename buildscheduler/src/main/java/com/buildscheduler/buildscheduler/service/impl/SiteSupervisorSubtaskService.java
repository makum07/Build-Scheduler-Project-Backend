package com.buildscheduler.buildscheduler.service.impl;

import com.buildscheduler.buildscheduler.dto.site_supervisor.EquipmentNeedDto;
import com.buildscheduler.buildscheduler.dto.site_supervisor.SubtaskRequestDto;
import com.buildscheduler.buildscheduler.dto.site_supervisor.SubtaskResponseDto;
import com.buildscheduler.buildscheduler.exception.ResourceNotFoundException;
import com.buildscheduler.buildscheduler.model.*;
import com.buildscheduler.buildscheduler.repository.*;
import com.buildscheduler.buildscheduler.service.NotificationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public SiteSupervisorSubtaskService(
            UserRepository userRepository,
            MainTaskRepository mainTaskRepository,
            SubtaskRepository subtaskRepository,
            SkillRepository skillRepository,
            NotificationService notificationService
    ) {
        this.userRepository = userRepository;
        this.mainTaskRepository = mainTaskRepository;
        this.subtaskRepository = subtaskRepository;
        this.skillRepository = skillRepository;
        this.notificationService = notificationService;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User)) {
            throw new ResourceNotFoundException("User not authenticated");
        }
        return (User) auth.getPrincipal();
    }

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
        subtask.setEstimatedHours(dto.getEstimatedHours());
        subtask.setRequiredWorkers(dto.getRequiredWorkers());
        subtask.setPriority(dto.getPriority());
        subtask.setRequiredSkills(lookupSkills(dto.getRequiredSkills()));
        subtask.setStatus(Subtask.TaskStatus.PLANNED);
        subtask.setMainTask(mainTask);
        subtask.setProject(mainTask.getProject());

        if (dto.getEquipmentNeeds() != null) {
            for (EquipmentNeedDto needDto : dto.getEquipmentNeeds()) {

            }
        }

        Subtask savedSubtask = subtaskRepository.save(subtask);
        notifyEquipmentManager(savedSubtask);

        return convertToDto(savedSubtask);
    }

    private Set<Skill> lookupSkills(Set<String> names) {
        if (names == null || names.isEmpty()) return Set.of();

        return names.stream()
                .map(String::toLowerCase)
                .distinct()
                .map(name -> skillRepository.findByNameIgnoreCase(name)
                        .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + name)))
                .collect(Collectors.toSet());
    }

    private SubtaskResponseDto convertToDto(Subtask s) {
        SubtaskResponseDto dto = new SubtaskResponseDto();
        dto.setId(s.getId());
        dto.setTitle(s.getTitle());
        dto.setDescription(s.getDescription());

        dto.setEstimatedHours(s.getEstimatedHours());
        dto.setRequiredWorkers(s.getRequiredWorkers());
        dto.setPriority(s.getPriority());
        dto.setStatus(s.getStatus().name());
        dto.setMainTaskId(s.getMainTask().getId());
        dto.setProjectId(s.getProject().getId());

        dto.setRequiredSkills(
                s.getRequiredSkills() == null ? Set.of() :
                        s.getRequiredSkills().stream()
                                .map(Skill::getName)
                                .collect(Collectors.toSet())
        );

        dto.setEquipmentNeeds(
                s.getEquipmentNeeds() == null ? Set.of() :
                        new HashSet<>(s.getEquipmentNeeds()).stream()
                                .map(e -> {
                                    EquipmentNeedDto edto = new EquipmentNeedDto();

                                    return edto;
                                }).collect(Collectors.toSet())
        );

        return dto;
    }

    private void notifyEquipmentManager(Subtask subtask) {
        User manager = subtask.getProject().getEquipmentManager();
        if (manager != null) {
            String message = String.format(
                    "New equipment needs requested for subtask: %s (Project: %s)",
                    subtask.getTitle(), subtask.getProject().getTitle()
            );
            notificationService.createNotification(
                    manager.getId(),
                    message,
                    Notification.NotificationType.EQUIPMENT_REQUEST
            );
        }
    }
}