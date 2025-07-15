package com.buildscheduler.buildscheduler.mapper;

import com.buildscheduler.buildscheduler.dto.worker.SkillDto;
import com.buildscheduler.buildscheduler.model.Skill;
import org.springframework.stereotype.Component;

@Component
public class SkillMapper implements Mapper<Skill, SkillDto> {
    @Override
    public SkillDto toDto(Skill entity) {
        SkillDto dto = new SkillDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        return dto;
    }

    @Override
    public Skill toEntity(SkillDto dto) {
        Skill entity = new Skill();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        return entity;
    }
}