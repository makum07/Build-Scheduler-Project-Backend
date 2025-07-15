package com.buildscheduler.buildscheduler.repository;

import com.buildscheduler.buildscheduler.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByName(String name);

    boolean existsByName(String skillName);
    Optional<Skill> findByNameIgnoreCase(String name);

}