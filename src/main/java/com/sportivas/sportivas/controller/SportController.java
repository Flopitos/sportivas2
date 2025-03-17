package com.sportivas.sportivas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sportivas.sportivas.dto.SportDto;
import com.sportivas.sportivas.service.SportService;

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

    @Operation(summary = "Get user's sports", description = "Returns all sports selected by the user")
    @GetMapping("/user")
    public ResponseEntity<List<SportDto>> getUserSports() {
        return new ResponseEntity<>(sportService.getUserSports(), HttpStatus.OK);
    }

    @Operation(summary = "Add sport to user", description = "Add a sport to user's preferences")
    @PostMapping("/user/{sportId}")
    public ResponseEntity<Void> addSportToUser(@PathVariable Long sportId) {
        sportService.addSportToUser(sportId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Remove sport from user", description = "Remove a sport from user's preferences")
    @DeleteMapping("/user/{sportId}")
    public ResponseEntity<Void> removeSportFromUser(@PathVariable Long sportId) {
        sportService.removeSportFromUser(sportId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}