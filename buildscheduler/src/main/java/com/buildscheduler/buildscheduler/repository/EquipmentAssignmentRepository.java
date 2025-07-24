package com.buildscheduler.buildscheduler.repository;

import com.buildscheduler.buildscheduler.model.EquipmentAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentAssignmentRepository extends JpaRepository<EquipmentAssignment, Long> {
    List<EquipmentAssignment> findByEquipmentId(Long equipmentId);
}