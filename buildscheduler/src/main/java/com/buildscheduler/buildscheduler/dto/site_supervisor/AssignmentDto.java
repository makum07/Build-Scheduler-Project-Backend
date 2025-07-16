package com.buildscheduler.buildscheduler.dto.site_supervisor;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentDto {

    private Long id;

    private Long workerId; // Used when assigning
    private Long taskId;
    private String taskTitle; // ✅ Add this

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime endTime;

    private String status;

    // Custom constructor used in UserMapper
    public AssignmentDto(Long id, Long taskId, String taskTitle,
                         LocalDateTime startTime, LocalDateTime endTime, String status) {
        this.id = id;
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }
}
