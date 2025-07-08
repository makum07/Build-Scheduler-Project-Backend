package com.buildscheduler.buildscheduler.service.impl;

import com.buildscheduler.buildscheduler.dto.UserDto;
import com.buildscheduler.buildscheduler.exception.RoleNotFoundException;
import com.buildscheduler.buildscheduler.exception.UserAlreadyExistsException;
import com.buildscheduler.buildscheduler.mapper.UserMapper;
import com.buildscheduler.buildscheduler.model.Role;
import com.buildscheduler.buildscheduler.model.User;
import com.buildscheduler.buildscheduler.repository.RoleRepository;
import com.buildscheduler.buildscheduler.repository.UserRepository;
import com.buildscheduler.buildscheduler.service.custom.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RoleRepository roleRepository,
            UserMapper userMapper
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserDto registerNewUser(UserDto userDto) {
        // Check unique constraints
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new UserAlreadyExistsException("User already exists with username: " + userDto.getUsername());
        }

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered: " + userDto.getEmail());
        }

        // Convert to entity
        User user = userMapper.toEntity(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        // Assign role
        String roleName = "ROLE_" + userDto.getRole().toUpperCase().replace(" ", "_");
        Role selectedRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RoleNotFoundException("Invalid role: " + userDto.getRole()));

        user.getRoles().add(selectedRole);

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }
}