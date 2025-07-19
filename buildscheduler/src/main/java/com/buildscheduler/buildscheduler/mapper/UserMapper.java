package com.buildscheduler.buildscheduler.mapper;

import com.buildscheduler.buildscheduler.dto.auth.UserDto;
import com.buildscheduler.buildscheduler.dto.project_manager.UserTableDto;
import com.buildscheduler.buildscheduler.dto.worker.SimpleUserDto;
import com.buildscheduler.buildscheduler.model.Role;
import com.buildscheduler.buildscheduler.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper implements Mapper<User, UserDto> {

    @Override
    public UserDto toDto(User entity) {
        UserDto dto = new UserDto();
        dto.setUsername(entity.getUsername());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());

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
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        return user;
    }

    private String toTitleCase(String input) {
        if (input == null || input.isEmpty()) return input;

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

    public UserTableDto toUserTableDto(User user) {
        String role = user.getRoles().stream()
                .map(Role::getName)
                .findFirst()
                .orElse("UNKNOWN");

        return new UserTableDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                role
        );
    }

    public List<UserTableDto> toUserTableDtos(List<User> users) {
        return users.stream()
                .map(this::toUserTableDto)
                .collect(Collectors.toList());
    }

//    public FullUserProfileDto toFullProfileDto(User user) {
//        FullUserProfileDto dto = new FullUserProfileDto();
//        dto.setId(user.getId());
//        dto.setUsername(user.getUsername());
//        dto.setEmail(user.getEmail());
//        dto.setPhone(user.getPhone());
//        dto.setProfileStatus(user.getProfileStatus());
//
//        String role = user.getRoles().stream()
//                .map(Role::getName)
//                .map(r -> r.replace("ROLE_", "").replace("_", " ").toUpperCase())
//                .findFirst()
//                .orElse("UNKNOWN");
//        dto.setRole(role);
//
//        dto.setSkills(user.getSkills().stream()
//                .map(skill -> new SkillDto(skill.getId(), skill.getName()))
//                .collect(Collectors.toList()));
//
//        dto.setCertifications(user.getCertifications());
//
//        dto.setAvailabilitySlots(user.getAvailabilitySlots().stream()
//                .map(slot -> new AvailabilitySlotDto(
//                        slot.getId(),
//                        slot.getDate(),
//                        slot.getStartTime(),
//                        slot.getEndTime()
//                ))
//                .collect(Collectors.toList()));
//
//        if (user.getSiteSupervisor() != null) {
//            dto.setSiteSupervisor(new SimpleUserDto(
//                    user.getSiteSupervisor().getId(),
//                    user.getSiteSupervisor().getUsername(),
//                    user.getSiteSupervisor().getEmail()
//            ));
//        }
//
//        if (user.getProjectManager() != null) {
//            dto.setProjectManager(new SimpleUserDto(
//                    user.getProjectManager().getId(),
//                    user.getProjectManager().getUsername(),
//                    user.getProjectManager().getEmail()
//            ));
//        }
//
//        dto.setAssignments(user.getAssignments().stream()
//                .map(a -> new AssignmentDto(
//                        a.getId(),
//                        a.getMainTask().getId(),
//                        a.getMainTask().getTitle(),
//                        a.getStartTime(),
//                        a.getEndTime(),
//                        a.getStatus() != null ? a.getStatus().name() : "UNKNOWN"
//                ))
//                .collect(Collectors.toList()));
//
//        return dto;
//    }

    public SimpleUserDto toSimpleUserDto(User user) {
        if (user == null) return null;
        return new SimpleUserDto(user.getId(), user.getUsername(), user.getEmail());
    }
}
