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
public class PerformanceDto {
    private Long id;
    private LocalDate date;
    private Integer steps;
    private Integer caloriesBurned;
    private Double distanceKm;
    private Integer heartRate;
    private Integer performancePoints;
}