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

import com.sportivas.sportivas.dto.InjuryDto;
import com.sportivas.sportivas.service.InjuryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/injuries")
@RequiredArgsConstructor
@Tag(name = "Injuries", description = "API for managing injuries")
public class InjuryController {

    private final InjuryService injuryService;

    @Operation(summary = "Add injury", description = "Records a new injury for the user")
    @PostMapping
    public ResponseEntity<InjuryDto> addInjury(@Valid @RequestBody InjuryDto injuryDto) {
        return new ResponseEntity<>(injuryService.addInjury(injuryDto), HttpStatus.CREATED);
    }

    @Operation(summary = "Get all injuries", description = "Returns all injuries recorded by the user")
    @GetMapping
    public ResponseEntity<List<InjuryDto>> getAllInjuries() {
        return new ResponseEntity<>(injuryService.getUserInjuries(), HttpStatus.OK);
    }

    @Operation(summary = "Get injury by ID", description = "Returns a specific injury by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<InjuryDto> getInjuryById(@PathVariable Long id) {
        return new ResponseEntity<>(injuryService.getInjuryById(id), HttpStatus.OK);
    }

    @Operation(summary = "Update injury", description = "Updates an existing injury")
    @PutMapping("/{id}")
    public ResponseEntity<InjuryDto> updateInjury(
            @PathVariable Long id, 
            @Valid @RequestBody InjuryDto injuryDto) {
        return new ResponseEntity<>(injuryService.updateInjury(id, injuryDto), HttpStatus.OK);
    }

    @Operation(summary = "Delete injury", description = "Deletes an injury by its ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInjury(@PathVariable Long id) {
        injuryService.deleteInjury(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}