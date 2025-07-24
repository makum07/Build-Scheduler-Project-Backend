package com.buildscheduler.buildscheduler.repository;

import com.buildscheduler.buildscheduler.model.Equipment;
import com.buildscheduler.buildscheduler.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    // Fetch equipment with its nonAvailableSlots and assignments eagerly for dynamic status calculation
    @EntityGraph(attributePaths = {"nonAvailableSlots", "assignments"})
    Optional<Equipment> findById(Long id);

    // Fetch equipment with its nonAvailableSlots and assignments eagerly for dynamic status calculation
    @EntityGraph(attributePaths = {"nonAvailableSlots", "assignments"})
    List<Equipment> findByEquipmentManager(User manager);

    // Fetch all equipment with its nonAvailableSlots and assignments eagerly
    @EntityGraph(attributePaths = {"nonAvailableSlots", "assignments"})
    List<Equipment> findAll(); // Override if you want to apply EntityGraph to findAll too
}