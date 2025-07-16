package com.buildscheduler.buildscheduler.dto.project_manager;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ProjectRequestDto {
    @NotBlank
    private String title;
    private String description;
    @NotNull private LocalDate startDate;
    @NotNull private LocalDate endDate;
    private BigDecimal estimatedBudget;
    private String location;
    @Min(1) @Max(4) private Integer priority = 1;
}