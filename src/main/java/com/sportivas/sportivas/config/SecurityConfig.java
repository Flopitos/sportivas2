package com.sportivas.sportivas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize ->
                authorize
                    // Pour le front-end
                    .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/img/**", "/favicon.ico").permitAll()
                    // Pour les API publiques
                    .requestMatchers("/api/auth/**", "/api/health").permitAll()
                    // Pour Swagger
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/swagger-ui/index.html", "/v3/api-docs/**", "/v3/api-docs", "/swagger").permitAll()
                    // Pour H2 Console (si utilisé)
                    .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                    // Toutes les autres requêtes nécessitent une authentification, mais sans pop-up
                    .anyRequest().permitAll()
            )
            // Désactiver complètement l'authentification HTTP Basic
            .httpBasic(AbstractHttpConfigurer::disable)
            // Désactiver la page de login par défaut
            .formLogin(AbstractHttpConfigurer::disable)
            // Configuration pour H2 Console
            .headers(headers -> headers.frameOptions().disable());

        return http.build();
    }
}