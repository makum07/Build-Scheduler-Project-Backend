package com.buildscheduler.buildscheduler.repository;

import com.buildscheduler.buildscheduler.model.Subtask;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SubtaskRepository extends JpaRepository<Subtask, Long> {
    List<Subtask> findByMainTaskIdIn(List<Long> mainTaskIds);

    @Modifying
    @Query("DELETE FROM Subtask s WHERE s.mainTask.id = :mainTaskId")
    void deleteByMainTaskId(@Param("mainTaskId") Long mainTaskId);

    @Query("SELECT s FROM Subtask s LEFT JOIN FETCH s.equipmentNeeds WHERE s.id = :id")
    Optional<Subtask> findWithEquipmentNeeds(@Param("id") Long id);

    @EntityGraph(
            type = EntityGraph.EntityGraphType.FETCH,
            attributePaths = {
                    "requiredSkills",
                    "equipmentNeeds",
                    "workerAssignments.worker",
                    "workerAssignments.assignedBy",
                    "equipmentAssignments.equipment",
                    "equipmentAssignments.assignedBy"
            }
    )
    List<Subtask> findByMainTaskId(Long mainTaskId);
}
