package com.sportivas.sportivas.service.impl;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.sportivas.sportivas.dto.PasswordChangeDto;
import com.sportivas.sportivas.dto.UserDto;
import com.sportivas.sportivas.dto.UserRegistrationDto;
import com.sportivas.sportivas.exception.BadRequestException;
import com.sportivas.sportivas.exception.ResourceNotFoundException;
import com.sportivas.sportivas.model.User;
import com.sportivas.sportivas.repository.UserRepository;
import com.sportivas.sportivas.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByInsuranceNumber(registrationDto.getInsuranceNumber())) {
            throw new BadRequestException("Insurance number already exists");
        }

        User user = User.builder()
                .insuranceNumber(registrationDto.getInsuranceNumber())
                .password(passwordEncoder.encode(registrationDto.getPassword()))
                .firstName(registrationDto.getFirstName())
                .lastName(registrationDto.getLastName())
                .passwordChanged(false)
                .build();

        User savedUser = userRepository.save(user);
        return mapToDto(savedUser);
    }

    @Override
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToDto(user);
    }

    @Override
    public UserDto getUserByInsuranceNumber(String insuranceNumber) {
        User user = userRepository.findByInsuranceNumber(insuranceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("User", "insuranceNumber", insuranceNumber));
        return mapToDto(user);
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());

        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    @Override
    public void changePassword(Long userId, PasswordChangeDto passwordChangeDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!passwordEncoder.matches(passwordChangeDto.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(passwordChangeDto.getNewPassword()));
        user.setPasswordChanged(true);
        userRepository.save(user);
    }
    
    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
                authentication instanceof AnonymousAuthenticationToken) {
            throw new BadRequestException("No authenticated user found");
        }
        
        return userRepository.findByInsuranceNumber(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", "insuranceNumber", authentication.getName()));
    }

    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .insuranceNumber(user.getInsuranceNumber())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }
}