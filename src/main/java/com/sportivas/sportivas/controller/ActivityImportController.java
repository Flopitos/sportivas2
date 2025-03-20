package com.sportivas.sportivas.controller;

import com.sportivas.sportivas.model.Activity;

import com.sportivas.sportivas.service.impl.ActivityImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/activities")
public class ActivityImportController {

    private final ActivityImportService activityImportService;

    @Autowired
    public ActivityImportController(ActivityImportService activityImportService) {
        this.activityImportService = activityImportService;
    }

    @PostMapping("/import")
    public ResponseEntity<String> importActivitiesFromJson(@RequestBody String jsonContent) {
        try {
            List<Activity> importedActivities = activityImportService.importActivitiesFromJson(jsonContent);
            return ResponseEntity.ok("Importé avec succès " + importedActivities.size() + " activités dans la base de données PostgreSQL");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de l'importation : " + e.getMessage());
        }
    }

    @PostMapping("/import-file")
    public ResponseEntity<String> importActivitiesFromFile(@RequestParam("file") MultipartFile file) {
        try {
            String jsonContent = new String(file.getBytes(), StandardCharsets.UTF_8);
            List<Activity> importedActivities = activityImportService.importActivitiesFromJson(jsonContent);
            return ResponseEntity.ok("Importé avec succès " + importedActivities.size() + " activités dans la base de données PostgreSQL");
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Erreur lors de la lecture du fichier : " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de l'importation : " + e.getMessage());
        }
    }
}