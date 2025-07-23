package com.buildscheduler.buildscheduler.repository;

import com.buildscheduler.buildscheduler.model.Equipment;
import com.buildscheduler.buildscheduler.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    // Add a method to find equipment by its manager
    List<Equipment> findByEquipmentManager(User manager);
}