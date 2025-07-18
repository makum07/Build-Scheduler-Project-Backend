package com.buildscheduler.buildscheduler.repository;

import com.buildscheduler.buildscheduler.model.Subtask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubtaskRepository extends JpaRepository<Subtask, Long> {
    List<Subtask> findByMainTaskIdIn(List<Long> mainTaskIds);

    @Modifying
    @Query("DELETE FROM Subtask s WHERE s.mainTask.id = :mainTaskId")
    void deleteByMainTaskId(@Param("mainTaskId") Long mainTaskId);
}
