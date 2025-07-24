package com.buildscheduler.buildscheduler.service.impl;

import com.buildscheduler.buildscheduler.dto.notification.NotificationDto;
import com.buildscheduler.buildscheduler.dto.user.FullUserProfileDto;
import com.buildscheduler.buildscheduler.exception.ResourceNotFoundException;
import com.buildscheduler.buildscheduler.mapper.NotificationMapper;
import com.buildscheduler.buildscheduler.mapper.UserMapper;
import com.buildscheduler.buildscheduler.model.Notification;
import com.buildscheduler.buildscheduler.model.User;
import com.buildscheduler.buildscheduler.repository.NotificationRepository;
import com.buildscheduler.buildscheduler.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final UserMapper userMapper;
    private final NotificationMapper notificationMapper;

    @Transactional(readOnly = true)
    public FullUserProfileDto getFullUserProfileById(Long userId) {
        // Use the custom query to eagerly fetch all necessary data
        User user = userRepository.findFullProfileById(userId) // <-- Use the new custom query
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // No need for explicit .size() calls here anymore, as JOIN FETCH handles it
        return userMapper.toFullProfileDto(user);
    }

    @Transactional(readOnly = true)
    public FullUserProfileDto getMyFullProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new ResourceNotFoundException("User not authenticated");
        }
        User currentUser = (User) authentication.getPrincipal(); // This User object might be a proxy
        // Fetch the full profile based on the username from the authentication principal
        // This ensures the fetched user is the one with all collections initialized by the custom query.
        return getFullUserProfileById(currentUser.getId()); // Use the ID to call the full fetch method
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getMyNotifications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            throw new ResourceNotFoundException("User not authenticated");
        }
        User currentUser = (User) authentication.getPrincipal();
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
        return notificationMapper.toDtoList(notifications);
    }
}