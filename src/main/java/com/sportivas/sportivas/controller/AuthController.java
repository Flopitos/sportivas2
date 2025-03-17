package com.sportivas.sportivas.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sportivas.sportivas.dto.LoginDto;
import com.sportivas.sportivas.dto.PasswordChangeDto;
import com.sportivas.sportivas.dto.UserDto;
import com.sportivas.sportivas.dto.UserRegistrationDto;
import com.sportivas.sportivas.exception.BadRequestException;
import com.sportivas.sportivas.model.User;
import com.sportivas.sportivas.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API for user authentication and registration")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    @Operation(summary = "Register a new user", description = "Creates a new user account with the provided details")
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
        return new ResponseEntity<>(userService.registerUser(registrationDto), HttpStatus.CREATED);
    }

    @Operation(summary = "User login", description = "Authenticates a user with their credentials")
    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@Valid @RequestBody LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getInsuranceNumber(), loginDto.getPassword()));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        return new ResponseEntity<>(userService.getUserByInsuranceNumber(loginDto.getInsuranceNumber()), HttpStatus.OK);
    }

    @Operation(summary = "Get current user", description = "Returns details of the currently authenticated user")
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        User currentUser = userService.getCurrentUser();
        return new ResponseEntity<>(userService.getUserById(currentUser.getId()), HttpStatus.OK);
    }

    @Operation(summary = "Change password", description = "Changes the user's password")
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody PasswordChangeDto passwordChangeDto) {
        User currentUser = userService.getCurrentUser();
        
        if (!currentUser.isPasswordChanged()) {
            userService.changePassword(currentUser.getId(), passwordChangeDto);
            return new ResponseEntity<>("Password changed successfully", HttpStatus.OK);
        } else {
            throw new BadRequestException("Password has already been changed");
        }
    }
}