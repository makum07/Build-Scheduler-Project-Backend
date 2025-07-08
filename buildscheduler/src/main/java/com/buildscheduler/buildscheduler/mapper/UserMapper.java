package com.buildscheduler.buildscheduler.mapper;

import com.buildscheduler.buildscheduler.dto.UserDto;
import com.buildscheduler.buildscheduler.model.Role;
import com.buildscheduler.buildscheduler.model.User;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UserMapper implements Mapper<User, UserDto> {

    @Override
    public UserDto toDto(User entity) {
        UserDto dto = new UserDto();
        dto.setUsername(entity.getUsername());

        // Convert roles to display format (remove ROLE_ prefix and convert to title case)
        String roles = entity.getRoles().stream()
                .map(role -> role.getName().replace("ROLE_", ""))
                .map(this::toTitleCase)
                .collect(Collectors.joining(", "));

        dto.setRole(roles);
        return dto;
    }

    @Override
    public User toEntity(UserDto dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        // Password will be set separately after encoding
        return user;
    }

    private String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder titleCase = new StringBuilder();
        boolean convertNext = true;

        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                convertNext = true;
            } else if (convertNext) {
                c = Character.toTitleCase(c);
                convertNext = false;
            } else {
                c = Character.toLowerCase(c);
            }
            titleCase.append(c);
        }

        return titleCase.toString();
    }
}