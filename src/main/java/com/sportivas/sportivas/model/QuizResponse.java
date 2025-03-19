package com.sportivas.sportivas.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_responses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "feeling")
    private String feeling;

    @Column(name = "fatigue_type")
    private String fatigueType;

    @Column(name = "has_pain")
    private String hasPain;

    @Column(name = "injury_type")
    private String injuryType;

    @Column(name = "pain_location")
    private String painLocation;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}