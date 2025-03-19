package com.sportivas.sportivas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sportivas.sportivas.dto.SportDto;
import com.sportivas.sportivas.dto.UserSportDto;
import com.sportivas.sportivas.service.SportService;
import com.sportivas.sportivas.service.impl.SportServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sports")
@RequiredArgsConstructor
@Tag(name = "Sports", description = "API for managing sports")
public class SportController {

    private final SportService sportService;

    @Operation(summary = "Get all sports", description = "Returns all available sports")
    @GetMapping
    public ResponseEntity<List<SportDto>> getAllSports() {
        return new ResponseEntity<>(sportService.getAllSports(), HttpStatus.OK);
    }

    @Operation(summary = "Get user's sports", description = "Returns all sports selected by the user with frequency and level")
    @GetMapping("/user")
    public ResponseEntity<List<UserSportDto>> getUserSports() {
        return new ResponseEntity<>(sportService.getUserSports(), HttpStatus.OK);
    }

    @Operation(summary = "Add sport to user", description = "Add a sport to user's preferences with frequency and level")
    @PostMapping("/user/{sportId}")
    public ResponseEntity<UserSportDto> addSportToUser(
            @PathVariable Long sportId,
            @RequestParam(required = false, defaultValue = "1 fois par semaine") String frequency,
            @RequestParam(required = false, defaultValue = "Débutant") String level) {
        System.out.println("Adding sport with ID: " + sportId + ", frequency: " + frequency + ", level: " + level);
        try {
            UserSportDto userSportDto = sportService.addSportToUser(sportId, frequency, level);
            System.out.println("Sport added successfully: " + userSportDto);
            return new ResponseEntity<>(userSportDto, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error adding sport: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @Operation(summary = "Add fixed sport", description = "Add a specific sport (ID 1) with fixed settings for debugging")
    @GetMapping("/debug-add")
    public ResponseEntity<UserSportDto> debugAddSport() {
        try {
            System.out.println("Debug endpoint (GET): Adding sport with ID 1 using simple method");
            // Utiliser la méthode simplifiée
            UserSportDto userSportDto = ((SportServiceImpl)sportService).addSportToUserSimple(1L);
            System.out.println("Debug endpoint (GET): Sport added successfully: " + userSportDto);
            return new ResponseEntity<>(userSportDto, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Debug endpoint (GET): Error adding sport: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @Operation(summary = "Add sport with fixed settings", description = "Add a sport with fixed default settings")
    @PostMapping("/add-default/{sportId}")
    public ResponseEntity<UserSportDto> addSportDefault(@PathVariable Long sportId) {
        System.out.println("Adding sport with default settings, ID: " + sportId);
        try {
            UserSportDto userSportDto = sportService.addSportToUser(sportId, "1 fois par semaine", "Débutant");
            System.out.println("Sport added successfully: " + userSportDto);
            return new ResponseEntity<>(userSportDto, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error adding sport: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Operation(summary = "Remove sport from user", description = "Remove a sport from user's preferences")
    @DeleteMapping("/user/{userSportId}")
    public ResponseEntity<Void> removeSportFromUser(@PathVariable Long userSportId) {
        sportService.removeSportFromUser(userSportId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Get user's sports by userId", description = "Returns all sports for a specific user ID")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserSportDto>> getUserSportsByUserId(@PathVariable Long userId) {
        return new ResponseEntity<>(sportService.getSportsByUserId(userId), HttpStatus.OK);
    }
}