package com.sportivas.sportivas.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportivas.sportivas.service.ActivityService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ActivityServiceImpl implements ActivityService {

    private static final String ACTIVITIES_FILE = "activites.json";
    private final ObjectMapper objectMapper;

    public ActivityServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Map<String, Object>> getAllActivities() {
        try {
            List<Map<String, Object>> activities = readActivitiesFromJson();
            
            // Enhance activities with additional calculated data
            activities.forEach(activity -> {
                // Calculate duration in minutes
                if (activity.containsKey("moving_time")) {
                    int movingTimeSeconds = (int) activity.get("moving_time");
                    activity.put("duration_minutes", Math.round(movingTimeSeconds / 60.0));
                }
                
                // Convert distance from meters to kilometers
                if (activity.containsKey("distance")) {
                    double distanceMeters = ((Number) activity.get("distance")).doubleValue();
                    activity.put("distance_km", Math.round(distanceMeters / 100) / 10.0);
                }
                
                // Add estimated calories if missing based on activity type and duration
                if (!activity.containsKey("calories")) {
                    String type = (String) activity.getOrDefault("sport_type", "Other");
                    int durationMinutes = (int) activity.getOrDefault("duration_minutes", 0);
                    int calories = estimateCalories(type, durationMinutes);
                    activity.put("calories", calories);
                }
                
                // Add estimated steps if missing based on activity type and distance
                if (!activity.containsKey("steps")) {
                    String type = (String) activity.getOrDefault("sport_type", "Other");
                    double distanceKm = (double) activity.getOrDefault("distance_km", 0.0);
                    int steps = estimateSteps(type, distanceKm);
                    activity.put("steps", steps);
                }
            });
            
            return activities;
        } catch (IOException e) {
            throw new RuntimeException("Error reading activities file", e);
        }
    }
    
    @Override
    public Map<String, Object> getSummaryStats() {
        List<Map<String, Object>> activities = getAllActivities();
        
        Map<String, Object> summary = new HashMap<>();
        
        // Calculate total metrics
        int totalCalories = activities.stream()
                .mapToInt(a -> ((Number) a.getOrDefault("calories", 0)).intValue())
                .sum();
                
        int totalSteps = activities.stream()
                .mapToInt(a -> ((Number) a.getOrDefault("steps", 0)).intValue())
                .sum();
                
        double totalDistance = activities.stream()
                .mapToDouble(a -> ((Number) a.getOrDefault("distance_km", 0.0)).doubleValue())
                .sum();
        
        int totalDuration = activities.stream()
                .mapToInt(a -> ((Number) a.getOrDefault("duration_minutes", 0)).intValue())
                .sum();
        
        // Count activity types
        Map<String, Long> activityTypes = activities.stream()
                .collect(Collectors.groupingBy(
                        a -> (String) a.getOrDefault("sport_type", "Other"),
                        Collectors.counting()
                ));
        
        // Group activities by date (yyyy-MM-dd)
        Map<String, List<Map<String, Object>>> activitiesByDate = activities.stream()
                .collect(Collectors.groupingBy(a -> {
                    String startDate = (String) a.get("start_date");
                    LocalDate date = Instant.parse(startDate)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    return date.toString();
                }));
        
        // Add all stats to summary
        summary.put("totalCalories", totalCalories);
        summary.put("totalSteps", totalSteps);
        summary.put("totalDistance", Math.round(totalDistance * 10) / 10.0);
        summary.put("totalDuration", totalDuration);
        summary.put("activityCount", activities.size());
        summary.put("activityTypes", activityTypes);
        
        // Calculate daily stats
        Map<String, Map<String, Object>> dailyStats = new HashMap<>();
        activitiesByDate.forEach((date, dateActivities) -> {
            Map<String, Object> dateSummary = new HashMap<>();
            
            int dateCalories = dateActivities.stream()
                    .mapToInt(a -> ((Number) a.getOrDefault("calories", 0)).intValue())
                    .sum();
                    
            int dateSteps = dateActivities.stream()
                    .mapToInt(a -> ((Number) a.getOrDefault("steps", 0)).intValue())
                    .sum();
                    
            dateSummary.put("calories", dateCalories);
            dateSummary.put("steps", dateSteps);
            dateSummary.put("activityCount", dateActivities.size());
            
            dailyStats.put(date, dateSummary);
        });
        
        summary.put("dailyStats", dailyStats);
        
        return summary;
    }
    
    private List<Map<String, Object>> readActivitiesFromJson() throws IOException {
        ClassPathResource resource = new ClassPathResource(ACTIVITIES_FILE);
        
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, new TypeReference<List<Map<String, Object>>>() {});
        }
    }
    
    private int estimateCalories(String activityType, int durationMinutes) {
        // Simple estimation based on activity type and duration
        switch (activityType.toLowerCase()) {
            case "run":
                return durationMinutes * 10; // ~600 calories per hour
            case "ride":
            case "cycling": 
                return durationMinutes * 8;  // ~480 calories per hour
            case "swim":
                return durationMinutes * 9;  // ~540 calories per hour
            case "walk":
                return durationMinutes * 5;  // ~300 calories per hour
            default:
                return durationMinutes * 6;  // ~360 calories per hour
        }
    }
    
    private int estimateSteps(String activityType, double distanceKm) {
        // Simple estimation based on activity type and distance
        switch (activityType.toLowerCase()) {
            case "run":
                return (int) (distanceKm * 1400); // ~1400 steps per km
            case "walk":
                return (int) (distanceKm * 1300); // ~1300 steps per km
            default:
                return 0; // No steps for other activities
        }
    }
}