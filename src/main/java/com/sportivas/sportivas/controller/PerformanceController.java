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

import com.sportivas.sportivas.dto.PerformanceDto;
import com.sportivas.sportivas.service.PerformanceService;
import com.sportivas.sportivas.service.impl.ActivityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/performances")
@RequiredArgsConstructor
@Tag(name = "Performances", description = "API for managing performance tracking")
@Slf4j
public class PerformanceController {

    private final PerformanceService performanceService;
    private final ActivityService activityService;

    @Operation(summary = "Add performance", description = "Records a new performance entry for the user")
    @PostMapping
    public ResponseEntity<PerformanceDto> addPerformance(@Valid @RequestBody PerformanceDto performanceDto) {
        return new ResponseEntity<>(performanceService.addPerformance(performanceDto), HttpStatus.CREATED);
    }

    @Operation(summary = "Get all performances", description = "Returns all performance entries recorded by the user")
    @GetMapping
    public ResponseEntity<List<PerformanceDto>> getAllPerformances() {
        return new ResponseEntity<>(performanceService.getUserPerformances(), HttpStatus.OK);
    }

    @Operation(summary = "Get performance by ID", description = "Returns a specific performance entry by its ID")
    @GetMapping("/{id}")
    public ResponseEntity<PerformanceDto> getPerformanceById(@PathVariable Long id) {
        return new ResponseEntity<>(performanceService.getPerformanceById(id), HttpStatus.OK);
    }

    @Operation(summary = "Update performance", description = "Updates an existing performance entry")
    @PutMapping("/{id}")
    public ResponseEntity<PerformanceDto> updatePerformance(
            @PathVariable Long id, 
            @Valid @RequestBody PerformanceDto performanceDto) {
        return new ResponseEntity<>(performanceService.updatePerformance(id, performanceDto), HttpStatus.OK);
    }

    @Operation(summary = "Delete performance", description = "Deletes a performance entry by its ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerformance(@PathVariable Long id) {
        performanceService.deletePerformance(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @Operation(summary = "Get total points", description = "Returns the total performance points for the user")
    @GetMapping("/points")
    public ResponseEntity<Integer> getTotalPoints() {
        int totalPoints = performanceService.getTotalPoints();
        
        // Ajouter les points calculés à partir des activités
        double totalDistanceInMeters = activityService.getAllActivities().stream()
                .mapToDouble(activity -> activity.getDistance())
                .sum();
        
        // Convertir en km et multiplier par 16 points par km
        int activityPoints = (int)(totalDistanceInMeters / 1000 * 16);
        log.info("Points from activities: {} (total distance: {} meters)", activityPoints, totalDistanceInMeters);
        
        int grandTotal = totalPoints + activityPoints;
        log.info("Total points: {} (performance: {}, activities: {})", grandTotal, totalPoints, activityPoints);
        
        return new ResponseEntity<>(grandTotal, HttpStatus.OK);
    }
}