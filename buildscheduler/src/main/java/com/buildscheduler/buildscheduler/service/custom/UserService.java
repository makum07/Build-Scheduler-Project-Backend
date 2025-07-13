package com.buildscheduler.buildscheduler.service.custom;

import com.buildscheduler.buildscheduler.dto.RoleUpdateDto;
import com.buildscheduler.buildscheduler.dto.UserDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;

public interface UserService {
    UserDto registerNewUser(UserDto userDto);

    UserDto updateUserRole(@Valid RoleUpdateDto roleUpdateDto);

    UserDto getUserByEmail(@Email String email);
}
