package com.sportivas.sportivas.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sportivas.sportivas.service.ActivityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/performances")
@RequiredArgsConstructor
@Tag(name = "Performances", description = "API for viewing fitness activities and performance metrics")
public class PerformanceController {

    private final ActivityService activityService;

    @Operation(summary = "Get all activities", description = "Returns all fitness activities from Strava integration")
    @GetMapping("/activities")
    public ResponseEntity<List<Map<String, Object>>> getAllActivities() {
        return new ResponseEntity<>(activityService.getAllActivities(), HttpStatus.OK);
    }

    @Operation(summary = "Get summary statistics", description = "Returns performance summary data highlighting steps and calories")
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummaryStats() {
        return new ResponseEntity<>(activityService.getSummaryStats(), HttpStatus.OK);
    }
}