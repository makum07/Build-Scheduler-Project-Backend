package com.buildscheduler.buildscheduler.repository;

import com.buildscheduler.buildscheduler.model.MainTask;
import com.buildscheduler.buildscheduler.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MainTaskRepository extends JpaRepository<MainTask, Long> {
    Page<MainTask> findByProject(Project project, Pageable pageable);
    List<MainTask> findByProject(Project project);

    List<MainTask> findByProjectIdIn(List<Long> projectIds);
}