package com.buildscheduler.buildscheduler.dto.auth;

import com.buildscheduler.buildscheduler.dto.site_supervisor.AssignmentDto;
import com.buildscheduler.buildscheduler.dto.worker.AvailabilitySlotDto;
import com.buildscheduler.buildscheduler.dto.worker.SimpleUserDto;
import com.buildscheduler.buildscheduler.dto.worker.SkillDto;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class FullUserProfileDto {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String role;
    private String profileStatus;

    private List<SkillDto> skills;
    private Set<String> certifications;
    private List<AvailabilitySlotDto> availabilitySlots;

    private SimpleUserDto siteSupervisor;
    private SimpleUserDto projectManager;

    private List<AssignmentDto> assignments;
}
