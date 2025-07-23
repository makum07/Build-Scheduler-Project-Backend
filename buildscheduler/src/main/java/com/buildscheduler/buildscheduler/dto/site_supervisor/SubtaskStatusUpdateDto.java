package com.buildscheduler.buildscheduler.dto.site_supervisor;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubtaskStatusUpdateDto {
    @NotBlank(message = "Status cannot be empty")
    private String status;
}