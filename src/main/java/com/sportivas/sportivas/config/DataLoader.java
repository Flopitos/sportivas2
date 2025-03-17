package com.sportivas.sportivas.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.sportivas.sportivas.model.Sport;
import com.sportivas.sportivas.model.User;
import com.sportivas.sportivas.repository.SportRepository;
import com.sportivas.sportivas.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final SportRepository sportRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Loading initial data...");
        
        // Load only if no sports exist
        if (sportRepository.count() == 0) {
            loadSports();
        }
        
        // Create a demo user if none exists
        if (userRepository.count() == 0) {
            createDemoUser();
        }
        
        log.info("Initial data loaded successfully");
    }
    
    private void loadSports() {
        log.info("Loading sports...");
        
        Sport[] sports = {
            Sport.builder().name("Running").description("Jogging, sprinting, marathon").build(),
            Sport.builder().name("Swimming").description("Freestyle, backstroke, butterfly").build(),
            Sport.builder().name("Cycling").description("Road cycling, mountain biking").build(),
            Sport.builder().name("Soccer").description("Football, the beautiful game").build(),
            Sport.builder().name("Basketball").description("Indoor or outdoor team sport").build(),
            Sport.builder().name("Tennis").description("Singles or doubles racquet sport").build(),
            Sport.builder().name("Yoga").description("Physical, mental and spiritual practice").build(),
            Sport.builder().name("Weightlifting").description("Strength training with weights").build(),
            Sport.builder().name("Hiking").description("Long walks in nature").build(),
            Sport.builder().name("Golf").description("Precision club and ball sport").build()
        };
        
        for (Sport sport : sports) {
            sportRepository.save(sport);
            log.info("Saved sport: {}", sport.getName());
        }
    }
    
    private void createDemoUser() {
        log.info("Creating demo user...");
        
        User demoUser = User.builder()
                .insuranceNumber("123456789")
                .password(passwordEncoder.encode("password123"))
                .firstName("Demo")
                .lastName("User")
                .passwordChanged(false)
                .build();
        
        userRepository.save(demoUser);
        log.info("Demo user created with insurance number: {}", demoUser.getInsuranceNumber());
    }
}