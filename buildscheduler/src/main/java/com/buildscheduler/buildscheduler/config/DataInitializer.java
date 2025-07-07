// DataInitializer.java
package com.buildscheduler.buildscheduler.config;

import com.buildscheduler.buildscheduler.model.Role;
import com.buildscheduler.buildscheduler.repository.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PostConstruct
    public void init() {

        createRoleIfNotFound("ROLE_WORKER");
        createRoleIfNotFound("ROLE_SITE_SUPERVISOR");
        createRoleIfNotFound("ROLE_PROJECT_MANAGER");
        createRoleIfNotFound("ROLE_EQUIPMENT_MANAGER");
    }

    private void createRoleIfNotFound(String name) {
        if (!roleRepository.existsByName(name)) {
            Role role = new Role();
            role.setName(name);
            roleRepository.save(role);
        }
    }
}