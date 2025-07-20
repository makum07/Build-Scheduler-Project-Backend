package com.buildscheduler.buildscheduler.service;

import com.buildscheduler.buildscheduler.exception.ResourceNotFoundException;
import com.buildscheduler.buildscheduler.model.Notification;
import com.buildscheduler.buildscheduler.model.User;
import com.buildscheduler.buildscheduler.repository.NotificationRepository;
import com.buildscheduler.buildscheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public void createNotification(Long userId, String message, Notification.NotificationType type) {
        Notification notification = new Notification();
        notification.setUser(userRepository.getReferenceById(userId)); // Proxy only
        notification.setMessage(message);
        notification.setType(type);
        notificationRepository.save(notification);
    }
}
