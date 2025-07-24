package com.buildscheduler.buildscheduler.mapper;

import com.buildscheduler.buildscheduler.dto.notification.NotificationDto;
import com.buildscheduler.buildscheduler.model.Notification;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class NotificationMapper {

    public NotificationDto toDto(Notification notification) {
        if (notification == null) {
            return null;
        }
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setType(notification.getType());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }

    public List<NotificationDto> toDtoList(List<Notification> notifications) {
        if (notifications == null) {
            return null;
        }
        return notifications.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public Set<NotificationDto> toDtoSet(Set<Notification> notifications) {
        if (notifications == null) {
            return null;
        }
        return notifications.stream()
                .map(this::toDto)
                .collect(Collectors.toSet());
    }
}