package com.sportivas.sportivas.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sportivas.sportivas.dto.SportSessionDto;
import com.sportivas.sportivas.exception.BadRequestException;
import com.sportivas.sportivas.exception.ResourceNotFoundException;
import com.sportivas.sportivas.model.Sport;
import com.sportivas.sportivas.model.SportSession;
import com.sportivas.sportivas.model.User;
import com.sportivas.sportivas.model.UserSport;
import com.sportivas.sportivas.repository.SportRepository;
import com.sportivas.sportivas.repository.SportSessionRepository;
import com.sportivas.sportivas.service.SportSessionService;
import com.sportivas.sportivas.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SportSessionServiceImpl implements SportSessionService {

    private final SportSessionRepository sportSessionRepository;
    private final SportRepository sportRepository;
    private final UserService userService;
    private final Random random = new Random();

    @Override
    public SportSessionDto addSportSession(SportSessionDto sessionDto) {
        User currentUser = userService.getCurrentUser();
        Sport sport = sportRepository.findById(sessionDto.getSportId())
                .orElseThrow(() -> new ResourceNotFoundException("Sport", "id", sessionDto.getSportId()));
        
        SportSession session = SportSession.builder()
                .user(currentUser)
                .sport(sport)
                .scheduledStart(sessionDto.getScheduledStart())
                .scheduledEnd(sessionDto.getScheduledEnd())
                .notes(sessionDto.getNotes())
                .completed(false)
                .build();
        
        SportSession savedSession = sportSessionRepository.save(session);
        return mapToDto(savedSession);
    }

    @Override
    public List<SportSessionDto> getUserSportSessions() {
        User currentUser = userService.getCurrentUser();
        return sportSessionRepository.findByUserOrderByScheduledStartDesc(currentUser).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SportSessionDto> getUpcomingSportSessions() {
        User currentUser = userService.getCurrentUser();
        return sportSessionRepository.findByUserAndScheduledStartGreaterThanEqualOrderByScheduledStart(
                currentUser, LocalDateTime.now()).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public SportSessionDto getSportSessionById(Long id) {
        User currentUser = userService.getCurrentUser();
        SportSession session = sportSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SportSession", "id", id));
        
        // Security check - ensure the session belongs to the current user
        if (!session.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("SportSession", "id", id);
        }
        
        return mapToDto(session);
    }

    @Override
    public SportSessionDto updateSportSession(Long id, SportSessionDto sessionDto) {
        User currentUser = userService.getCurrentUser();
        SportSession session = sportSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SportSession", "id", id));
        
        // Security check - ensure the session belongs to the current user
        if (!session.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("SportSession", "id", id);
        }
        
        if (sessionDto.getSportId() != null && !sessionDto.getSportId().equals(session.getSport().getId())) {
            Sport sport = sportRepository.findById(sessionDto.getSportId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sport", "id", sessionDto.getSportId()));
            session.setSport(sport);
        }
        
        session.setScheduledStart(sessionDto.getScheduledStart());
        session.setScheduledEnd(sessionDto.getScheduledEnd());
        session.setNotes(sessionDto.getNotes());
        session.setCompleted(sessionDto.isCompleted());
        
        SportSession updatedSession = sportSessionRepository.save(session);
        return mapToDto(updatedSession);
    }

    @Override
    public void deleteSportSession(Long id) {
        User currentUser = userService.getCurrentUser();
        SportSession session = sportSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SportSession", "id", id));
        
        // Security check - ensure the session belongs to the current user
        if (!session.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("SportSession", "id", id);
        }
        
        sportSessionRepository.delete(session);
    }

    @Override
    public SportSessionDto markSessionCompleted(Long id) {
        User currentUser = userService.getCurrentUser();
        SportSession session = sportSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SportSession", "id", id));
        
        // Security check - ensure the session belongs to the current user
        if (!session.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("SportSession", "id", id);
        }
        
        session.setCompleted(true);
        SportSession updatedSession = sportSessionRepository.save(session);
        return mapToDto(updatedSession);
    }

    @Override
    public String getSportSuggestion() {
        User currentUser = userService.getCurrentUser();
        
        // Récupérer les sports depuis les UserSport
        List<Sport> userSports = currentUser.getUserSports().stream()
                .map(UserSport::getSport)
                .collect(Collectors.toList());
        
        if (userSports.isEmpty()) {
            return "Veuillez ajouter des sports à votre profil pour obtenir des suggestions.";
        }
        
        // Simple MVP suggestion - just randomly pick one of user's sports
        Sport suggestedSport = userSports.get(random.nextInt(userSports.size()));
        return "We suggest you try " + suggestedSport.getName() + " today!";
    }
    
    private SportSessionDto mapToDto(SportSession session) {
        return SportSessionDto.builder()
                .id(session.getId())
                .sportId(session.getSport().getId())
                .sportName(session.getSport().getName())
                .scheduledStart(session.getScheduledStart())
                .scheduledEnd(session.getScheduledEnd())
                .notes(session.getNotes())
                .completed(session.isCompleted())
                .build();
    }
}