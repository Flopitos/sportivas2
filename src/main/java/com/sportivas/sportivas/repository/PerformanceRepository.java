package com.sportivas.sportivas.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sportivas.sportivas.model.Performance;
import com.sportivas.sportivas.model.User;

public interface PerformanceRepository extends JpaRepository<Performance, Long> {
    List<Performance> findByUserOrderByDateDesc(User user);
    Optional<Performance> findByUserAndDate(User user, LocalDate date);
}