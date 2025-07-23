package com.buildscheduler.buildscheduler.dto.site_supervisor;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AssignmentRequestDto {
    @NotNull(message = "Worker ID is required for assignment")
    private Long workerId;

    private LocalDateTime assignmentStart;
    private LocalDateTime assignmentEnd;

    private String workerNotes;
}