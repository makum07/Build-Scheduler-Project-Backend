package com.buildscheduler.buildscheduler.mapper;

import com.buildscheduler.buildscheduler.dto.auth.RoleDto;
import com.buildscheduler.buildscheduler.model.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper implements Mapper<Role, RoleDto> {

    @Override
    public RoleDto toDto(Role entity) {
        RoleDto dto = new RoleDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName().replace("ROLE_", ""));
        return dto;
    }

    @Override
    public Role toEntity(RoleDto dto) {
        Role role = new Role();
        // ID should not be set manually for new entities
        if (dto.getId() != null) {
            role.setId(dto.getId());
        }
        role.setName("ROLE_" + dto.getName().toUpperCase());
        return role;
    }
}