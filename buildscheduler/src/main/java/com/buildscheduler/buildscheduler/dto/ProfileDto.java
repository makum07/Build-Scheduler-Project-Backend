package com.buildscheduler.buildscheduler.dto;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.Set;

@Getter @Setter
public class ProfileDto {
    private Long id;
    private String username;
    private String email;
    private String profileStatus;
    private List<SkillDto> skills;
    private Set<String> certifications;
    private List<AvailabilitySlotDto> availability;
}
