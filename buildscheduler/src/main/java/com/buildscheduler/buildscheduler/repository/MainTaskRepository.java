package com.buildscheduler.buildscheduler.repository;

import com.buildscheduler.buildscheduler.model.MainTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface MainTaskRepository extends JpaRepository<MainTask, Long> {

//    @Query("SELECT t FROM Task t WHERE t.startTime BETWEEN :start AND :end")
//    List<MainTask> findTasksInDateRange(
//            @Param("start") LocalDateTime start,
//            @Param("end") LocalDateTime end
//    );
//
//    List<MainTask> findByStatus(MainTask.TaskStatus status);
}