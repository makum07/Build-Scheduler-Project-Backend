package com.buildscheduler.buildscheduler.repository;

import com.buildscheduler.buildscheduler.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("SELECT t FROM Task t WHERE t.startTime BETWEEN :start AND :end")
    List<Task> findTasksInDateRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    List<Task> findByStatus(Task.TaskStatus status);
}