package com.sportivas.sportivas.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SportSessionDto {
    private Long id;
    private Long sportId;
    private String sportName;
    private LocalDateTime scheduledStart;
    private LocalDateTime scheduledEnd;
    private String notes;
    private boolean completed;
}