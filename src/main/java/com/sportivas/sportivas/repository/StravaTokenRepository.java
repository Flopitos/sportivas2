package com.sportivas.sportivas.repository;

import com.sportivas.sportivas.model.StravaToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StravaTokenRepository extends JpaRepository<StravaToken, Long> {

    // Trouver le token pour un utilisateur spécifique
    Optional<StravaToken> findByUserId(Long userId);

    // Supprimer les tokens d'un utilisateur
    void deleteByUserId(Long userId);
}