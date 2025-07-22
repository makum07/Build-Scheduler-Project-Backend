package com.buildscheduler.buildscheduler.repository;

import com.buildscheduler.buildscheduler.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

}
