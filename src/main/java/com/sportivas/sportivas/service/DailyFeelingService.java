package com.sportivas.sportivas.service;

import java.util.List;

import com.sportivas.sportivas.dto.DailyFeelingDto;
import com.sportivas.sportivas.model.DailyFeeling.Feeling;

public interface DailyFeelingService {
    DailyFeelingDto recordFeeling(Feeling feeling);
    DailyFeelingDto getFeelingForToday();
    List<DailyFeelingDto> getUserFeelings();
}