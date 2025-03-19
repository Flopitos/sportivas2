package com.sportivas.sportivas.controller;


import com.sportivas.sportivas.dto.QuizResponseDto;
import com.sportivas.sportivas.dto.QuizSubmissionDto;
import com.sportivas.sportivas.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final QuizService quizService;

    @Autowired
    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping("/submit")
    public ResponseEntity<QuizResponseDto> submitQuiz(@RequestBody QuizSubmissionDto submissionDto) {
        QuizResponseDto savedResponse = quizService.saveQuizResponse(submissionDto);
        return new ResponseEntity<>(savedResponse, HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<QuizResponseDto>> getQuizResponsesByUserId(@PathVariable Long userId) {
        List<QuizResponseDto> responses = quizService.getQuizResponsesByUserId(userId);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<List<QuizResponseDto>> getQuizResponsesByUserIdAndDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<QuizResponseDto> responses = quizService.getQuizResponsesByUserIdAndDateRange(userId, startDate, endDate);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}/latest")
    public ResponseEntity<QuizResponseDto> getLatestQuizResponseByUserId(@PathVariable Long userId) {
        Optional<QuizResponseDto> response = quizService.getLatestQuizResponseByUserId(userId);
        return response
                .map(r -> new ResponseEntity<>(r, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/user/{userId}/completed-today")
    public ResponseEntity<Boolean> hasUserCompletedQuizToday(@PathVariable Long userId) {
        boolean completed = quizService.hasUserCompletedQuizToday(userId);
        return new ResponseEntity<>(completed, HttpStatus.OK);
    }
}