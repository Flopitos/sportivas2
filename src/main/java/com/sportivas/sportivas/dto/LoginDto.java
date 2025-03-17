package com.sportivas.sportivas.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {
    @NotBlank(message = "Insurance number is required")
    private String insuranceNumber;
    
    @NotBlank(message = "Password is required")
    private String password;
}