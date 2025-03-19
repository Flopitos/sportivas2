package com.sportivas.sportivas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSportDto {
    private Long id;
    private Long sportId;
    private String sportName;
    private String sportDescription;
    private String frequency;
    private String level;
}