package com.sportivas.sportivas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sportivas.sportivas.dto.SportSessionDto;
import com.sportivas.sportivas.service.SportSessionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
@Tag(name = "Sport Sessions", description = "API for managing sport sessions and planning")
public class SportSessionController {

    private final SportSessionService sportSessionService;

    @Operation(summary = "Add sport session", description = "Creates a new planned sport session")
    @PostMapping
    public ResponseEntity<SportSessionDto> addSportSession(@Valid @RequestBody SportSessionDto sessionDto) {
        return new ResponseEntity<>(sportSessionService.addSportSession(sessionDto), HttpStatus.CREATED);
    }

    @Operation(summary = "Get all sessions", description = "Returns all sport sessions for the user")
    @GetMapping
    public ResponseEntity<List<SportSessionDto>> getAllSessions() {
        return new ResponseEntity<>(sportSessionService.getUserSportSessions(), HttpStatus.OK);
    }

    @Operation(summary = "Get upcoming sessions", description = "Returns upcoming sport sessions for the user")
    @GetMapping("/upcoming")
    public ResponseEntity<List<SportSessionDto>> getUpcomingSessions() {
        return new ResponseEntity<>(sportSessionService.getUpcomingSportSessions(), HttpStatus.OK);
    }

    @Operation(summary = "Get session by ID", description = "Returns a specific sport session by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<SportSessionDto> getSessionById(@PathVariable Long id) {
        return new ResponseEntity<>(sportSessionService.getSportSessionById(id), HttpStatus.OK);
    }

    @Operation(summary = "Update session", description = "Updates an existing sport session")
    @PutMapping("/{id}")
    public ResponseEntity<SportSessionDto> updateSession(
            @PathVariable Long id, 
            @Valid @RequestBody SportSessionDto sessionDto) {
        return new ResponseEntity<>(sportSessionService.updateSportSession(id, sessionDto), HttpStatus.OK);
    }

    @Operation(summary = "Delete session", description = "Deletes a sport session by its ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        sportSessionService.deleteSportSession(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @Operation(summary = "Mark session as completed", description = "Marks a sport session as completed")
    @PostMapping("/{id}/complete")
    public ResponseEntity<SportSessionDto> markSessionCompleted(@PathVariable Long id) {
        return new ResponseEntity<>(sportSessionService.markSessionCompleted(id), HttpStatus.OK);
    }
    
    @Operation(summary = "Get sport suggestion", description = "Returns a suggested sport for the user")
    @GetMapping("/suggestion")
    public ResponseEntity<String> getSportSuggestion() {
        return new ResponseEntity<>(sportSessionService.getSportSuggestion(), HttpStatus.OK);
    }
}