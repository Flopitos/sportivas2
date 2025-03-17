package com.sportivas.sportivas.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sportivas.sportivas.model.DailyFeeling;
import com.sportivas.sportivas.model.User;

public interface DailyFeelingRepository extends JpaRepository<DailyFeeling, Long> {
    List<DailyFeeling> findByUserOrderByDateDesc(User user);
    Optional<DailyFeeling> findByUserAndDate(User user, LocalDate date);
}