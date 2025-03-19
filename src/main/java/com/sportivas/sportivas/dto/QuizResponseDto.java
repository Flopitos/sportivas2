package com.sportivas.sportivas.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponseDto {
    private Long id;
    private Long userId;
    private String feeling;
    private String fatigueType;
    private String hasPain;
    private String injuryType;
    private String painLocation;
    private LocalDateTime createdAt;
}

