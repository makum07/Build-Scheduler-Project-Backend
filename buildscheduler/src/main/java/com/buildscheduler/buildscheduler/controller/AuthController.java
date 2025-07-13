package com.buildscheduler.buildscheduler.controller;

import com.buildscheduler.buildscheduler.dto.JwtAuthRequest;
import com.buildscheduler.buildscheduler.dto.JwtAuthResponse;
import com.buildscheduler.buildscheduler.dto.RoleUpdateDto;
import com.buildscheduler.buildscheduler.dto.UserDto;
import com.buildscheduler.buildscheduler.response.ApiResponse;
import com.buildscheduler.buildscheduler.security.JwtTokenHelper;
import com.buildscheduler.buildscheduler.service.custom.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenHelper jwtTokenHelper;
    private final UserDetailsService userDetailsService;
    private final UserService userService;

    // Constructor injection
    public AuthController(
            AuthenticationManager authenticationManager,
            JwtTokenHelper jwtTokenHelper,
            UserDetailsService userDetailsService,
            UserService userService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenHelper = jwtTokenHelper;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> login(@Valid @RequestBody JwtAuthRequest request) {
        this.authenticate(request.getEmail(), request.getPassword());
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(request.getEmail()); // uses email now
        String token = this.jwtTokenHelper.generateToken(userDetails);

        JwtAuthResponse response = new JwtAuthResponse();
        response.setToken(token);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Login successful", response));
    }

    private void authenticate(String email, String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(@Valid @RequestBody UserDto userDto) {
        UserDto registeredUser = userService.registerNewUser(userDto);
        return new ResponseEntity<>(
                ApiResponse.ofSuccess("User registered successfully", registeredUser),
                HttpStatus.CREATED
        );
    }

    @PostMapping("/update-role")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    public ResponseEntity<ApiResponse<UserDto>> updateUserRole(
            @Valid @RequestBody RoleUpdateDto roleUpdateDto) {
        UserDto updatedUser = userService.updateUserRole(roleUpdateDto);
        return ResponseEntity.ok(
                ApiResponse.ofSuccess("User role updated successfully", updatedUser)
        );
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<UserDto>> getUserByEmail(
            @RequestParam @Email String email) {
        UserDto user = userService.getUserByEmail(email);
        return ResponseEntity.ok(
                ApiResponse.ofSuccess("User retrieved successfully", user)
        );
    }

}