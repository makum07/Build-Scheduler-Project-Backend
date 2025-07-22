package com.buildscheduler.buildscheduler.dto.site_supervisor;

import com.buildscheduler.buildscheduler.dto.worker.SimpleUserDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class WorkerAssignmentDto {
    private Long id;
    private SimpleUserDto worker;
    private SimpleUserDto assignedBy;
    private LocalDateTime assignmentStart;
    private LocalDateTime assignmentEnd;
    private String notes;
}