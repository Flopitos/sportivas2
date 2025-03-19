package com.sportivas.sportivas.controller;

import com.sportivas.sportivas.dto.ActivityDTO;
import com.sportivas.sportivas.service.impl.StravaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/strava/activities")
public class StravaActivityController {
    private static final Logger logger = LoggerFactory.getLogger(StravaActivityController.class);

    private final StravaService stravaService;

    public StravaActivityController(StravaService stravaService) {
        this.stravaService = stravaService;
    }

    /**
     * Récupère les activités Strava de l'utilisateur
     */
    @GetMapping
    public ResponseEntity<List<ActivityDTO>> getStravaActivities(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "per_page", defaultValue = "30") int perPage,
            HttpSession session) {

        Long userId = getCurrentUserId(session);

        logger.info("Récupération des activités Strava pour l'utilisateur {}", userId);
        List<ActivityDTO> activities = stravaService.getStravaActivities(userId, page, perPage);

        return ResponseEntity.ok(activities);
    }

    /**
     * Lance une synchronisation manuelle des activités Strava
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> synchronizeActivities(HttpSession session) {
        Long userId = getCurrentUserId(session);

        logger.info("Synchronisation manuelle des activités Strava pour l'utilisateur {}", userId);
        int count = stravaService.forceSynchronize(userId);

        // Stocker le nombre d'activités importées dans la session
        session.setAttribute("strava_imported_activities_count", count);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Synchronisation réussie");
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    /**
     * Méthode utilitaire pour récupérer l'ID de l'utilisateur actuel
     * Dans une application réelle, cela viendrait de votre système d'authentification
     */
    private Long getCurrentUserId(HttpSession session) {
        // Pour simplifier, nous utilisons un ID utilisateur statique
        // Dans une application réelle, vous récupéreriez cet ID à partir de la session
        // ou d'un token JWT d'authentification
        return 1L;
    }
}