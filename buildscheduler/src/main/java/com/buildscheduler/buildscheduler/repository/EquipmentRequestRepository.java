package com.buildscheduler.buildscheduler.repository;

import com.buildscheduler.buildscheduler.model.EquipmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRequestRepository extends JpaRepository<EquipmentRequest, Long> {
}