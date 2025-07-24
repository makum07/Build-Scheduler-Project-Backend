package com.buildscheduler.buildscheduler.dto.notification;

import com.buildscheduler.buildscheduler.model.Notification;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDto {
    private Long id;
    private String message;
    private Notification.NotificationType type;
    private LocalDateTime createdAt;
}