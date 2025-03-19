package com.sportivas.sportivas.repository;

import com.sportivas.sportivas.model.QuizResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<QuizResponse, Long> {

    List<QuizResponse> findByUserId(Long userId);

    @Query("SELECT q FROM QuizResponse q WHERE q.userId = :userId AND q.createdAt BETWEEN :startDate AND :endDate")
    List<QuizResponse> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT q FROM QuizResponse q WHERE q.userId = :userId ORDER BY q.createdAt DESC LIMIT 1")
    Optional<QuizResponse> findLatestByUserId(@Param("userId") Long userId);
}