package com.sportivas.sportivas.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizSubmissionDto {
    private Long userId;
    private String feeling;
    private String fatigue;
    private String moralDouleurs;
    private String blessure;
    private String zoneDouleurs;
}