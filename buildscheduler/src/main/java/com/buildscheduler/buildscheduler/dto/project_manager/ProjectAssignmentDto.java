package com.buildscheduler.buildscheduler.dto.project_manager;

import com.buildscheduler.buildscheduler.model.ProjectAssignment.AssignmentRole;
import lombok.*;

import java.time.LocalDateTime;

@Data
public class ProjectAssignmentDto {
    private Long id;

    private Long userId;
    private String userName;
    private String userEmail;

    private Long assignedById;
    private String assignedByName;

    private AssignmentRole assignmentRole;
    private LocalDateTime assignedAt;

    private boolean isActive;


    public void setIsActive(boolean active) {
    }
}
