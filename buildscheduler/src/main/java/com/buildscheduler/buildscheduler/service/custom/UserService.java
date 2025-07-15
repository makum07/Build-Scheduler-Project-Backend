package com.buildscheduler.buildscheduler.service.custom;

import com.buildscheduler.buildscheduler.dto.project_manager.RoleUpdateDto;
import com.buildscheduler.buildscheduler.dto.auth.UserDto;
import com.buildscheduler.buildscheduler.dto.project_manager.UserTableDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;

import java.util.List;

public interface UserService {
    UserDto registerNewUser(UserDto userDto);

    UserDto updateUserRole(@Valid RoleUpdateDto roleUpdateDto);

    UserDto getUserByEmail(@Email String email);

    List<UserTableDto> getAllUsersSortedByCreationDate();
}
