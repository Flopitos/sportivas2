package com.sportivas.sportivas.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sportivas.sportivas.model.SportSession;
import com.sportivas.sportivas.model.User;

public interface SportSessionRepository extends JpaRepository<SportSession, Long> {
    List<SportSession> findByUserOrderByScheduledStartDesc(User user);
    List<SportSession> findByUserAndScheduledStartGreaterThanEqualOrderByScheduledStart(User user, LocalDateTime dateTime);
}