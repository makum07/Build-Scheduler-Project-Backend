package com.buildscheduler.buildscheduler.service.impl;

import com.buildscheduler.buildscheduler.dto.project_manager.RoleUpdateDto;
import com.buildscheduler.buildscheduler.dto.auth.UserDto;
import com.buildscheduler.buildscheduler.dto.project_manager.UserTableDto;
import com.buildscheduler.buildscheduler.exception.ResourceNotFoundException;
import com.buildscheduler.buildscheduler.exception.RoleNotFoundException;
import com.buildscheduler.buildscheduler.exception.UserAlreadyExistsException;
import com.buildscheduler.buildscheduler.mapper.UserMapper;
import com.buildscheduler.buildscheduler.model.Role;
import com.buildscheduler.buildscheduler.model.User;
import com.buildscheduler.buildscheduler.repository.RoleRepository;
import com.buildscheduler.buildscheduler.repository.UserRepository;
import com.buildscheduler.buildscheduler.service.custom.UserService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

        // Handle role assignment with default
        String roleName = (userDto.getRole() == null || userDto.getRole().isBlank())
                ? "Worker"  // Default role
                : userDto.getRole();

        String formattedRoleName = "ROLE_" + roleName.toUpperCase().replace(" ", "_");
        Role selectedRole = roleRepository.findByName(formattedRoleName)
                .orElseThrow(() -> new RoleNotFoundException("Invalid role: " + roleName));

        user.getRoles().add(selectedRole);

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUserRole(RoleUpdateDto roleUpdateDto) {
        User user = userRepository.findByEmail(roleUpdateDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + roleUpdateDto.getEmail()
                ));

        // Standardize role input: uppercase and replace spaces
        String standardizedRole = roleUpdateDto.getRole().toUpperCase().replace(" ", "_");
        String roleName = "ROLE_" + standardizedRole;

        // Find the new role
        Role newRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RoleNotFoundException("Invalid role: " + roleUpdateDto.getRole()));

        // Clear existing roles and add the new one
        user.getRoles().clear();
        user.getRoles().add(newRole);

        User updatedUser = userRepository.save(user);

        // Create DTO with standardized role
        UserDto userDto = userMapper.toDto(updatedUser);
        userDto.setRole(standardizedRole); // Set standardized role name

        return userDto;
    }

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with email: " + email
                ));

        UserDto dto = userMapper.toDto(user);

        // Standardize the role format
        if (dto.getRole() != null) {
            dto.setRole(dto.getRole().toUpperCase().replace(" ", "_"));
        }

        return dto;
    }
    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + id
                ));

        UserDto dto = userMapper.toDto(user);

        // Standardize the role format
        if (dto.getRole() != null) {
            dto.setRole(dto.getRole().toUpperCase().replace(" ", "_"));
        }

        return dto;
    }

    @Override
    public List<UserTableDto> getAllUsersSortedByCreationDate() {
        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        return users.stream()
                .map(userMapper::toUserTableDto)
                .toList();
    }
    @Override
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }



}