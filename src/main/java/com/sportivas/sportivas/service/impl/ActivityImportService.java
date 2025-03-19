package com.sportivas.sportivas.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sportivas.sportivas.model.Activity;
import com.sportivas.sportivas.repository.ActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ActivityImportService {

    private final ActivityRepository activityRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public ActivityImportService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public List<Activity> importActivitiesFromJson(String jsonContent) throws IOException {
        List<Map<String, Object>> activitiesData = objectMapper.readValue(jsonContent,
                new TypeReference<List<Map<String, Object>>>() {});

        List<Activity> importedActivities = new ArrayList<>();

        for (Map<String, Object> activityData : activitiesData) {
            Activity activity = new Activity();

            // Extraire les champs du JSON Strava
            activity.setId(((Number) activityData.get("id")).longValue());
            activity.setName((String) activityData.get("name"));
            activity.setDistance(((Number) activityData.get("distance")).doubleValue());
            activity.setMovingTime(((Number) activityData.get("moving_time")).intValue());
            activity.setElapsedTime(((Number) activityData.get("elapsed_time")).intValue());
            activity.setTotalElevationGain(((Number) activityData.get("total_elevation_gain")).doubleValue());
            activity.setType((String) activityData.get("type"));
            activity.setSportType((String) activityData.get("sport_type"));

            // Conversion des dates
            String startDateStr = (String) activityData.get("start_date");
            String startDateLocalStr = (String) activityData.get("start_date_local");
            if (startDateStr != null) {
                LocalDateTime startDate = ZonedDateTime.parse(startDateStr).toLocalDateTime();
                activity.setStartDate(startDate);
            }
            if (startDateLocalStr != null) {
                LocalDateTime startDateLocal = ZonedDateTime.parse(startDateLocalStr).toLocalDateTime();
                activity.setStartDateLocal(startDateLocal);
            }

            activity.setTimezone((String) activityData.get("timezone"));
            activity.setLocationCountry((String) activityData.get("location_country"));

            if (activityData.get("average_speed") != null) {
                activity.setAverageSpeed(((Number) activityData.get("average_speed")).doubleValue());
            }
            if (activityData.get("max_speed") != null) {
                activity.setMaxSpeed(((Number) activityData.get("max_speed")).doubleValue());
            }

            if (activityData.get("has_heartrate") != null) {
                activity.setHasHeartrate((Boolean) activityData.get("has_heartrate"));
            }
            if (activityData.get("average_heartrate") != null) {
                activity.setAverageHeartrate(((Number) activityData.get("average_heartrate")).doubleValue());
            }
            if (activityData.get("max_heartrate") != null) {
                activity.setMaxHeartrate(((Number) activityData.get("max_heartrate")).doubleValue());
            }

            importedActivities.add(activity);
        }

        // Sauvegarder toutes les activités en base de données
        return activityRepository.saveAll(importedActivities);
    }
}