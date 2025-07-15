package com.buildscheduler.buildscheduler.dto.auth;

import lombok.Data;

@Data
public class JwtAuthRequest {
    private String email;
    private String password;
}