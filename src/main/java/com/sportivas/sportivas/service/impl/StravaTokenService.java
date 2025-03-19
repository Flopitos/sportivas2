package com.sportivas.sportivas.service.impl;

import com.sportivas.sportivas.dto.StravaTokenDTO;
import com.sportivas.sportivas.model.StravaToken;
import com.sportivas.sportivas.repository.StravaTokenRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

@Service
public class StravaTokenService {
    private static final Logger logger = LoggerFactory.getLogger(StravaTokenService.class);

    @Value("${strava.client.id}")
    private String clientId;

    @Value("${strava.client.secret}")
    private String clientSecret;

    @Value("${strava.redirect.uri}")
    private String redirectUri;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final StravaTokenRepository stravaTokenRepository;

    // Injecter les dépendances via le constructeur
    public StravaTokenService(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            StravaTokenRepository stravaTokenRepository) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.stravaTokenRepository = stravaTokenRepository;
    }

    /**
     * Échanger le code d'autorisation contre un token d'accès
     * @param code Le code d'autorisation fourni par Strava
     * @param userId L'ID de l'utilisateur dans notre système
     * @return Le token d'accès
     */
    public String exchangeAuthorizationCode(String code, Long userId) {
        try {
            logger.info("Échange du code d'autorisation pour l'utilisateur {}", userId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("client_id", clientId);
            map.add("client_secret", clientSecret);
            map.add("code", code);
            map.add("grant_type", "authorization_code");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://www.strava.com/oauth/token",
                    request,
                    String.class);

            // Analyser la réponse
            JsonNode root = objectMapper.readTree(response.getBody());

            // Créer ou mettre à jour le token dans la base de données
            StravaToken stravaToken = new StravaToken();
            stravaToken.setUserId(userId);
            stravaToken.setAccessToken(root.path("access_token").asText());
            stravaToken.setRefreshToken(root.path("refresh_token").asText());
            stravaToken.setExpiresAt(root.path("expires_at").asLong());

            // Récupérer l'ID de l'athlète si disponible
            if (root.has("athlete") && root.path("athlete").has("id")) {
                stravaToken.setAthleteId(root.path("athlete").path("id").asLong());
            }

            // Chercher un token existant pour cet utilisateur
            Optional<StravaToken> existingToken = stravaTokenRepository.findByUserId(userId);
            if (existingToken.isPresent()) {
                // Mettre à jour le token existant
                StravaToken token = existingToken.get();
                token.setAccessToken(stravaToken.getAccessToken());
                token.setRefreshToken(stravaToken.getRefreshToken());
                token.setExpiresAt(stravaToken.getExpiresAt());
                if (stravaToken.getAthleteId() != null) {
                    token.setAthleteId(stravaToken.getAthleteId());
                }
                stravaTokenRepository.save(token);
            } else {
                // Enregistrer le nouveau token
                stravaTokenRepository.save(stravaToken);
            }

            return stravaToken.getAccessToken();
        } catch (Exception e) {
            logger.error("Erreur lors de l'échange du code d'autorisation", e);
            throw new RuntimeException("Erreur lors de l'échange du code d'autorisation", e);
        }
    }

    /**
     * Obtenir le token d'accès pour un utilisateur
     * Si le token a expiré, il sera automatiquement rafraîchi
     * @param userId L'ID de l'utilisateur
     * @return Le token d'accès valide ou null si aucun token n'existe
     */
    public String getAccessToken(Long userId) {
        Optional<StravaToken> tokenOpt = stravaTokenRepository.findByUserId(userId);

        if (tokenOpt.isEmpty()) {
            logger.warn("Aucun token Strava trouvé pour l'utilisateur {}", userId);
            return null;
        }

        StravaToken token = tokenOpt.get();

        // Vérifier si le token a expiré
        if (token.isExpired()) {
            logger.info("Token expiré pour l'utilisateur {}, rafraîchissement en cours", userId);
            refreshAccessToken(userId);
            // Récupérer le token à nouveau après le rafraîchissement
            return stravaTokenRepository.findByUserId(userId)
                    .map(StravaToken::getAccessToken)
                    .orElse(null);
        }

        return token.getAccessToken();
    }

    /**
     * Rafraîchir le token d'accès pour un utilisateur
     * @param userId L'ID de l'utilisateur
     * @return true si le rafraîchissement a réussi, false sinon
     */
    public boolean refreshAccessToken(Long userId) {
        try {
            Optional<StravaToken> tokenOpt = stravaTokenRepository.findByUserId(userId);
            if (tokenOpt.isEmpty()) {
                logger.warn("Impossible de rafraîchir le token : aucun token trouvé pour l'utilisateur {}", userId);
                return false;
            }

            StravaToken token = tokenOpt.get();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("client_id", clientId);
            map.add("client_secret", clientSecret);
            map.add("refresh_token", token.getRefreshToken());
            map.add("grant_type", "refresh_token");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://www.strava.com/oauth/token",
                    request,
                    String.class);

            // Analyser la réponse
            JsonNode root = objectMapper.readTree(response.getBody());

            // Mettre à jour le token
            token.setAccessToken(root.path("access_token").asText());
            token.setRefreshToken(root.path("refresh_token").asText());
            token.setExpiresAt(root.path("expires_at").asLong());

            stravaTokenRepository.save(token);

            logger.info("Token rafraîchi avec succès pour l'utilisateur {}", userId);
            return true;
        } catch (Exception e) {
            logger.error("Erreur lors du rafraîchissement du token pour l'utilisateur {}", userId, e);
            return false;
        }
    }

    /**
     * Vérifier si un utilisateur a un token Strava valide
     * @param userId L'ID de l'utilisateur
     * @return true si l'utilisateur a un token valide, false sinon
     */
    public boolean hasValidToken(Long userId) {
        return stravaTokenRepository.findByUserId(userId)
                .map(token -> !token.isExpired())
                .orElse(false);
    }

    /**
     * Déconnecter un utilisateur de Strava
     * @param userId L'ID de l'utilisateur
     */
    public void disconnectUser(Long userId) {
        logger.info("Déconnexion de l'utilisateur {} de Strava", userId);
        stravaTokenRepository.deleteByUserId(userId);
    }

    /**
     * Obtenir les en-têtes HTTP avec le token d'authentification
     * @param userId L'ID de l'utilisateur
     * @return Les en-têtes HTTP avec le token d'authentification
     */
    public HttpHeaders getAuthorizationHeaders(Long userId) {
        String accessToken = getAccessToken(userId);
        if (accessToken == null) {
            throw new RuntimeException("Aucun token d'accès valide trouvé pour l'utilisateur " + userId);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }
}