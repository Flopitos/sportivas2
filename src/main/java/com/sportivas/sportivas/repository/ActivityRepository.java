package com.sportivas.sportivas.repository;

import com.sportivas.sportivas.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByType(String type);

    List<Activity> findByStartDateBetween(LocalDateTime start, LocalDateTime end);

    List<Activity> findByTypeAndStartDateBetween(String type, LocalDateTime start, LocalDateTime end);

    // Méthode pour trouver une activité par son ID Strava
    Optional<Activity> findByStravaId(Long stravaId);
}