package com.sportivas.sportivas.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sportivas.sportivas.dto.DailyFeelingDto;
import com.sportivas.sportivas.model.DailyFeeling;
import com.sportivas.sportivas.model.DailyFeeling.Feeling;
import com.sportivas.sportivas.model.User;
import com.sportivas.sportivas.repository.DailyFeelingRepository;
import com.sportivas.sportivas.service.DailyFeelingService;
import com.sportivas.sportivas.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DailyFeelingServiceImpl implements DailyFeelingService {

    private final DailyFeelingRepository dailyFeelingRepository;
    private final UserService userService;

    @Override
    public DailyFeelingDto recordFeeling(Feeling feeling) {
        User currentUser = userService.getCurrentUser();
        LocalDate today = LocalDate.now();
        
        DailyFeeling dailyFeeling = dailyFeelingRepository.findByUserAndDate(currentUser, today)
                .orElse(DailyFeeling.builder().user(currentUser).date(today).build());
        
        dailyFeeling.setFeeling(feeling);
        dailyFeeling = dailyFeelingRepository.save(dailyFeeling);
        
        return mapToDto(dailyFeeling);
    }

    @Override
    public DailyFeelingDto getFeelingForToday() {
        User currentUser = userService.getCurrentUser();
        LocalDate today = LocalDate.now();
        
        return dailyFeelingRepository.findByUserAndDate(currentUser, today)
                .map(this::mapToDto)
                .orElse(null);
    }

    @Override
    public List<DailyFeelingDto> getUserFeelings() {
        User currentUser = userService.getCurrentUser();
        return dailyFeelingRepository.findByUserOrderByDateDesc(currentUser).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    private DailyFeelingDto mapToDto(DailyFeeling dailyFeeling) {
        return DailyFeelingDto.builder()
                .id(dailyFeeling.getId())
                .date(dailyFeeling.getDate())
                .feeling(dailyFeeling.getFeeling())
                .build();
    }
}