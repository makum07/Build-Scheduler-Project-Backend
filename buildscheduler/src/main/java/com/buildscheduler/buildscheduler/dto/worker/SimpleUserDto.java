package com.buildscheduler.buildscheduler.dto.worker;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleUserDto {
    private Long id;
    private String username;
    private String email; // Optional: not used in your mapper now
}

