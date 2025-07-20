package com.buildscheduler.buildscheduler.service.impl;

import com.buildscheduler.buildscheduler.dto.project_manager.ProjectResponseDto;
import com.buildscheduler.buildscheduler.dto.worker.SimpleUserDto;
import com.buildscheduler.buildscheduler.exception.ResourceNotFoundException;
import com.buildscheduler.buildscheduler.model.Project;
import com.buildscheduler.buildscheduler.model.User;
import com.buildscheduler.buildscheduler.repository.ProjectRepository;
import com.buildscheduler.buildscheduler.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SiteSupervisorProjectService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public SiteSupervisorProjectService(
            UserRepository userRepository,
            ProjectRepository projectRepository
    ) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new ResourceNotFoundException("User not authenticated");
        }

        return (User) authentication.getPrincipal();
    }

    public List<ProjectResponseDto> getProjectsForSupervisor() {
        User currentUser = getCurrentUser();
        List<Project> projects = projectRepository.findBySiteSupervisor(currentUser);

        return projects.stream().map(project -> {
            ProjectResponseDto dto = new ProjectResponseDto();
            dto.setId(project.getId());
            dto.setTitle(project.getTitle());
            dto.setDescription(project.getDescription());
            dto.setStatus(project.getStatus());  // Use enum directly instead of .name()
            dto.setStartDate(project.getStartDate());
            dto.setEndDate(project.getEndDate());
            dto.setEstimatedBudget(project.getEstimatedBudget());
            dto.setLocation(project.getLocation());
            dto.setPriority(project.getPriority());

            // Convert users to SimpleUserDto
            if (project.getSiteSupervisor() != null) {
                dto.setSiteSupervisor(convertToSimpleUserDto(project.getSiteSupervisor()));
            }
            if (project.getEquipmentManager() != null) {
                dto.setEquipmentManager(convertToSimpleUserDto(project.getEquipmentManager()));
            }
            if (project.getProjectManager() != null) {
                dto.setProjectManagerId(project.getProjectManager().getId());
                dto.setProjectManagerName(project.getProjectManager().getUsername());
            }

            // Default values (you'll implement these later)
            dto.setCompletionPercentage(0.0);
            dto.setOverdue(false);

            return dto;
        }).collect(Collectors.toList());
    }

    private SimpleUserDto convertToSimpleUserDto(User user) {
        SimpleUserDto dto = new SimpleUserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setUsername(user.getUsername());
        return dto;
    }
}