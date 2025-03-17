package com.sportivas.sportivas.service;

import com.sportivas.sportivas.dto.PasswordChangeDto;
import com.sportivas.sportivas.dto.UserDto;
import com.sportivas.sportivas.dto.UserRegistrationDto;
import com.sportivas.sportivas.model.User;

public interface UserService {
    UserDto registerUser(UserRegistrationDto registrationDto);
    UserDto getUserById(Long id);
    UserDto getUserByInsuranceNumber(String insuranceNumber);
    UserDto updateUser(Long id, UserDto userDto);
    void changePassword(Long userId, PasswordChangeDto passwordChangeDto);
    User getCurrentUser();
}