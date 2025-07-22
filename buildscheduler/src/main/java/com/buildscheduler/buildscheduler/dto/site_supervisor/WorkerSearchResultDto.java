package com.buildscheduler.buildscheduler.dto.site_supervisor;

import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
public class WorkerSearchResultDto {
    private Long id;
    private String username;
    private String email;
    private Set<String> skills; // Only skill names
}