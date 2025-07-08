// DataInitializer.java
package com.buildscheduler.buildscheduler.config;

import com.buildscheduler.buildscheduler.dto.RoleDto;
import com.buildscheduler.buildscheduler.mapper.RoleMapper;
import com.buildscheduler.buildscheduler.model.Role;
import com.buildscheduler.buildscheduler.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    public DataInitializer(RoleRepository roleRepository, RoleMapper roleMapper) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
    }

    @PostConstruct
    public void init() {

        createRoleIfNotFound("WORKER");
        createRoleIfNotFound("SITE_SUPERVISOR");
        createRoleIfNotFound("PROJECT_MANAGER");
        createRoleIfNotFound("EQUIPMENT_MANAGER");
    }

    private void createRoleIfNotFound(String displayName) {
        String roleName = "ROLE_" + displayName;
        if (!roleRepository.existsByName(roleName)) {
            RoleDto roleDto = new RoleDto();
            roleDto.setName(displayName);
            roleRepository.save(roleMapper.toEntity(roleDto));
        }
    }
}