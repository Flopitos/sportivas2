package com.sportivas.sportivas.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sportivas.sportivas.dto.SportDto;
import com.sportivas.sportivas.exception.ResourceNotFoundException;
import com.sportivas.sportivas.model.Sport;
import com.sportivas.sportivas.model.User;
import com.sportivas.sportivas.repository.SportRepository;
import com.sportivas.sportivas.repository.UserRepository;
import com.sportivas.sportivas.service.SportService;
import com.sportivas.sportivas.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SportServiceImpl implements SportService {

    private final SportRepository sportRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public List<SportDto> getAllSports() {
        return sportRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public SportDto getSportById(Long id) {
        Sport sport = sportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sport", "id", id));
        return mapToDto(sport);
    }

    @Override
    public List<SportDto> getUserSports() {
        User user = userService.getCurrentUser();
        return user.getSports().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public void addSportToUser(Long sportId) {
        User user = userService.getCurrentUser();
        Sport sport = sportRepository.findById(sportId)
                .orElseThrow(() -> new ResourceNotFoundException("Sport", "id", sportId));
        
        user.getSports().add(sport);
        userRepository.save(user);
    }

    @Override
    public void removeSportFromUser(Long sportId) {
        User user = userService.getCurrentUser();
        Sport sport = sportRepository.findById(sportId)
                .orElseThrow(() -> new ResourceNotFoundException("Sport", "id", sportId));
        
        user.getSports().remove(sport);
        userRepository.save(user);
    }
    
    private SportDto mapToDto(Sport sport) {
        return SportDto.builder()
                .id(sport.getId())
                .name(sport.getName())
                .description(sport.getDescription())
                .build();
    }
}