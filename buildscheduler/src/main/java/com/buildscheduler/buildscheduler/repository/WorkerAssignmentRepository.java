package com.buildscheduler.buildscheduler.repository;

import com.buildscheduler.buildscheduler.model.User;
import com.buildscheduler.buildscheduler.model.WorkerAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface WorkerAssignmentRepository extends JpaRepository<WorkerAssignment, Long> {
    Set<WorkerAssignment> findByWorker(User worker);
}