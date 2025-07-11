package com.buildscheduler.buildscheduler.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter; import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Set;

@Getter @Setter
public class TaskDto {
    private Long id;
    private String title;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endTime;
    private String status;
    private Set<SkillDto> requiredSkills;
}
