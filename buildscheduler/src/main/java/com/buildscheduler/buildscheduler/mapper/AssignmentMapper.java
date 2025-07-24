package com.buildscheduler.buildscheduler.mapper;

import com.buildscheduler.buildscheduler.dto.site_supervisor.WorkerAssignmentDto;
import com.buildscheduler.buildscheduler.dto.worker.SimpleUserDto; // Import SimpleUserDto
import com.buildscheduler.buildscheduler.model.WorkerAssignment;
import com.buildscheduler.buildscheduler.model.User; // Import User for direct mapping
import lombok.RequiredArgsConstructor; // Keep if you have other dependencies, or remove if not needed
import org.springframework.stereotype.Component;

@Component
// @RequiredArgsConstructor // Remove this if no other final fields are injected
public class AssignmentMapper {

    // REMOVE THIS: private final UserMapper userMapper; // This caused the circular dependency

    // Add a simple helper for User to SimpleUserDto conversion directly here
    private SimpleUserDto toSimpleUserDto(User user) {
        if (user == null) {
            return null;
        }
        return new SimpleUserDto(user.getId(), user.getUsername(), user.getEmail());
    }

    public WorkerAssignmentDto toWorkerAssignmentDto(WorkerAssignment workerAssignment) {
        if (workerAssignment == null) {
            return null;
        }
        WorkerAssignmentDto dto = new WorkerAssignmentDto();
        dto.setId(workerAssignment.getId());
        dto.setAssignmentStart(workerAssignment.getAssignmentStart());
        dto.setAssignmentEnd(workerAssignment.getAssignmentEnd());


        // Map the associated User entities to SimpleUserDto directly
        dto.setWorker(toSimpleUserDto(workerAssignment.getWorker()));
        dto.setAssignedBy(toSimpleUserDto(workerAssignment.getAssignedBy())); // Assuming AssignedBy exists

        return dto;
    }
}