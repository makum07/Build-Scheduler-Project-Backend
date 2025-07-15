package com.buildscheduler.buildscheduler.dto.worker;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SimpleUserDto {
    private Long id;
    private String username;
    private String email;
}
