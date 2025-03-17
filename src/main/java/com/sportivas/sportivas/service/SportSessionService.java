package com.sportivas.sportivas.service;

import java.util.List;

import com.sportivas.sportivas.dto.SportSessionDto;

public interface SportSessionService {
    SportSessionDto addSportSession(SportSessionDto sessionDto);
    List<SportSessionDto> getUserSportSessions();
    List<SportSessionDto> getUpcomingSportSessions();
    SportSessionDto getSportSessionById(Long id);
    SportSessionDto updateSportSession(Long id, SportSessionDto sessionDto);
    void deleteSportSession(Long id);
    SportSessionDto markSessionCompleted(Long id);
    String getSportSuggestion();
}