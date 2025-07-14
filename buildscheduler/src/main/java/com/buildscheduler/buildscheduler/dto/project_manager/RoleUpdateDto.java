package com.buildscheduler.buildscheduler.dto.project_manager;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RoleUpdateDto {
    @NotBlank(message = "User email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Role is required")
    private String role;
}