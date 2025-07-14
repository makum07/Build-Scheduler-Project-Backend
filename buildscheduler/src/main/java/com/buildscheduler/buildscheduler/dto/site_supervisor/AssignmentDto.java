package com.buildscheduler.buildscheduler.dto.site_supervisor;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter @Setter
public class AssignmentDto {
    private Long id; // Add this field
    private Long workerId;
    private Long taskId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endTime;
    private String status; // Add this field
}