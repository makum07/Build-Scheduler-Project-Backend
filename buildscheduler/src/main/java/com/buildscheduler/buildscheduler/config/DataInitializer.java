package com.buildscheduler.buildscheduler.config;

import com.buildscheduler.buildscheduler.dto.RoleDto;
import com.buildscheduler.buildscheduler.mapper.RoleMapper;
import com.buildscheduler.buildscheduler.model.Role;
import com.buildscheduler.buildscheduler.model.Skill;
import com.buildscheduler.buildscheduler.repository.RoleRepository;
import com.buildscheduler.buildscheduler.repository.SkillRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final SkillRepository skillRepository;

    public DataInitializer(RoleRepository roleRepository, RoleMapper roleMapper, SkillRepository skillRepository) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
        this.skillRepository = skillRepository;
    }

    @PostConstruct
    public void init() {
        initRoles();
        initSkills(); // <-- add this
    }

    private void initRoles() {
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

    private void initSkills() {
        List<String> defaultSkills = List.of(
                "Masonry", "Plumbing", "Electrical", "Carpentry",
                "Welding", "Painting", "Excavation", "Crane Operation",
                "Concrete Finishing", "Safety Supervision"
        );

        for (String skillName : defaultSkills) {
            if (!skillRepository.existsByName(skillName)) {
                Skill skill = new Skill();
                skill.setName(skillName);
                skillRepository.save(skill);
            }
        }
    }
}
