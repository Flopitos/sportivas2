package com.sportivas.sportivas.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sportivas.sportivas.dto.DailyFeelingDto;
import com.sportivas.sportivas.model.DailyFeeling.Feeling;
import com.sportivas.sportivas.service.DailyFeelingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/feelings")
@RequiredArgsConstructor
@Tag(name = "Daily Feelings", description = "API for managing daily mood feelings")
public class DailyFeelingController {

    private final DailyFeelingService dailyFeelingService;

    @Operation(summary = "Record feeling", description = "Records the user's current feeling for today")
    @PostMapping
    public ResponseEntity<DailyFeelingDto> recordFeeling(@RequestParam Feeling feeling) {
        return new ResponseEntity<>(dailyFeelingService.recordFeeling(feeling), HttpStatus.CREATED);
    }

    @Operation(summary = "Get today's feeling", description = "Gets the user's feeling for today if recorded")
    @GetMapping("/today")
    public ResponseEntity<DailyFeelingDto> getTodayFeeling() {
        DailyFeelingDto feeling = dailyFeelingService.getFeelingForToday();
        return feeling != null ? 
                new ResponseEntity<>(feeling, HttpStatus.OK) : 
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Get all feelings", description = "Gets all of the user's recorded feelings")
    @GetMapping
    public ResponseEntity<List<DailyFeelingDto>> getAllFeelings() {
        return new ResponseEntity<>(dailyFeelingService.getUserFeelings(), HttpStatus.OK);
    }
}