package com.buildscheduler.buildscheduler.dto.project_manager;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class MainTaskRequestDto {
    @NotBlank
    private String title;
    private String description;
    @NotNull private LocalDate plannedStartDate;
    @NotNull private LocalDate plannedEndDate;
    @Min(1) @Max(4) private Integer priority = 1;
    @Min(0) private Integer estimatedHours = 0;
    private Long supervisorId;
    private Long equipmentManagerId;

}