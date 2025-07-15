package com.buildscheduler.buildscheduler.repository;

import com.buildscheduler.buildscheduler.model.Assignment;
import com.buildscheduler.buildscheduler.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    @Query("SELECT a FROM Assignment a WHERE a.worker = :worker")
    List<Assignment> findByWorker(@Param("worker") User worker);

    @Query("SELECT a FROM Assignment a WHERE a.worker = :worker AND " +
            "((a.startTime <= :start AND a.endTime >= :start) OR " +
            "(a.startTime <= :end AND a.endTime >= :end))")
    List<Assignment> findConflictingAssignments(
            @Param("worker") User worker,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}