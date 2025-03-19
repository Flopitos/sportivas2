package com.sportivas.sportivas.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sportivas.sportivas.model.Sport;
import com.sportivas.sportivas.model.User;
import com.sportivas.sportivas.repository.SportRepository;
import com.sportivas.sportivas.service.UserService;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/simple")
@RequiredArgsConstructor
public class SimpleController {

    private final UserService userService;
    private final SportRepository sportRepository;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/add-sport/{sportId}")
    public ResponseEntity<Map<String, Object>> addSport(
            @PathVariable Long sportId,
            @RequestParam(required = false, defaultValue = "1 fois par semaine") String frequency,
            @RequestParam(required = false, defaultValue = "Débutant") String level) {
        try {
            System.out.println("SimpleController: Adding sport with ID " + sportId + ", frequency: " + frequency + ", level: " + level);
            
            // Utiliser un ID utilisateur fixe pour le mode démo
            Long userId = 1L; // Utiliser l'ID de l'utilisateur de démo
            
            Sport sport = sportRepository.findById(sportId).orElse(null);
            
            if (sport == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Sport not found with ID: " + sportId);
                return ResponseEntity.badRequest().body(response);
            }
            
            // Vérifier si l'utilisateur a déjà ce sport
            String checkSql = "SELECT COUNT(*) FROM user_sports WHERE user_id = ? AND sport_id = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, userId, sportId);
            
            if (count != null && count > 0) {
                // L'utilisateur a déjà ce sport, on le met à jour
                String updateSql = "UPDATE user_sports SET frequency = ?, level = ? WHERE user_id = ? AND sport_id = ?";
                jdbcTemplate.update(updateSql, frequency, level, userId, sportId);
            } else {
                // Insérer directement dans la base de données
                String sql = "INSERT INTO user_sports (user_id, sport_id, frequency, level) VALUES (?, ?, ?, ?)";
                jdbcTemplate.update(sql, userId, sportId, frequency, level);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sport ajouté avec succès");
            response.put("sportName", sport.getName());
            response.put("userId", userId);
            response.put("sportId", sportId);
            response.put("frequency", frequency);
            response.put("level", level);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/list-sports")
    public ResponseEntity<Map<String, Object>> listSports() {
        try {
            // Utiliser un ID utilisateur fixe pour le mode démo
            Long userId = 1L; // Utiliser l'ID de l'utilisateur de démo
            
            // Liste des sports de l'utilisateur
            String sql = "SELECT us.id, s.name, us.frequency, us.level FROM user_sports us " +
                        "JOIN sports s ON us.sport_id = s.id " +
                        "WHERE us.user_id = ?";
            
            var userSports = jdbcTemplate.queryForList(sql, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userSports", userSports);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/remove-sport/{userSportId}")
    public ResponseEntity<Map<String, Object>> removeSport(@PathVariable Long userSportId) {
        try {
            // Utiliser un ID utilisateur fixe pour le mode démo
            Long userId = 1L; // Utiliser l'ID de l'utilisateur de démo
            
            // Vérifier que l'utilisateur possède bien ce sport
            String checkSql = "SELECT COUNT(*) FROM user_sports WHERE id = ? AND user_id = ?";
            int count = jdbcTemplate.queryForObject(checkSql, Integer.class, userSportId, userId);
            
            if (count == 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Sport non trouvé ou non autorisé");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Supprimer le sport
            String sql = "DELETE FROM user_sports WHERE id = ?";
            jdbcTemplate.update(sql, userSportId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sport supprimé avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/add-injury")
    public ResponseEntity<Map<String, Object>> addInjury(
            @RequestParam String bodyPart,
            @RequestParam String painType,
            @RequestParam String injuryDate,
            @RequestParam(required = false) String recoveryDate,
            @RequestParam(required = false) String description) {
        
        try {
            System.out.println("SimpleController: Adding injury with bodyPart: " + bodyPart 
                    + ", painType: " + painType 
                    + ", injuryDate: " + injuryDate 
                    + ", recoveryDate: " + recoveryDate);
            
            // Utiliser un ID utilisateur fixe pour le mode démo
            Long userId = 1L;
            
            // Parser les dates
            LocalDate parsedInjuryDate = LocalDate.parse(injuryDate);
            LocalDate parsedRecoveryDate = recoveryDate != null && !recoveryDate.isEmpty() 
                    ? LocalDate.parse(recoveryDate) 
                    : null;
            
            // Insérer directement dans la base de données
            String sql;
            Object[] params;
            
            if (parsedRecoveryDate != null) {
                sql = "INSERT INTO injuries (user_id, body_part, pain_type, injury_date, recovery_date, description) VALUES (?, ?, ?, ?, ?, ?)";
                params = new Object[] { userId, bodyPart, painType, parsedInjuryDate, parsedRecoveryDate, description };
            } else {
                sql = "INSERT INTO injuries (user_id, body_part, pain_type, injury_date, description) VALUES (?, ?, ?, ?, ?)";
                params = new Object[] { userId, bodyPart, painType, parsedInjuryDate, description };
            }
            
            jdbcTemplate.update(sql, params);
            
            // Récupérer les infos de la blessure ajoutée (dernière blessure de l'utilisateur)
            String selectSql = "SELECT * FROM injuries WHERE user_id = ? ORDER BY id DESC LIMIT 1";
            Map<String, Object> injury = jdbcTemplate.queryForMap(selectSql, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Blessure ajoutée avec succès");
            response.put("injury", injury);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/list-injuries")
    public ResponseEntity<Map<String, Object>> listInjuries() {
        try {
            // Utiliser un ID utilisateur fixe pour le mode démo
            Long userId = 1L;
            
            // Liste des blessures de l'utilisateur
            String sql = "SELECT * FROM injuries WHERE user_id = ? ORDER BY injury_date DESC";
            List<Map<String, Object>> injuries = jdbcTemplate.queryForList(sql, userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("injuries", injuries);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/remove-injury/{injuryId}")
    public ResponseEntity<Map<String, Object>> removeInjury(@PathVariable Long injuryId) {
        try {
            // Utiliser un ID utilisateur fixe pour le mode démo
            Long userId = 1L;
            
            // Vérifier que l'utilisateur possède bien cette blessure
            String checkSql = "SELECT COUNT(*) FROM injuries WHERE id = ? AND user_id = ?";
            int count = jdbcTemplate.queryForObject(checkSql, Integer.class, injuryId, userId);
            
            if (count == 0) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Blessure non trouvée ou non autorisée");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Supprimer la blessure
            String sql = "DELETE FROM injuries WHERE id = ?";
            jdbcTemplate.update(sql, injuryId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Blessure supprimée avec succès");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/performance-points")
    public ResponseEntity<Map<String, Object>> getPerformancePoints() {
        try {
            // Utiliser un ID utilisateur fixe pour le mode démo
            Long userId = 1L;
            
            // Récupérer la distance totale des activités
            String activitiesSql = "SELECT SUM(distance) as total_distance FROM activities";
            Double totalDistance = jdbcTemplate.queryForObject(activitiesSql, Double.class);
            
            if (totalDistance == null) {
                totalDistance = 0.0;
            }
            
            // Convertir en km et calculer les points (16 points par km)
            double distanceInKm = totalDistance / 1000;
            int pointsFromActivities = (int) Math.round(distanceInKm * 16);
            
            // Récupérer les points des performances si la table existe
            int pointsFromPerformances = 0;
            try {
                String performancesSql = "SELECT SUM(performance_points) as total_points FROM performances WHERE user_id = ?";
                Integer perfPoints = jdbcTemplate.queryForObject(performancesSql, Integer.class, userId);
                if (perfPoints != null) {
                    pointsFromPerformances = perfPoints;
                }
            } catch (Exception e) {
                System.out.println("Table performances may not exist yet: " + e.getMessage());
            }
            
            int totalPoints = pointsFromActivities + pointsFromPerformances;
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalPoints", totalPoints);
            response.put("pointsFromActivities", pointsFromActivities);
            response.put("pointsFromPerformances", pointsFromPerformances);
            response.put("totalDistanceInKm", distanceInKm);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/activities-summary")
    public ResponseEntity<Map<String, Object>> getActivitiesSummary() {
        try {
            // Récupérer le nombre total d'activités
            String countSql = "SELECT COUNT(*) FROM activities";
            Integer activityCount = jdbcTemplate.queryForObject(countSql, Integer.class);
            
            // Récupérer la distance totale
            String distanceSql = "SELECT SUM(distance) FROM activities";
            Double totalDistance = jdbcTemplate.queryForObject(distanceSql, Double.class);
            
            // Récupérer la durée totale
            String durationSql = "SELECT SUM(moving_time) FROM activities";
            Integer totalDuration = jdbcTemplate.queryForObject(durationSql, Integer.class);
            
            // Récupérer le dénivelé total
            String elevationSql = "SELECT SUM(total_elevation_gain) FROM activities";
            Double totalElevation = jdbcTemplate.queryForObject(elevationSql, Double.class);
            
            // Récupérer les types d'activités et leur nombre
            String typesSql = "SELECT type, COUNT(*) as count FROM activities GROUP BY type";
            List<Map<String, Object>> activityTypesMaps = jdbcTemplate.queryForList(typesSql);
            
            // Convertir la liste de maps en map simple pour le frontend
            Map<String, Integer> activityTypes = new HashMap<>();
            for (Map<String, Object> typeMap : activityTypesMaps) {
                String type = (String) typeMap.get("type");
                Integer count = ((Number) typeMap.get("count")).intValue();
                activityTypes.put(type, count);
            }
            
            // Construire la réponse
            Map<String, Object> summary = new HashMap<>();
            summary.put("activityCount", activityCount != null ? activityCount : 0);
            summary.put("totalDistance", totalDistance != null ? totalDistance : 0);
            summary.put("totalDuration", totalDuration != null ? totalDuration : 0);
            summary.put("totalElevation", totalElevation != null ? totalElevation : 0);
            summary.put("activityTypes", activityTypes);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("summary", summary);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/activities")
    public ResponseEntity<Map<String, Object>> getActivities() {
        try {
            // Récupérer toutes les activités, formatées pour l'affichage
            String sql = "SELECT * FROM activities ORDER BY start_date DESC";
            List<Map<String, Object>> rawActivities = jdbcTemplate.queryForList(sql);
            
            List<Map<String, Object>> formattedActivities = new ArrayList<>();
            
            // Formater chaque activité pour l'affichage
            for (Map<String, Object> rawActivity : rawActivities) {
                Map<String, Object> activity = new HashMap<>();
                
                // Copier et formater les champs principaux
                activity.put("id", rawActivity.get("id"));
                activity.put("name", rawActivity.get("name"));
                activity.put("type", rawActivity.get("type"));
                activity.put("sportType", rawActivity.get("sport_type"));
                
                // Formater la distance (en mètres)
                Double distance = (Double) rawActivity.get("distance");
                activity.put("distance", distance != null ? distance : 0);
                
                // Formater les durées (en secondes)
                Integer movingTime = (Integer) rawActivity.get("moving_time");
                Integer elapsedTime = (Integer) rawActivity.get("elapsed_time");
                activity.put("movingTime", movingTime != null ? movingTime : 0);
                activity.put("elapsedTime", elapsedTime != null ? elapsedTime : 0);
                
                // Formater le dénivelé
                Double elevationGain = (Double) rawActivity.get("total_elevation_gain");
                activity.put("totalElevationGain", elevationGain != null ? elevationGain : 0);
                
                // Formater les dates
                Object startDateObj = rawActivity.get("start_date");
                String startDateStr = startDateObj != null ? startDateObj.toString() : null;
                activity.put("startDate", startDateStr);
                
                Object startDateLocalObj = rawActivity.get("start_date_local");
                String startDateLocalStr = startDateLocalObj != null ? startDateLocalObj.toString() : null;
                activity.put("startDateLocal", startDateLocalStr);
                
                // Formater les vitesses
                Double avgSpeed = (Double) rawActivity.get("average_speed");
                Double maxSpeed = (Double) rawActivity.get("max_speed");
                activity.put("averageSpeed", avgSpeed != null ? avgSpeed : 0);
                activity.put("maxSpeed", maxSpeed != null ? maxSpeed : 0);
                
                // Formater les données de fréquence cardiaque
                Boolean hasHeartrate = (Boolean) rawActivity.get("has_heartrate");
                activity.put("hasHeartrate", hasHeartrate != null ? hasHeartrate : false);
                
                Double avgHeartrate = (Double) rawActivity.get("average_heartrate");
                Double maxHeartrate = (Double) rawActivity.get("max_heartrate");
                activity.put("averageHeartrate", avgHeartrate);
                activity.put("maxHeartrate", maxHeartrate);
                
                // Ajouter à la liste formatée
                formattedActivities.add(activity);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("activities", formattedActivities);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}