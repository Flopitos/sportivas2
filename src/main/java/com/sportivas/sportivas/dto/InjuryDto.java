package com.sportivas.sportivas.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InjuryDto {
    private Long id;
    
    @NotBlank(message = "La zone du corps est obligatoire")
    private String bodyPart;
    
    @NotBlank(message = "Le type de douleur est obligatoire")
    private String painType;
    
    @NotNull(message = "La date de blessure est obligatoire")
    @PastOrPresent(message = "La date de blessure ne peut pas être dans le futur")
    private LocalDate injuryDate;
    
    private LocalDate recoveryDate;
    
    private String description;
}