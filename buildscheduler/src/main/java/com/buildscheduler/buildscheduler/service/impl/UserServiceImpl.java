package com.buildscheduler.buildscheduler.service.impl;

import com.buildscheduler.buildscheduler.dto.UserDto;
import com.buildscheduler.buildscheduler.exception.RoleNotFoundException;
import com.buildscheduler.buildscheduler.exception.UserAlreadyExistsException;
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

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RoleRepository roleRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDto registerNewUser(UserDto userDto) {
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new UserAlreadyExistsException("User already exists with username: " + userDto.getUsername());
        }

        // Convert role to database format
        String roleName = "ROLE_" + userDto.getRole().toUpperCase().replace(" ", "_");

        Role selectedRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RoleNotFoundException("Invalid role selected: " + userDto.getRole()));

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.getRoles().add(selectedRole);

        User savedUser = userRepository.save(user);

        // Return DTO with role information
        return new UserDto(
                savedUser.getUsername(),
                null,
                userDto.getRole() // Return the original role name
        );
    }
}