package com.sportivas.sportivas.model;

import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "performances")
public class Performance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(nullable = false)
    private LocalDate date;
    
    private Integer steps;
    
    private Integer caloriesBurned;
    
    private Double distanceKm;
    
    private Integer heartRate;
    
    private Integer performancePoints;
    
    @PrePersist
    @PreUpdate
    protected void calculatePoints() {
        // Gamification logic: 1km = 16 points
        performancePoints = 0;
        
        if (steps != null) {
            performancePoints += steps / 1000;
        }
        
        if (caloriesBurned != null) {
            performancePoints += caloriesBurned / 100;
        }
        
        if (distanceKm != null) {
            // Chaque kilomètre vaut 16 points
            performancePoints += (int)(distanceKm * 16);
        }
    }
}