package com.sportivas.sportivas.dto;

import java.time.LocalDate;

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
    private String bodyPart;
    private String painType;
    private LocalDate injuryDate;
    private LocalDate recoveryDate;
    private String description;
}