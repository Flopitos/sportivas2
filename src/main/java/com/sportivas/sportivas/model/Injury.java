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
@Table(name = "injuries")
public class Injury {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(nullable = false)
    private String bodyPart;
    
    @Column(nullable = false)
    private String painType;
    
    @Column(nullable = false)
    private LocalDate injuryDate;
    
    private LocalDate recoveryDate;
    
    private String description;
}