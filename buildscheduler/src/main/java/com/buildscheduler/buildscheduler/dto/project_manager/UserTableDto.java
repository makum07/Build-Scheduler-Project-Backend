package com.buildscheduler.buildscheduler.dto.project_manager;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTableDto {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String role;
}
