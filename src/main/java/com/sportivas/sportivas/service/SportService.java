package com.sportivas.sportivas.service;

import java.util.List;

import com.sportivas.sportivas.dto.SportDto;

public interface SportService {
    List<SportDto> getAllSports();
    SportDto getSportById(Long id);
    List<SportDto> getUserSports();
    void addSportToUser(Long sportId);
    void removeSportFromUser(Long sportId);
}