package com.sportivas.sportivas.service;

import java.util.List;

import com.sportivas.sportivas.dto.SportDto;
import com.sportivas.sportivas.dto.UserSportDto;

public interface SportService {
    List<SportDto> getAllSports();
    SportDto getSportById(Long id);
    List<UserSportDto> getUserSports();
    UserSportDto addSportToUser(Long sportId, String frequency, String level);
    void removeSportFromUser(Long userSportId);
}