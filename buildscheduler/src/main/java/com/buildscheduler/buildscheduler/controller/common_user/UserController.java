package com.buildscheduler.buildscheduler.controller.common_user;

import com.buildscheduler.buildscheduler.dto.auth.FullUserProfileDto;
import com.buildscheduler.buildscheduler.dto.auth.UserDto;
import com.buildscheduler.buildscheduler.mapper.UserMapper;
import com.buildscheduler.buildscheduler.model.User;
import com.buildscheduler.buildscheduler.response.ApiResponse;
import com.buildscheduler.buildscheduler.service.custom.UserService;
import jakarta.validation.constraints.Email;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/by-email")
    public ResponseEntity<ApiResponse<UserDto>> getUserByEmail(@RequestParam @Email String email) {
        UserDto user = userService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.ofSuccess("User retrieved successfully by email", user));
    }

    @GetMapping("/by-id")
    public ResponseEntity<ApiResponse<UserDto>> getUserById(@RequestParam Long id) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.ofSuccess("User retrieved successfully by ID", user));
    }

    @GetMapping("/profile-by-id")
    public ResponseEntity<ApiResponse<FullUserProfileDto>> getFullUserProfileById(@RequestParam Long id) {
        User user = userService.getUserEntityById(id);
        FullUserProfileDto fullProfile = userMapper.toFullProfileDto(user);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Full user profile retrieved", fullProfile));
    }
}
