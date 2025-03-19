package com.sportivas.sportivas.service;

import com.sportivas.sportivas.dto.QuizResponseDto;
import com.sportivas.sportivas.dto.QuizSubmissionDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface QuizService {

    /**
     * Sauvegarde une réponse de quiz
     * @param submissionDto les données du quiz soumises
     * @return la réponse sauvegardée
     */
    QuizResponseDto saveQuizResponse(QuizSubmissionDto submissionDto);

    /**
     * Récupère toutes les réponses de quiz pour un utilisateur
     * @param userId l'identifiant de l'utilisateur
     * @return la liste des réponses de quiz
     */
    List<QuizResponseDto> getQuizResponsesByUserId(Long userId);

    /**
     * Récupère les réponses de quiz pour un utilisateur dans une plage de dates
     * @param userId l'identifiant de l'utilisateur
     * @param startDate la date de début
     * @param endDate la date de fin
     * @return la liste des réponses de quiz dans la plage de dates
     */
    List<QuizResponseDto> getQuizResponsesByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * Récupère la dernière réponse de quiz pour un utilisateur
     * @param userId l'identifiant de l'utilisateur
     * @return la dernière réponse de quiz si elle existe
     */
    Optional<QuizResponseDto> getLatestQuizResponseByUserId(Long userId);

    /**
     * Vérifie si un utilisateur a déjà répondu au quiz aujourd'hui
     * @param userId l'identifiant de l'utilisateur
     * @return true si l'utilisateur a répondu au quiz aujourd'hui, false sinon
     */
    boolean hasUserCompletedQuizToday(Long userId);
}