package com.buildscheduler.buildscheduler.dto.user;

import lombok.Data;

import java.util.Set;

@Data
public class SimpleUserWithRolesDto {
    private Long id;
    private String username;
    private String email;
    private Set<String> roles; // Just the role names
}