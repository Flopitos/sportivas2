package com.sportivas.sportivas.service;

import java.util.List;

import com.sportivas.sportivas.dto.PerformanceDto;

public interface PerformanceService {
    PerformanceDto addPerformance(PerformanceDto performanceDto);
    List<PerformanceDto> getUserPerformances();
    PerformanceDto getPerformanceById(Long id);
    PerformanceDto updatePerformance(Long id, PerformanceDto performanceDto);
    void deletePerformance(Long id);
    int getTotalPoints();
}