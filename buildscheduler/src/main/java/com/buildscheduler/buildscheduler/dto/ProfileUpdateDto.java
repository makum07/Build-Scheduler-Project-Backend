package com.buildscheduler.buildscheduler.dto;

import lombok.Getter; import lombok.Setter;
import java.util.Set;

@Getter @Setter
public class ProfileUpdateDto {
    private Set<Long> skillIds;
    private Set<String> certifications;
}