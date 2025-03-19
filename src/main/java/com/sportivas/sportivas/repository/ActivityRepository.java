package com.sportivas.sportivas.repository;

import com.sportivas.sportivas.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByType(String type);

    List<Activity> findByStartDateBetween(LocalDateTime start, LocalDateTime end);

    List<Activity> findByTypeAndStartDateBetween(String type, LocalDateTime start, LocalDateTime end);
}