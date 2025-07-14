package com.buildscheduler.buildscheduler.controller.common_user;

import com.buildscheduler.buildscheduler.dto.auth.UserDto;
import com.buildscheduler.buildscheduler.response.ApiResponse;
import com.buildscheduler.buildscheduler.service.custom.UserService;
import jakarta.validation.constraints.Email;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<UserDto>> getUserByEmail(@RequestParam @Email String email) {
        UserDto user = userService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.ofSuccess("User retrieved successfully", user));
    }

}