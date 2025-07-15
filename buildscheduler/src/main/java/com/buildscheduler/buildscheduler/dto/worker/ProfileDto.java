package com.buildscheduler.buildscheduler.dto.worker;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter @Setter
public class ProfileDto {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private String profileStatus;

    private Set<String> roles; // Names like "ROLE_WORKER", etc.

    private List<SkillDto> skills;
    private Set<String> certifications;
    private List<AvailabilitySlotDto> availability;

    // Hierarchy info
    private SimpleUserDto siteSupervisor;       // current user's supervisor (if WORKER)
    private SimpleUserDto projectManager;       // current user's manager (if SUPERVISOR or WORKER)

    private List<SimpleUserDto> workersSupervised; // if current user is SUPERVISOR
    private List<SimpleUserDto> teamMembers;       // if current user is PM
}