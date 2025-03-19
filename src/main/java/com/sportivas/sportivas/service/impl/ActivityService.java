package com.sportivas.sportivas.service.impl;

import com.sportivas.sportivas.dto.ActivityDTO;
import com.sportivas.sportivas.model.Activity;
import com.sportivas.sportivas.repository.ActivityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    @Autowired
    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public List<ActivityDTO> getAllActivities() {
        return activityRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ActivityDTO getActivityById(Long id) {
        return activityRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public List<ActivityDTO> getActivitiesByType(String type) {
        return activityRepository.findByType(type).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ActivityDTO> getActivitiesBetweenDates(LocalDateTime start, LocalDateTime end) {
        return activityRepository.findByStartDateBetween(start, end).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ActivityDTO saveActivity(ActivityDTO activityDTO) {
        Activity activity = convertToEntity(activityDTO);
        activity = activityRepository.save(activity);
        return convertToDTO(activity);
    }

    public void deleteActivity(Long id) {
        activityRepository.deleteById(id);
    }

    // Méthodes de conversion
    private ActivityDTO convertToDTO(Activity activity) {
        ActivityDTO dto = new ActivityDTO();
        dto.setId(activity.getId());
        dto.setName(activity.getName());
        dto.setDistance(activity.getDistance());
        dto.setType(activity.getType());
        return dto;
    }

    private Activity convertToEntity(ActivityDTO dto) {
        Activity activity = new Activity();
        activity.setId(dto.getId());
        activity.setName(dto.getName());
        activity.setDistance(dto.getDistance());
        activity.setType(dto.getType());
        return activity;
    }
}