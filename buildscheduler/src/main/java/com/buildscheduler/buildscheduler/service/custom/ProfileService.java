package com.buildscheduler.buildscheduler.service.custom;


import com.buildscheduler.buildscheduler.dto.worker.AvailabilitySlotDto;
import com.buildscheduler.buildscheduler.dto.worker.BulkAvailabilityDto;
import com.buildscheduler.buildscheduler.dto.worker.ProfileDto;
import com.buildscheduler.buildscheduler.dto.worker.SkillDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface ProfileService {
    SkillDto addSkill(SkillDto dto);
    void removeSkill(Long id);
    void addCertification(String cert);
    void removeCertification(String cert);
    AvailabilitySlotDto addAvailabilitySlot(AvailabilitySlotDto dto);
    List<AvailabilitySlotDto> updateAvailabilitySlots(BulkAvailabilityDto dto);
    void removeAvailabilitySlot(Long id);
    List<AvailabilitySlotDto> getAvailabilitySlots(LocalDate start, LocalDate end);
    ProfileDto getUserProfile();
    List<SkillDto> getAllSkills();

    List<SkillDto> getMySkills();

    Set<String> getMyCertifications();

    List<AvailabilitySlotDto> getAllAvailabilitySlots();
}