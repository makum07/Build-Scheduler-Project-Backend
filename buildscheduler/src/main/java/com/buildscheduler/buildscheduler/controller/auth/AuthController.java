package com.buildscheduler.buildscheduler.controller.auth;

import com.buildscheduler.buildscheduler.dto.auth.JwtAuthRequest;
import com.buildscheduler.buildscheduler.dto.auth.JwtAuthResponse;
import com.buildscheduler.buildscheduler.dto.auth.UserDto;
import com.buildscheduler.buildscheduler.response.ApiResponse;
import com.buildscheduler.buildscheduler.security.JwtTokenHelper;
import com.buildscheduler.buildscheduler.service.custom.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    public AuthController(AuthenticationManager authenticationManager, JwtTokenHelper jwtTokenHelper,
                          UserDetailsService userDetailsService, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenHelper = jwtTokenHelper;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> login(@Valid @RequestBody JwtAuthRequest request) {
        this.authenticate(request.getEmail(), request.getPassword());
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(request.getEmail());
        String token = this.jwtTokenHelper.generateToken(userDetails);
        JwtAuthResponse response = new JwtAuthResponse();
        response.setToken(token);
        return ResponseEntity.ok(ApiResponse.ofSuccess("Login successful", response));
    }

    private void authenticate(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(@Valid @RequestBody UserDto userDto) {
        UserDto registeredUser = userService.registerNewUser(userDto);
        return new ResponseEntity<>(ApiResponse.ofSuccess("User registered successfully", registeredUser), HttpStatus.CREATED);
    }
}