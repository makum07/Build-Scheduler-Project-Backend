package com.buildscheduler.buildscheduler.repository;

import com.buildscheduler.buildscheduler.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}