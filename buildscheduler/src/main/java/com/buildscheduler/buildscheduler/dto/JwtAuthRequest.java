package com.buildscheduler.buildscheduler.dto;

import lombok.Data;

@Data
public class JwtAuthRequest {
    private String username;
    private String password;
}
