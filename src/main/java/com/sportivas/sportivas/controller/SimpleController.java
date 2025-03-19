package com.sportivas.sportivas.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sportivas.sportivas.model.Sport;
import com.sportivas.sportivas.model.User;
import com.sportivas.sportivas.repository.SportRepository;
import com.sportivas.sportivas.service.UserService;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
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
            @org.springframework.web.bind.annotation.RequestParam(required = false, defaultValue = "1 fois par semaine") String frequency,
            @org.springframework.web.bind.annotation.RequestParam(required = false, defaultValue = "Débutant") String level) {
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
}