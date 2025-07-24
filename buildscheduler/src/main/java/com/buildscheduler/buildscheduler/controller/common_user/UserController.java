package com.buildscheduler.buildscheduler.controller.common_user;

import com.buildscheduler.buildscheduler.dto.auth.UserDto;
import com.buildscheduler.buildscheduler.dto.notification.NotificationDto;
import com.buildscheduler.buildscheduler.dto.user.FullUserProfileDto; // Import the new DTO
import com.buildscheduler.buildscheduler.mapper.UserMapper;
import com.buildscheduler.buildscheduler.response.ApiResponse;
// Inject new service
import com.buildscheduler.buildscheduler.service.custom.UserService; // Keep if other methods still use it
import com.buildscheduler.buildscheduler.service.impl.UserProfileService;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor; // Use Lombok for constructor injection
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor // Use Lombok for constructor injection
public class UserController {

    private final UserService userService; // Keep if other methods need it
    private final UserProfileService userProfileService; // Inject the new service
    private final UserMapper userMapper; // Keep if other methods need it

    @GetMapping("/by-email")
    // Consider adding @PreAuthorize for security if this API should not be public
    public ResponseEntity<ApiResponse<UserDto>> getUserByEmail(@RequestParam @Email String email) {
        UserDto user = userService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.ofSuccess("User retrieved successfully by email", user));
    }

    @GetMapping("/by-id")
    // Consider adding @PreAuthorize for security if this API should not be public (e.g., only Admin/PM)
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@RequestParam Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.ofSuccess("User retrieved successfully by ID", user));
    }

    // New API for complete user profile by ID (can be used by managers, etc.)
    @GetMapping("/profile-by-id")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'SITE_SUPERVISOR', 'EQUIPMENT_MANAGER')") // Restrict access as needed
    public ResponseEntity<ApiResponse<FullUserProfileDto>> getFullUserProfileById(@RequestParam Long id) {
        FullUserProfileDto fullProfile = userProfileService.getFullUserProfileById(id);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Full user profile retrieved", fullProfile));
    }

    // New API for currently logged-in user's complete profile
    @GetMapping("/my-profile")
    @PreAuthorize("isAuthenticated()") // Only authenticated users can access their own profile
    public ResponseEntity<ApiResponse<FullUserProfileDto>> getMyFullProfile() {
        FullUserProfileDto fullProfile = userProfileService.getMyFullProfile();
        return ResponseEntity.ok(ApiResponse.ofSuccess("My full user profile retrieved", fullProfile));
    }

    // New API for currently logged-in user's notifications
    @GetMapping("/notifications")
    @PreAuthorize("isAuthenticated()") // Only authenticated users can access their own notifications
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getMyNotifications() {
        List<NotificationDto> notifications = userProfileService.getMyNotifications();
        return ResponseEntity.ok(ApiResponse.ofSuccess("Notifications fetched successfully", notifications));
    }


}