package com.buildscheduler.buildscheduler.repository;

import com.buildscheduler.buildscheduler.model.Project;
import com.buildscheduler.buildscheduler.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findByProjectManager(User manager, Pageable pageable);

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.mainTasks WHERE p.id = :id")
    Optional<Project> findByIdWithMainTasks(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Project p " +
            "LEFT JOIN FETCH p.mainTasks mt " +
            "LEFT JOIN FETCH mt.subtasks st " +  // Changed to subtasks (plural)
            "WHERE p.id = :id")
    Optional<Project> findByIdWithTasksAndSubtasks(@Param("id") Long id);
}