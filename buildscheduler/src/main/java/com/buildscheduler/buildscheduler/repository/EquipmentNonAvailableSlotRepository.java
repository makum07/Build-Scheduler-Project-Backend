package com.buildscheduler.buildscheduler.repository;

import com.buildscheduler.buildscheduler.model.Equipment;
import com.buildscheduler.buildscheduler.model.EquipmentNonAvailableSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentNonAvailableSlotRepository extends JpaRepository<EquipmentNonAvailableSlot, Long> {
    List<EquipmentNonAvailableSlot> findByEquipmentId(Long equipmentId);

    // Added for assignment deletion cleanup (if not handled by cascade/orphanRemoval implicitly)
    Optional<EquipmentNonAvailableSlot> findByEquipmentAndTypeAndStartTimeAndEndTime(
            Equipment equipment,
            EquipmentNonAvailableSlot.NonAvailabilityType type,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
}