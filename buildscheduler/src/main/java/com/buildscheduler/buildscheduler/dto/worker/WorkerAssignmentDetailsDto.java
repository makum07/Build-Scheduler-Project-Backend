package com.buildscheduler.buildscheduler.dto.worker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkerAssignmentDetailsDto {
    private Long assignmentId;
    private Long subtaskId;
    private String subtaskTitle;
    private String subtaskDescription;
    private Long mainTaskId;
    private String mainTaskTitle;
    private Long projectId;
    private String projectTitle;
    private LocalDateTime assignmentStart;
    private LocalDateTime assignmentEnd;
    private Long assignedById;
    private String assignedByName;
    private String workerNotes;
}