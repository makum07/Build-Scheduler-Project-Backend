package com.buildscheduler.buildscheduler.service.custom;

import com.buildscheduler.buildscheduler.dto.UserDto;

public interface UserService {
    UserDto registerNewUser(UserDto userDto);
}
