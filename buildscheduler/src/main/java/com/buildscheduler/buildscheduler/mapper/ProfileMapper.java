package com.buildscheduler.buildscheduler.mapper;

import com.buildscheduler.buildscheduler.dto.ProfileDto;
import com.buildscheduler.buildscheduler.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ProfileMapper {
    private final SkillMapper skillMapper;
    private final AvailabilitySlotMapper slotMapper;

    @Autowired
    public ProfileMapper(SkillMapper skillMapper, AvailabilitySlotMapper slotMapper) {
        this.skillMapper = skillMapper;
        this.slotMapper = slotMapper;
    }

    public ProfileDto toDto(User user) {
        ProfileDto dto = new ProfileDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setProfileStatus(user.getProfileStatus());
        dto.setSkills(user.getSkills().stream()
                .map(skillMapper::toDto)
                .collect(Collectors.toList()));
        dto.setCertifications(user.getCertifications());
        dto.setAvailability(user.getAvailabilitySlots().stream()
                .map(slotMapper::toDto)
                .collect(Collectors.toList()));
        return dto;
    }
}