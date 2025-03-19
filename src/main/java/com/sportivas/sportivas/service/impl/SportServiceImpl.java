package com.sportivas.sportivas.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sportivas.sportivas.dto.SportDto;
import com.sportivas.sportivas.dto.UserSportDto;
import com.sportivas.sportivas.exception.ResourceNotFoundException;
import com.sportivas.sportivas.model.Sport;
import com.sportivas.sportivas.model.User;
import com.sportivas.sportivas.model.UserSport;
import com.sportivas.sportivas.repository.SportRepository;
import com.sportivas.sportivas.repository.UserRepository;
import com.sportivas.sportivas.repository.UserSportRepository;
import com.sportivas.sportivas.service.SportService;
import com.sportivas.sportivas.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SportServiceImpl implements SportService {

    private final SportRepository sportRepository;
    private final UserRepository userRepository;
    private final UserSportRepository userSportRepository;
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
    public List<UserSportDto> getUserSports() {
        User user = userService.getCurrentUser();
        return userSportRepository.findByUser(user).stream()
                .map(this::mapToUserSportDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserSportDto addSportToUser(Long sportId, String frequency, String level) {
        try {
            User user = userService.getCurrentUser();
            Sport sport = sportRepository.findById(sportId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sport", "id", sportId));
            
            // Check if user already has this sport
            UserSport existingUserSport = userSportRepository.findByUserAndSport(user, sport).orElse(null);
            
            if (existingUserSport != null) {
                // Update existing record
                existingUserSport.setFrequency(frequency);
                existingUserSport.setLevel(level);
                return mapToUserSportDto(userSportRepository.save(existingUserSport));
            } else {
                // Create new record
                UserSport userSport = UserSport.builder()
                        .user(user)
                        .sport(sport)
                        .frequency(frequency)
                        .level(level)
                        .build();
                
                return mapToUserSportDto(userSportRepository.save(userSport));
            }
        } catch (Exception e) {
            System.err.println("Service error adding sport: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    // Méthode simplifiée sans vérification d'existence
    public UserSportDto addSportToUserSimple(Long sportId) {
        try {
            System.out.println("Simple add sport method: sportId=" + sportId);
            User user = userService.getCurrentUser();
            System.out.println("User found: " + user.getId());
            
            Sport sport = sportRepository.findById(sportId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sport", "id", sportId));
            System.out.println("Sport found: " + sport.getName());
            
            // Créer directement sans vérifier s'il existe déjà
            UserSport userSport = new UserSport();
            userSport.setUser(user);
            userSport.setSport(sport);
            userSport.setFrequency("1 fois par semaine");
            userSport.setLevel("Débutant");
            
            UserSport savedUserSport = userSportRepository.save(userSport);
            System.out.println("UserSport saved with ID: " + savedUserSport.getId());
            
            return mapToUserSportDto(savedUserSport);
        } catch (Exception e) {
            System.err.println("Error in addSportToUserSimple: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void removeSportFromUser(Long userSportId) {
        UserSport userSport = userSportRepository.findById(userSportId)
                .orElseThrow(() -> new ResourceNotFoundException("UserSport", "id", userSportId));
        
        // Check if the current user owns this user-sport relationship
        User currentUser = userService.getCurrentUser();
        if (!userSport.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("UserSport", "id", userSportId);
        }
        
        userSportRepository.delete(userSport);
    }
    
    private SportDto mapToDto(Sport sport) {
        return SportDto.builder()
                .id(sport.getId())
                .name(sport.getName())
                .description(sport.getDescription())
                .build();
    }
    
    private UserSportDto mapToUserSportDto(UserSport userSport) {
        return UserSportDto.builder()
                .id(userSport.getId())
                .sportId(userSport.getSport().getId())
                .sportName(userSport.getSport().getName())
                .sportDescription(userSport.getSport().getDescription())
                .frequency(userSport.getFrequency())
                .level(userSport.getLevel())
                .build();
    }
}