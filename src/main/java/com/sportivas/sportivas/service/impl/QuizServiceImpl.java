package com.sportivas.sportivas.service.impl;
import com.sportivas.sportivas.dto.QuizResponseDto;
import com.sportivas.sportivas.dto.QuizSubmissionDto;
import com.sportivas.sportivas.model.QuizResponse;
import com.sportivas.sportivas.repository.QuizRepository;
import com.sportivas.sportivas.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;

    @Autowired
    public QuizServiceImpl(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    @Override
    public QuizResponseDto saveQuizResponse(QuizSubmissionDto submissionDto) {
        QuizResponse quizResponse = new QuizResponse();
        quizResponse.setUserId(submissionDto.getUserId());
        quizResponse.setFeeling(submissionDto.getFeeling());
        quizResponse.setFatigueType(submissionDto.getFatigue());
        quizResponse.setHasPain(submissionDto.getMoralDouleurs());
        quizResponse.setInjuryType(submissionDto.getBlessure());
        quizResponse.setPainLocation(submissionDto.getZoneDouleurs());

        QuizResponse savedResponse = quizRepository.save(quizResponse);
        return mapToDto(savedResponse);
    }

    @Override
    public List<QuizResponseDto> getQuizResponsesByUserId(Long userId) {
        return quizRepository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<QuizResponseDto> getQuizResponsesByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        return quizRepository.findByUserIdAndDateRange(userId, startDateTime, endDateTime).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<QuizResponseDto> getLatestQuizResponseByUserId(Long userId) {
        return quizRepository.findLatestByUserId(userId)
                .map(this::mapToDto);
    }

    @Override
    public boolean hasUserCompletedQuizToday(Long userId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        List<QuizResponse> todaysResponses = quizRepository.findByUserIdAndDateRange(userId, startOfDay, endOfDay);
        return !todaysResponses.isEmpty();
    }

    private QuizResponseDto mapToDto(QuizResponse quizResponse) {
        return new QuizResponseDto(
                quizResponse.getId(),
                quizResponse.getUserId(),
                quizResponse.getFeeling(),
                quizResponse.getFatigueType(),
                quizResponse.getHasPain(),
                quizResponse.getInjuryType(),
                quizResponse.getPainLocation(),
                quizResponse.getCreatedAt()
        );
    }
}