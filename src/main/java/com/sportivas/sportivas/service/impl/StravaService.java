package com.sportivas.sportivas.service.impl;

import com.sportivas.sportivas.dto.ActivityDTO;
import com.sportivas.sportivas.model.Activity;
import com.sportivas.sportivas.repository.ActivityRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class StravaService {
    private static final Logger logger = LoggerFactory.getLogger(StravaService.class);

    @Value("${strava.api.url}")
    private String stravaApiUrl;

    private final StravaTokenService tokenService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ActivityService activityService;
    private final ActivityRepository activityRepository;

    // Map pour stocker le timestamp de dernière synchronisation par utilisateur
    private final Map<Long, Long> lastSyncTimestampByUser = new HashMap<>();

    public StravaService(
            StravaTokenService tokenService,
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            ActivityService activityService,
            ActivityRepository activityRepository) {
        this.tokenService = tokenService;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.activityService = activityService;
        this.activityRepository = activityRepository;
    }

    /**
     * Récupère les activités Strava de l'utilisateur
     * @param userId ID de l'utilisateur
     * @param page Numéro de page
     * @param perPage Nombre d'activités par page
     * @return Liste des activités
     */
    public List<ActivityDTO> getStravaActivities(Long userId, int page, int perPage) {
        try {
            if (!tokenService.hasValidToken(userId)) {
                logger.warn("Pas de token valide pour l'utilisateur {}", userId);
                return new ArrayList<>();
            }

            String url = stravaApiUrl + "/athlete/activities?page=" + page + "&per_page=" + perPage;

            HttpEntity<String> entity = new HttpEntity<>(tokenService.getAuthorizationHeaders(userId));
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class);

            JsonNode activitiesNode = objectMapper.readTree(response.getBody());
            List<ActivityDTO> activities = new ArrayList<>();

            for (JsonNode activityNode : activitiesNode) {
                ActivityDTO activity = mapJsonToActivityDTO(activityNode);
                activities.add(activity);
            }

            return activities;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                logger.warn("Token non autorisé pour l'utilisateur {}, tentative de rafraîchissement", userId);
                boolean refreshed = tokenService.refreshAccessToken(userId);
                if (refreshed) {
                    // Réessayer après rafraîchissement du token
                    return getStravaActivities(userId, page, perPage);
                }
            }
            logger.error("Erreur HTTP lors de la récupération des activités Strava: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des activités Strava", e);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des activités Strava: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des activités Strava", e);
        }
    }

    /**
     * Synchronise les activités Strava avec la base de données locale
     * Cette méthode est exécutée pour un utilisateur spécifique
     * @param userId ID de l'utilisateur
     * @return Nombre d'activités synchronisées
     */
    public int synchronizeUserActivities(Long userId) {
        logger.info("Début de la synchronisation des activités Strava pour l'utilisateur {}", userId);
        int count = 0;

        try {
            if (!tokenService.hasValidToken(userId)) {
                logger.warn("Pas de token valide pour l'utilisateur {}", userId);
                return 0;
            }

            // Obtenir le timestamp de dernière synchronisation ou 0 si première synchro
            long lastSync = lastSyncTimestampByUser.getOrDefault(userId, 0L);
            String url = stravaApiUrl + "/athlete/activities";

            // Ajouter le paramètre after si ce n'est pas la première synchronisation
            if (lastSync > 0) {
                url += "?after=" + lastSync;
            }

            HttpEntity<String> entity = new HttpEntity<>(tokenService.getAuthorizationHeaders(userId));
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class);

            JsonNode activitiesNode = objectMapper.readTree(response.getBody());

            for (JsonNode activityNode : activitiesNode) {
                ActivityDTO activity = mapJsonToActivityDTO(activityNode);
                // Vérifier si cette activité existe déjà
                if (activity.getStravaId() != null) {
                    Optional<Activity> existingActivity = activityRepository.findByStravaId(activity.getStravaId());
                    if (existingActivity.isPresent()) {
                        logger.debug("Activité Strava {} déjà existante, mise à jour", activity.getStravaId());
                    } else {
                        count++; // Incrémenter seulement pour les nouvelles activités
                    }
                }

                activityService.saveActivity(activity); // Cette méthode gère les doublons via stravaId
            }

            // Mettre à jour le timestamp de dernière synchronisation
            lastSyncTimestampByUser.put(userId, Instant.now().getEpochSecond());
            logger.info("Synchronisation terminée pour l'utilisateur {} : {} nouvelles activités importées", userId, count);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                logger.warn("Token non autorisé pour l'utilisateur {}, tentative de rafraîchissement", userId);
                boolean refreshed = tokenService.refreshAccessToken(userId);
                if (refreshed) {
                    // Réessayer après rafraîchissement du token
                    return synchronizeUserActivities(userId);
                }
            }
            logger.error("Erreur HTTP lors de la synchronisation des activités Strava: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Erreur lors de la synchronisation des activités Strava: {}", e.getMessage());
        }

        return count;
    }

    /**
     * Synchronisation planifiée pour tous les utilisateurs qui ont un token valide
     * Cette méthode est exécutée toutes les 15 minutes
     */
    @Scheduled(fixedRateString = "${strava.sync.interval:900000}")
    public void scheduledSynchronization() {
        logger.info("Début de la synchronisation planifiée pour tous les utilisateurs");
        List<Long> usersWithStravaToken = getUsersWithStravaToken();

        for (Long userId : usersWithStravaToken) {
            try {
                int count = synchronizeUserActivities(userId);
                logger.info("Synchronisation automatique pour utilisateur {} : {} activités", userId, count);
            } catch (Exception e) {
                logger.error("Erreur lors de la synchronisation automatique pour l'utilisateur {}: {}", userId, e.getMessage());
            }
        }
    }

    /**
     * Force une synchronisation manuelle des activités Strava pour un utilisateur
     * @param userId ID de l'utilisateur
     * @return Nombre d'activités synchronisées
     */
    public int forceSynchronize(Long userId) {
        // Réinitialiser le timestamp pour récupérer toutes les activités
        lastSyncTimestampByUser.remove(userId);
        return synchronizeUserActivities(userId);
    }

    /**
     * Convertit un nœud JSON d'activité Strava en ActivityDTO
     */
    private ActivityDTO mapJsonToActivityDTO(JsonNode activityNode) {
        ActivityDTO activity = new ActivityDTO();

        // Récupérer l'ID Strava pour éviter les doublons
        activity.setStravaId(activityNode.path("id").asLong());

        // Données de base
        activity.setName(activityNode.path("name").asText());
        activity.setType(activityNode.path("type").asText());
        activity.setDistance(activityNode.path("distance").asDouble());

        // Données supplémentaires
        // Convertir la date ISO 8601 en LocalDateTime
        String startDateStr = activityNode.path("start_date").asText();
        if (startDateStr != null && !startDateStr.isEmpty()) {
            try {
                // Strava utilise le format ISO-8601 avec 'Z' pour UTC
                // Enlever le 'Z' et parser directement
                LocalDateTime date = LocalDateTime.parse(startDateStr.replace("Z", ""));
                activity.setStartDate(date);
            } catch (DateTimeParseException e) {
                logger.warn("Erreur lors de la conversion de la date: {}", startDateStr, e);
                // Fallback: utiliser l'instant epoch pour éviter les erreurs
                try {
                    Instant instant = Instant.parse(startDateStr);
                    activity.setStartDate(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));
                } catch (Exception ex) {
                    logger.error("Impossible de parser la date: {}", startDateStr, ex);
                }
            }
        }

        activity.setMovingTime(activityNode.path("moving_time").asInt());
        activity.setAverageSpeed(activityNode.path("average_speed").asDouble());
        activity.setMaxSpeed(activityNode.path("max_speed").asDouble());

        // Données cardiaques si disponibles
        if (activityNode.has("has_heartrate") && activityNode.path("has_heartrate").asBoolean()) {
            activity.setHasHeartrate(true);
            activity.setAverageHeartrate(activityNode.path("average_heartrate").asDouble());
        } else {
            activity.setHasHeartrate(false);
        }

        // Dénivelé
        activity.setTotalElevationGain(activityNode.path("total_elevation_gain").asDouble());

        return activity;
    }

    /**
     * Méthode pour récupérer les IDs des utilisateurs qui ont un token Strava
     * @return Liste d'IDs utilisateurs ayant un token Strava valide
     */
    private List<Long> getUsersWithStravaToken() {
        // Cette méthode doit être adaptée selon votre modèle de données
        // Idéalement, elle devrait appeler un repository pour récupérer les utilisateurs avec token

        // Pour cet exemple, nous créons une liste simple
        List<Long> userIds = new ArrayList<>();

        // Dans une application réelle, vous utiliseriez quelque chose comme:
        // return stravaTokenRepository.findUserIdsWithValidTokens();

        // Pour simplifier, nous ajoutons l'ID 1
        userIds.add(1L);
        return userIds;
    }
}