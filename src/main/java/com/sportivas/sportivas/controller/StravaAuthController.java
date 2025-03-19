package com.sportivas.sportivas.controller;

import com.sportivas.sportivas.service.impl.StravaService;
import com.sportivas.sportivas.service.impl.StravaTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/strava/auth")
public class StravaAuthController {
    private static final Logger logger = LoggerFactory.getLogger(StravaAuthController.class);

    @Value("${strava.client.id}")
    private String clientId;

    @Value("${strava.redirect.uri}")
    private String redirectUri;

    private final StravaTokenService stravaTokenService;
    private final StravaService stravaService;

    public StravaAuthController(
            StravaTokenService stravaTokenService,
            StravaService stravaService) {
        this.stravaTokenService = stravaTokenService;
        this.stravaService = stravaService;
    }

    /**
     * Endpoint pour initialiser le flux d'authentification Strava
     * Redirige l'utilisateur vers la page d'autorisation Strava
     */
    @GetMapping("/authorize")
    public RedirectView authorize(HttpSession session) {
        // Dans une application réelle, vous récupéreriez l'ID de l'utilisateur
        // à partir de la session ou du token d'authentification
        Long userId = getCurrentUserId(session);

        logger.info("Initialisation du flux d'authentification Strava pour l'utilisateur {}", userId);

        String stravaAuthUrl = "https://www.strava.com/oauth/authorize" +
                "?client_id=" + clientId +
                "&response_type=code" +
                "&redirect_uri=" + redirectUri +
                "&approval_prompt=force" +
                "&scope=activity:read_all";

        // Stocker l'ID de l'utilisateur dans la session pour le récupérer dans le callback
        session.setAttribute("strava_auth_user_id", userId);

        return new RedirectView(stravaAuthUrl);
    }

    /**
     * Endpoint pour recevoir le code d'autorisation après que l'utilisateur
     * ait autorisé l'application sur Strava
     */
    @GetMapping("/callback")
    public RedirectView callback(
            @RequestParam("code") String code,
            @RequestParam(value = "error", required = false) String error,
            HttpSession session) {

        // Récupérer l'ID de l'utilisateur depuis la session
        Long userId = (Long) session.getAttribute("strava_auth_user_id");
        if (userId == null) {
            logger.error("Aucun ID utilisateur trouvé dans la session pour le callback Strava");
            return new RedirectView("/static/strava-connection-failed.html");
        }

        if (error != null) {
            logger.warn("L'utilisateur {} a refusé l'autorisation Strava: {}", userId, error);
            return new RedirectView("/static/strava-connection-failed.html");
        }

        try {
            // Échanger le code d'autorisation contre un token d'accès
            logger.info("Échange du code d'autorisation pour l'utilisateur {}", userId);
            stravaTokenService.exchangeAuthorizationCode(code, userId);

            // Récupérer immédiatement les activités pour les afficher à l'utilisateur
            int activitiesCount = stravaService.forceSynchronize(userId);

            // Enregistrer en session le nombre d'activités importées
            session.setAttribute("strava_imported_activities_count", activitiesCount);

            logger.info("Connexion Strava réussie pour l'utilisateur {}, {} activités importées",
                    userId, activitiesCount);

            // Rediriger vers une page de succès
            return new RedirectView("/static/strava-connected.html");
        } catch (Exception e) {
            logger.error("Erreur lors de l'échange du code d'autorisation pour l'utilisateur {}", userId, e);
            return new RedirectView("/static/strava-connection-failed.html");
        }
    }

    /**
     * Endpoint pour vérifier l'état de connexion à Strava
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus(HttpSession session) {
        Long userId = getCurrentUserId(session);

        Map<String, Object> response = new HashMap<>();
        boolean isConnected = stravaTokenService.hasValidToken(userId);

        response.put("connected", isConnected);

        // Si connecté, ajouter des informations supplémentaires
        if (isConnected) {
            Integer importedActivitiesCount = (Integer) session.getAttribute("strava_imported_activities_count");
            if (importedActivitiesCount != null) {
                response.put("activitiesCount", importedActivitiesCount);
            }
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint pour déconnecter l'application de Strava
     */
    @PostMapping("/disconnect")
    public ResponseEntity<Map<String, Object>> disconnect(HttpSession session) {
        Long userId = getCurrentUserId(session);

        logger.info("Déconnexion de Strava pour l'utilisateur {}", userId);
        stravaTokenService.disconnectUser(userId);

        // Nettoyer les informations de session
        session.removeAttribute("strava_imported_activities_count");

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Déconnecté de Strava avec succès");

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