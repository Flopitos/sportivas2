package com.sportivas.sportivas.dto;

import java.time.LocalDate;

import com.sportivas.sportivas.model.DailyFeeling.Feeling;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyFeelingDto {
    private Long id;
    private LocalDate date;
    private Feeling feeling;
}