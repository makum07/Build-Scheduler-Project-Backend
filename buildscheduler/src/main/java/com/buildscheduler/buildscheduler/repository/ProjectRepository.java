package com.buildscheduler.buildscheduler.repository;

import com.buildscheduler.buildscheduler.model.Project;
import com.buildscheduler.buildscheduler.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findByProjectManager(User manager, Pageable pageable);
}