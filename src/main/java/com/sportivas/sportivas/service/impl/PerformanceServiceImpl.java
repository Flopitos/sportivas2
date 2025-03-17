package com.sportivas.sportivas.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sportivas.sportivas.dto.PerformanceDto;
import com.sportivas.sportivas.exception.ResourceNotFoundException;
import com.sportivas.sportivas.model.Performance;
import com.sportivas.sportivas.model.User;
import com.sportivas.sportivas.repository.PerformanceRepository;
import com.sportivas.sportivas.service.PerformanceService;
import com.sportivas.sportivas.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PerformanceServiceImpl implements PerformanceService {

    private final PerformanceRepository performanceRepository;
    private final UserService userService;

    @Override
    public PerformanceDto addPerformance(PerformanceDto performanceDto) {
        User currentUser = userService.getCurrentUser();
        
        Performance performance = Performance.builder()
                .user(currentUser)
                .date(performanceDto.getDate())
                .steps(performanceDto.getSteps())
                .caloriesBurned(performanceDto.getCaloriesBurned())
                .distanceKm(performanceDto.getDistanceKm())
                .heartRate(performanceDto.getHeartRate())
                .build();
        
        Performance savedPerformance = performanceRepository.save(performance);
        return mapToDto(savedPerformance);
    }

    @Override
    public List<PerformanceDto> getUserPerformances() {
        User currentUser = userService.getCurrentUser();
        return performanceRepository.findByUserOrderByDateDesc(currentUser).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public PerformanceDto getPerformanceById(Long id) {
        User currentUser = userService.getCurrentUser();
        Performance performance = performanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Performance", "id", id));
        
        // Security check - ensure the performance belongs to the current user
        if (!performance.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Performance", "id", id);
        }
        
        return mapToDto(performance);
    }

    @Override
    public PerformanceDto updatePerformance(Long id, PerformanceDto performanceDto) {
        User currentUser = userService.getCurrentUser();
        Performance performance = performanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Performance", "id", id));
        
        // Security check - ensure the performance belongs to the current user
        if (!performance.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Performance", "id", id);
        }
        
        performance.setDate(performanceDto.getDate());
        performance.setSteps(performanceDto.getSteps());
        performance.setCaloriesBurned(performanceDto.getCaloriesBurned());
        performance.setDistanceKm(performanceDto.getDistanceKm());
        performance.setHeartRate(performanceDto.getHeartRate());
        
        Performance updatedPerformance = performanceRepository.save(performance);
        return mapToDto(updatedPerformance);
    }

    @Override
    public void deletePerformance(Long id) {
        User currentUser = userService.getCurrentUser();
        Performance performance = performanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Performance", "id", id));
        
        // Security check - ensure the performance belongs to the current user
        if (!performance.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Performance", "id", id);
        }
        
        performanceRepository.delete(performance);
    }
    
    @Override
    public int getTotalPoints() {
        User currentUser = userService.getCurrentUser();
        return performanceRepository.findByUserOrderByDateDesc(currentUser).stream()
                .mapToInt(Performance::getPerformancePoints)
                .sum();
    }
    
    private PerformanceDto mapToDto(Performance performance) {
        return PerformanceDto.builder()
                .id(performance.getId())
                .date(performance.getDate())
                .steps(performance.getSteps())
                .caloriesBurned(performance.getCaloriesBurned())
                .distanceKm(performance.getDistanceKm())
                .heartRate(performance.getHeartRate())
                .performancePoints(performance.getPerformancePoints())
                .build();
    }
}